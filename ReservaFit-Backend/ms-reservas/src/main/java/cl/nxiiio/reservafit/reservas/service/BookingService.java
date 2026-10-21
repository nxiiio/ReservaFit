package cl.nxiiio.reservafit.reservas.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
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
import cl.nxiiio.reservafit.reservas.messaging.BookingEvent;
import cl.nxiiio.reservafit.reservas.model.EstadoReserva;
import cl.nxiiio.reservafit.reservas.model.Reserva;
import cl.nxiiio.reservafit.reservas.repository.BookingRepository;
import lombok.RequiredArgsConstructor;

// Each booking belongs to the user's Microsoft oid (from the token), so this service keeps
// no user data. Schedules belong to ms-gimnasios, which is asked over HTTP (GymClient).
// Every booking that is created or canceled is sent to RabbitMQ (BookingEventPublisher).
// @RequiredArgsConstructor (Lombok) writes the constructor that receives the final fields.
@Service
@RequiredArgsConstructor
public class BookingService {

    public static final int MAX_DAYS_AHEAD = 30;

    private final BookingRepository bookingRepository;
    private final GymClient gymClient;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

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
        ScheduleInfo schedule = findSchedule(request.getScheduleId());
        LocalDate date = request.getDate();
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
        if (bookingRepository.existsByHorarioIdAndFechaAndActivaTrue(schedule.getId(), date)) {
            throw new SlotAlreadyBookedException();
        }

        Reserva booking = new Reserva();
        booking.setUsuarioOid(oid);
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
        eventPublisher.publishEvent(BookingEvent.from(booking, schedule));

        return toResponse(booking, schedule);
    }

    // Upcoming confirmed bookings first (soonest first), then past and canceled ones (newest first)
    @Transactional(readOnly = true)
    public List<BookingResponse> findMine(String oid) {
        LocalDate today = LocalDate.now(clock);

        List<Reserva> bookings = new ArrayList<>(bookingRepository.findByUsuarioOidOrderByFechaAsc(oid));

        // Schedule of each booking, by schedule id, asked once per schedule to ms-gimnasios
        Map<Long, ScheduleInfo> schedulesById = bookings.stream()
                .map(Reserva::getHorarioId)
                .distinct()
                .map(this::findSchedule)
                .collect(Collectors.toMap(ScheduleInfo::getId, schedule -> schedule));

        // The query already orders by date; within the same date, order by start time
        bookings.sort((first, second) -> {
            if (!first.getFecha().equals(second.getFecha())) {
                return first.getFecha().compareTo(second.getFecha());
            }
            LocalTime firstStart = schedulesById.get(first.getHorarioId()).getStartTime();
            LocalTime secondStart = schedulesById.get(second.getHorarioId()).getStartTime();
            return firstStart.compareTo(secondStart);
        });

        // true: upcoming (confirmed and not in the past; today counts). false: the history
        Map<Boolean, List<Reserva>> byUpcoming = bookings.stream()
                .collect(Collectors.partitioningBy(booking ->
                        booking.getEstado() == EstadoReserva.CONFIRMADA && !booking.getFecha().isBefore(today)));

        // Upcoming keeps the order (soonest first); the history is reversed (newest first)
        return Stream.concat(byUpcoming.get(true).stream(), byUpcoming.get(false).reversed().stream())
                .map(booking -> toResponse(booking, schedulesById.get(booking.getHorarioId())))
                .toList();
    }

    @Transactional
    public BookingResponse cancel(String oid, Long bookingId) {
        // Another user's booking answers 404 too, so it does not reveal that it exists
        Reserva booking = bookingRepository.findById(bookingId)
                .filter(b -> b.getUsuarioOid().equals(oid))
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
        eventPublisher.publishEvent(BookingEvent.from(booking, schedule));

        return toResponse(booking, schedule);
    }

    // Empty when ms-gimnasios answers 404
    private ScheduleInfo findSchedule(Long scheduleId) {
        return gymClient.findSchedule(scheduleId)
                .orElseThrow(() -> new ScheduleNotFoundException(scheduleId));
    }

    // True when the slot's date has passed, or it is today and the slot already began
    private boolean hasStarted(ScheduleInfo schedule, LocalDate date) {
        LocalDate today = LocalDate.now(clock);
        return date.isBefore(today)
                || (date.equals(today) && !schedule.getStartTime().isAfter(LocalTime.now(clock)));
    }

    // A confirmed booking can be canceled until its slot starts
    private BookingResponse toResponse(Reserva booking, ScheduleInfo schedule) {
        boolean cancellable = booking.getEstado() == EstadoReserva.CONFIRMADA
                && !hasStarted(schedule, booking.getFecha());
        return BookingResponse.from(booking, schedule, cancellable);
    }
}
