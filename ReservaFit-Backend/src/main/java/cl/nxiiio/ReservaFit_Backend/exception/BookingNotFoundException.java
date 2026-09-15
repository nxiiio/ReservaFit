package cl.nxiiio.ReservaFit_Backend.exception;

// Also thrown for another user's booking, so its existence is not leaked
public class BookingNotFoundException extends RuntimeException {

    public BookingNotFoundException(Long id) {
        super("Booking " + id + " does not exist for the current user");
    }
}
