package cl.nxiiio.reservafit.reservas.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.nxiiio.reservafit.reservas.client.GymClient;
import cl.nxiiio.reservafit.reservas.client.ScheduleInfo;
import cl.nxiiio.reservafit.reservas.dto.BookingResponse;
import cl.nxiiio.reservafit.reservas.dto.CreateBookingRequest;
import cl.nxiiio.reservafit.reservas.dto.SlotAvailabilityResponse;
import cl.nxiiio.reservafit.reservas.exception.AlreadyCancelledException;
import cl.nxiiio.reservafit.reservas.exception.BookingAlreadyStartedException;
import cl.nxiiio.reservafit.reservas.exception.BookingNotFoundException;
import cl.nxiiio.reservafit.reservas.exception.GymNotFoundException;
import cl.nxiiio.reservafit.reservas.exception.InvalidBookingDateException;
import cl.nxiiio.reservafit.reservas.exception.ScheduleNotFoundException;
import cl.nxiiio.reservafit.reservas.exception.SlotAlreadyBookedException;
import cl.nxiiio.reservafit.reservas.exception.UserNotFoundException;
import cl.nxiiio.reservafit.reservas.model.EstadoReserva;
import cl.nxiiio.reservafit.reservas.messaging.BookingEvent;
import cl.nxiiio.reservafit.reservas.model.Reserva;
import cl.nxiiio.reservafit.reservas.model.UsuarioReplica;
import cl.nxiiio.reservafit.reservas.repository.BookingRepository;
import cl.nxiiio.reservafit.reservas.repository.UserReplicaRepository;

// Users and schedules belong to other services. Users are read from the local copy that
// RabbitMQ keeps up to date (UsuarioReplica); schedules are asked to ms-gimnasios (GymClient).
// Every booking that is created or canceled is sent to RabbitMQ (BookingEventPublisher).
@Service
public class BookingService {

    public static final int MAX_DAYS_AHEAD = 30;

    private final BookingRepository bookingRepository;
    private final UserReplicaRepository userRepository;
    private final GymClient gymClient;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    public BookingService(
            BookingRepository bookingRepository,
            UserReplicaRepository userRepository,
            GymClient gymClient,
            ApplicationEventPublisher eventPublisher,
            Clock clock) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.gymClient = gymClient;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<SlotAvailabilityResponse> getAvailability(Long gymId, LocalDate date) {
        List<ScheduleInfo> schedules = gymClient.findSchedulesOfGym(gymId)
                .orElseThrow(() -> new GymNotFoundException(gymId));

        Set<Long> taken = Set.copyOf(bookingRepository.findActiveScheduleIds(gymId, date));

        LocalDate today = LocalDate.now(clock);
        boolean dateBookable = !date.isBefore(today) && !date.isAfter(today.plusDays(MAX_DAYS_AHEAD));

        return schedules.stream()
                .map(schedule -> new SlotAvailabilityResponse(
                        schedule.getId(),
                        schedule.getStartTime(),
                        schedule.getEndTime(),
                        dateBookable && !taken.contains(schedule.getId()) && !hasStarted(schedule, date)))
                .toList();
    }

    @Transactional
    public BookingResponse create(String oid, CreateBookingRequest request) {
        UsuarioReplica user = findUser(oid);
        ScheduleInfo schedule = findSchedule(request.getScheduleId());
        LocalDate date = request.getDate();

        validateBookingDate(schedule, date);

        if (bookingRepository.existsByHorarioIdAndFechaAndActivaTrue(schedule.getId(), date)) {
            throw new SlotAlreadyBookedException();
        }

        Reserva booking = new Reserva();
        booking.setUsuarioId(user.getId());
        booking.setGimnasioId(schedule.getGymId());
        booking.setHorarioId(schedule.getId());
        booking.setFecha(date);
        booking.setEstado(EstadoReserva.CONFIRMADA);
        booking.setActiva(Boolean.TRUE);

        try {
            // Flush now so a uq_reserva_slot violation surfaces here, not at commit
            bookingRepository.saveAndFlush(booking);
        } catch (DataIntegrityViolationException e) {
            // A concurrent request took the slot between the check above and this insert
            throw new SlotAlreadyBookedException();
        }

        // Sent to RabbitMQ once the booking is committed (booking.created)
        eventPublisher.publishEvent(toEvent(booking));

        return toResponse(booking, schedule);
    }

