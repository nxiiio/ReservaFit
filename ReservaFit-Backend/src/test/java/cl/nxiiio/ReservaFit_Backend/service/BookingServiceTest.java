package cl.nxiiio.ReservaFit_Backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import cl.nxiiio.ReservaFit_Backend.config.ClockConfig;
import cl.nxiiio.ReservaFit_Backend.dto.BookingResponse;
import cl.nxiiio.ReservaFit_Backend.dto.CreateBookingRequest;
import cl.nxiiio.ReservaFit_Backend.dto.SlotAvailabilityResponse;
import cl.nxiiio.ReservaFit_Backend.exception.AlreadyCancelledException;
import cl.nxiiio.ReservaFit_Backend.exception.BookingNotFoundException;
import cl.nxiiio.ReservaFit_Backend.exception.GymNotFoundException;
import cl.nxiiio.ReservaFit_Backend.exception.InvalidBookingDateException;
import cl.nxiiio.ReservaFit_Backend.exception.ScheduleNotFoundException;
import cl.nxiiio.ReservaFit_Backend.exception.SlotAlreadyBookedException;
import cl.nxiiio.ReservaFit_Backend.exception.UserNotFoundException;
import cl.nxiiio.ReservaFit_Backend.model.EstadoReserva;
import cl.nxiiio.ReservaFit_Backend.model.Horario;
import cl.nxiiio.ReservaFit_Backend.model.Reserva;
import cl.nxiiio.ReservaFit_Backend.model.Usuario;
import cl.nxiiio.ReservaFit_Backend.repository.BookingRepository;
import cl.nxiiio.ReservaFit_Backend.repository.GymRepository;
import cl.nxiiio.ReservaFit_Backend.repository.ScheduleRepository;
import cl.nxiiio.ReservaFit_Backend.repository.UserRepository;

@SpringBootTest
@Transactional
class BookingServiceTest {

    // Monday 2026-09-14, 10:00 in Santiago
    private static final LocalDate TODAY = LocalDate.of(2026, 9, 14);
    private static final Clock CLOCK = Clock.fixed(
            LocalDateTime.of(TODAY, LocalTime.of(10, 0)).atZone(ClockConfig.ZONE).toInstant(), ClockConfig.ZONE);
    private static final String OID_A = "oid-user-a";
    private static final String OID_B = "oid-user-b";

    @Autowired private BookingRepository bookingRepository;
    @Autowired private ScheduleRepository scheduleRepository;
    @Autowired private GymRepository gymRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private UserService userService;

    private BookingService service;
    private Usuario userA;
    private Horario earlySlot; // gym 1, 07:00
    private Horario eveningSlot; // gym 1, 18:00

    @BeforeEach
    void setUp() {
        service = serviceAt(CLOCK);
        userA = userRepository.save(user(OID_A));
        userRepository.save(user(OID_B));
        List<Horario> slots = scheduleRepository.findByGimnasioIdOrderByHoraInicioAsc(1L);
        earlySlot = slots.get(0);
        eveningSlot = slots.get(2);
        assertEquals(LocalTime.of(7, 0), earlySlot.getHoraInicio());
        assertEquals(LocalTime.of(18, 0), eveningSlot.getHoraInicio());
    }

    private BookingService serviceAt(Clock clock) {
        return new BookingService(bookingRepository, scheduleRepository, gymRepository, userService, clock);
    }

    private static Usuario user(String oid) {
        Usuario user = new Usuario();
        user.setMicrosoftOid(oid);
        user.setNombre(oid);
        return user;
    }

    private BookingResponse book(String oid, Horario slot, LocalDate date) {
        return service.create(oid, new CreateBookingRequest(slot.getId(), date));
    }

    @Test
    void createsAConfirmedBooking() {
        BookingResponse booking = book(OID_A, earlySlot, TODAY.plusDays(3));

        assertEquals("CONFIRMADA", booking.getStatus());
        assertEquals(1L, booking.getGymId());
        assertEquals(earlySlot.getId(), booking.getScheduleId());
        assertTrue(booking.isCancellable()); // Monday -> Thursday
        assertNotNull(booking.getCreatedAt());
    }

    @Test
    void rejectsPastDates() {
        InvalidBookingDateException e = assertThrows(InvalidBookingDateException.class,
                () -> book(OID_A, earlySlot, TODAY.minusDays(1)));
        assertEquals("date", e.getField());
    }

    @Test
    void acceptsUpTo30DaysAheadButNotMore() {
        book(OID_A, earlySlot, TODAY.plusDays(30));
        InvalidBookingDateException e = assertThrows(InvalidBookingDateException.class,
                () -> book(OID_A, earlySlot, TODAY.plusDays(31)));
        assertEquals("date", e.getField());
    }

