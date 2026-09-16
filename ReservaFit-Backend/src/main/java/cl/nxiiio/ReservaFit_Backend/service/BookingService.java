package cl.nxiiio.ReservaFit_Backend.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.nxiiio.ReservaFit_Backend.dto.BookingResponse;
import cl.nxiiio.ReservaFit_Backend.dto.CreateBookingRequest;
import cl.nxiiio.ReservaFit_Backend.dto.SlotAvailabilityResponse;
import cl.nxiiio.ReservaFit_Backend.exception.AlreadyCancelledException;
import cl.nxiiio.ReservaFit_Backend.exception.BookingAlreadyStartedException;
import cl.nxiiio.ReservaFit_Backend.exception.BookingNotFoundException;
import cl.nxiiio.ReservaFit_Backend.exception.GymNotFoundException;
import cl.nxiiio.ReservaFit_Backend.exception.InvalidBookingDateException;
import cl.nxiiio.ReservaFit_Backend.exception.ScheduleNotFoundException;
import cl.nxiiio.ReservaFit_Backend.exception.SlotAlreadyBookedException;
import cl.nxiiio.ReservaFit_Backend.model.EstadoReserva;
import cl.nxiiio.ReservaFit_Backend.model.Horario;
import cl.nxiiio.ReservaFit_Backend.model.Reserva;
import cl.nxiiio.ReservaFit_Backend.model.Usuario;
import cl.nxiiio.ReservaFit_Backend.repository.BookingRepository;
import cl.nxiiio.ReservaFit_Backend.repository.GymRepository;
import cl.nxiiio.ReservaFit_Backend.repository.ScheduleRepository;

// Every method maps entities to DTOs inside its transaction (associations are LAZY)
@Service
public class BookingService {

    public static final int MAX_DAYS_AHEAD = 30;

    private final BookingRepository bookingRepository;
    private final ScheduleRepository scheduleRepository;
    private final GymRepository gymRepository;
    private final UserService userService;
    private final Clock clock;

    public BookingService(
            BookingRepository bookingRepository,
            ScheduleRepository scheduleRepository,
            GymRepository gymRepository,
            UserService userService,
            Clock clock) {
        this.bookingRepository = bookingRepository;
        this.scheduleRepository = scheduleRepository;
        this.gymRepository = gymRepository;
        this.userService = userService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<SlotAvailabilityResponse> getAvailability(Long gymId, LocalDate date) {

        if (!gymRepository.existsById(gymId)) {
            throw new GymNotFoundException(gymId);
        }
        
        Set<Long> taken = Set.copyOf(bookingRepository.findActiveScheduleIds(gymId, date));
        LocalDate today = LocalDate.now(clock);
        boolean dateBookable = !date.isBefore(today) && !date.isAfter(today.plusDays(MAX_DAYS_AHEAD));

        return scheduleRepository.findByGimnasioIdOrderByHoraInicioAsc(gymId).stream()
                .map(schedule -> new SlotAvailabilityResponse(
                        schedule.getId(),
                        schedule.getHoraInicio(),
                        schedule.getHoraFin(),
                        dateBookable && !taken.contains(schedule.getId()) && !hasStarted(schedule, date)))
                .toList();
    }

    @Transactional
    public BookingResponse create(String oid, CreateBookingRequest request) {
        Usuario user = userService.findByOid(oid);
        Horario schedule = scheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new ScheduleNotFoundException(request.getScheduleId()));
        LocalDate date = request.getDate();
        validateBookingDate(schedule, date);

        if (bookingRepository.existsByHorarioIdAndFechaAndActivaTrue(schedule.getId(), date)) {
            throw new SlotAlreadyBookedException();
        }

        Reserva booking = new Reserva();
        booking.setUsuario(user);
        booking.setGimnasio(schedule.getGimnasio());
        booking.setHorario(schedule);
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
        return toResponse(booking);
    }

    // Upcoming confirmed bookings first (soonest first), then past and canceled ones (newest first)
    @Transactional(readOnly = true)
    public List<BookingResponse> findMine(String oid) {
        Usuario user = userService.findByOid(oid);
        LocalDate today = LocalDate.now(clock);
        Comparator<Reserva> byDateAndTime = Comparator
                .comparing(Reserva::getFecha)
                .thenComparing(booking -> booking.getHorario().getHoraInicio());

        var partitioned = bookingRepository.findAllWithDetailsByUserId(user.getId()).stream()
                .collect(Collectors.partitioningBy(booking -> booking.getEstado() == EstadoReserva.CONFIRMADA
                        && !booking.getFecha().isBefore(today)));
        Stream<Reserva> upcoming = partitioned.get(true).stream().sorted(byDateAndTime);
        Stream<Reserva> history = partitioned.get(false).stream().sorted(byDateAndTime.reversed());

        return Stream.concat(upcoming, history).map(this::toResponse).toList();
    }

    @Transactional
    public BookingResponse cancel(String oid, Long bookingId) {
        Usuario user = userService.findByOid(oid);
        Reserva booking = bookingRepository.findWithDetailsById(bookingId)
                .filter(b -> b.getUsuario().getId().equals(user.getId()))
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
        if (booking.getEstado() == EstadoReserva.CANCELADA) {
            throw new AlreadyCancelledException();
        }
        if (hasStarted(booking.getHorario(), booking.getFecha())) {
            throw new BookingAlreadyStartedException();
        }
        booking.setEstado(EstadoReserva.CANCELADA);
        booking.setActiva(null);
        return toResponse(booking);
    }

    private void validateBookingDate(Horario schedule, LocalDate date) {
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

    // True when the slot's date has passed, or it is today and the slot already began
    private boolean hasStarted(Horario schedule, LocalDate date) {
        LocalDate today = LocalDate.now(clock);
        return date.isBefore(today)
                || (date.equals(today) && !schedule.getHoraInicio().isAfter(LocalTime.now(clock)));
    }

    // A confirmed booking can be canceled until its slot starts
    private BookingResponse toResponse(Reserva booking) {
        boolean cancellable = booking.getEstado() == EstadoReserva.CONFIRMADA
                && !hasStarted(booking.getHorario(), booking.getFecha());
        return BookingResponse.from(booking, cancellable);
    }
}