    // Upcoming confirmed bookings first (soonest first), then past and canceled ones (newest first)
    @Transactional(readOnly = true)
    public List<BookingResponse> findMine(String oid) {
        UsuarioReplica user = findUser(oid);
        LocalDate today = LocalDate.now(clock);

        List<Reserva> bookings = new ArrayList<>(bookingRepository.findByUsuarioIdOrderByFechaAsc(user.getId()));

        // Schedule of each booking, by schedule id, asked once per schedule to ms-gimnasios
        Map<Long, ScheduleInfo> schedulesById = bookings.stream()
                .map(Reserva::getHorarioId)
                .distinct()
                .map(this::findSchedule)
                .collect(Collectors.toMap(ScheduleInfo::getId, schedule -> schedule));

        // Oldest first: by date, and by start time within the same date
        bookings.sort((first, second) -> compareByDateAndTime(first, second, schedulesById));

        // Upcoming keeps that order (soonest first)
        List<Reserva> upcoming = bookings.stream()
                .filter(booking -> isUpcoming(booking, today))
                .toList();

        // History is reversed so the newest comes first
        List<Reserva> history = new ArrayList<>(bookings.stream()
                .filter(booking -> !isUpcoming(booking, today))
                .toList());
        Collections.reverse(history);

        return Stream.concat(upcoming.stream(), history.stream())
                .map(booking -> toResponse(booking, schedulesById.get(booking.getHorarioId())))
                .toList();
    }

    @Transactional
    public BookingResponse cancel(String oid, Long bookingId) {
        UsuarioReplica user = findUser(oid);
        Reserva booking = bookingRepository.findById(bookingId)
                .filter(b -> b.getUsuarioId().equals(user.getId()))
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
        ScheduleInfo schedule = findSchedule(booking.getHorarioId());

        if (booking.getEstado() == EstadoReserva.CANCELADA) {
            throw new AlreadyCancelledException();
        }
        if (hasStarted(schedule, booking.getFecha())) {
            throw new BookingAlreadyStartedException();
        }

        booking.setEstado(EstadoReserva.CANCELADA);
        booking.setActiva(null);

        // Sent to RabbitMQ once the cancellation is committed (booking.cancelled)
        eventPublisher.publishEvent(toEvent(booking));

        return toResponse(booking, schedule);
    }

    private UsuarioReplica findUser(String oid) {
        return userRepository.findByMicrosoftOid(oid).orElseThrow(UserNotFoundException::new);
    }

    // Empty when ms-gimnasios answers 404
    private ScheduleInfo findSchedule(Long scheduleId) {
        return gymClient.findSchedule(scheduleId)
                .orElseThrow(() -> new ScheduleNotFoundException(scheduleId));
    }

    private void validateBookingDate(ScheduleInfo schedule, LocalDate date) {
        LocalDate today = LocalDate.now(clock);

        if (date.isBefore(today)) {
            throw new InvalidBookingDateException("date", "La fecha no puede ser anterior a hoy");
        }
        if (date.isAfter(today.plusDays(MAX_DAYS_AHEAD))) {
            throw new InvalidBookingDateException("date",
                    "Solo puedes reservar con hasta " + MAX_DAYS_AHEAD + " días de anticipación");
        }
        if (hasStarted(schedule, date)) {
            throw new InvalidBookingDateException("scheduleId", "Este horario ya comenzó");
        }
    }

    // Confirmed and not in the past (today counts as upcoming)
    private boolean isUpcoming(Reserva booking, LocalDate today) {
        return booking.getEstado() == EstadoReserva.CONFIRMADA && !booking.getFecha().isBefore(today);
    }

    // True when the slot's date has passed, or it is today and the slot already began
    private boolean hasStarted(ScheduleInfo schedule, LocalDate date) {
        LocalDate today = LocalDate.now(clock);
        return date.isBefore(today)
                || (date.equals(today) && !schedule.getStartTime().isAfter(LocalTime.now(clock)));
    }

    // Negative when "first" goes before "second", positive when after, 0 when they are equal
    private int compareByDateAndTime(Reserva first, Reserva second, Map<Long, ScheduleInfo> schedulesById) {
        int byDate = first.getFecha().compareTo(second.getFecha());
        if (byDate != 0) {
            return byDate;
        }

        LocalTime firstStart = schedulesById.get(first.getHorarioId()).getStartTime();
        LocalTime secondStart = schedulesById.get(second.getHorarioId()).getStartTime();
        return firstStart.compareTo(secondStart);
    }

    // A confirmed booking can be canceled until its slot starts
    private BookingResponse toResponse(Reserva booking, ScheduleInfo schedule) {
        boolean cancellable = booking.getEstado() == EstadoReserva.CONFIRMADA
                && !hasStarted(schedule, booking.getFecha());
        return BookingResponse.from(booking, schedule, cancellable);
    }

    private BookingEvent toEvent(Reserva booking) {
        return new BookingEvent(
                booking.getId(),
                booking.getUsuarioId(),
                booking.getGimnasioId(),
                booking.getHorarioId(),
                booking.getFecha(),
                booking.getEstado().name());
    }
}