    @Test
    void todayOnlyAcceptsSlotsThatHaveNotStarted() {
        InvalidBookingDateException e = assertThrows(InvalidBookingDateException.class,
                () -> book(OID_A, earlySlot, TODAY));
        assertEquals("scheduleId", e.getField());
        book(OID_A, eveningSlot, TODAY);
    }

    @Test
    void rejectsUnknownSchedule() {
        assertThrows(ScheduleNotFoundException.class,
                () -> service.create(OID_A, new CreateBookingRequest(9999L, TODAY.plusDays(1))));
    }

    @Test
    void rejectsUnregisteredUser() {
        assertThrows(UserNotFoundException.class, () -> book("unknown-oid", earlySlot, TODAY.plusDays(1)));
    }

    @Test
    void rejectsATakenSlot() {
        book(OID_A, earlySlot, TODAY.plusDays(5));
        assertThrows(SlotAlreadyBookedException.class, () -> book(OID_B, earlySlot, TODAY.plusDays(5)));
    }

    @Test
    void anotherUserCannotSeeOrCancelTheBooking() {
        BookingResponse booking = book(OID_A, earlySlot, TODAY.plusDays(10));
        assertThrows(BookingNotFoundException.class, () -> service.cancel(OID_B, booking.getId()));
        assertThrows(BookingNotFoundException.class, () -> service.cancel(OID_A, 9999L));
    }

    @Test
    void canceledSlotCanBeBookedAgain() {
        BookingResponse first = book(OID_A, earlySlot, TODAY.plusDays(3));
        BookingResponse canceled = service.cancel(OID_A, first.getId());
        assertEquals("CANCELADA", canceled.getStatus());
        assertFalse(canceled.isCancellable());
        assertThrows(AlreadyCancelledException.class, () -> service.cancel(OID_A, first.getId()));

        BookingResponse second = book(OID_B, earlySlot, TODAY.plusDays(3));
        assertEquals("CONFIRMADA", second.getStatus());

        // And that one can be canceled too, leaving two canceled rows for the same slot
        service.cancel(OID_B, second.getId());
        book(OID_A, earlySlot, TODAY.plusDays(3));
    }

    @Test
    void availabilityMarksTakenAndStartedSlots() {
        book(OID_A, eveningSlot, TODAY);

        List<SlotAvailabilityResponse> slots = service.getAvailability(1L, TODAY);
        assertEquals(5, slots.size());
        assertFalse(slot(slots, earlySlot).isAvailable()); // already started
        assertFalse(slot(slots, eveningSlot).isAvailable()); // booked
        assertTrue(slots.get(4).isAvailable()); // 20:00

        assertTrue(slot(service.getAvailability(1L, TODAY.plusDays(1)), earlySlot).isAvailable());
        assertThrows(GymNotFoundException.class, () -> service.getAvailability(9999L, TODAY));
    }

    private static SlotAvailabilityResponse slot(List<SlotAvailabilityResponse> slots, Horario schedule) {
        return slots.stream().filter(s -> s.getId().equals(schedule.getId())).findFirst().orElseThrow();
    }

    @Test
    void listsUpcomingBookingsFirstThenHistory() {
        BookingResponse later = book(OID_A, earlySlot, TODAY.plusDays(8));
        BookingResponse soonEvening = book(OID_A, eveningSlot, TODAY.plusDays(4));
        BookingResponse soonEarly = book(OID_A, earlySlot, TODAY.plusDays(4));
        BookingResponse canceled = book(OID_A, eveningSlot, TODAY.plusDays(20));
        service.cancel(OID_A, canceled.getId());
        Reserva lastWeek = pastBooking(TODAY.minusDays(7));
        Reserva yesterday = pastBooking(TODAY.minusDays(1));
        book(OID_B, eveningSlot, TODAY.plusDays(1)); // someone else's booking

        List<Long> ids = service.findMine(OID_A).stream().map(BookingResponse::getId).toList();

        assertEquals(List.of(soonEarly.getId(), soonEvening.getId(), later.getId(),
                canceled.getId(), yesterday.getId(), lastWeek.getId()), ids);
    }

    private Reserva pastBooking(LocalDate date) {
        Reserva booking = new Reserva();
        booking.setUsuario(userA);
        booking.setGimnasio(earlySlot.getGimnasio());
        booking.setHorario(earlySlot);
        booking.setFecha(date);
        booking.setEstado(EstadoReserva.CONFIRMADA);
        booking.setActiva(Boolean.TRUE);
        return bookingRepository.saveAndFlush(booking);
    }
}
