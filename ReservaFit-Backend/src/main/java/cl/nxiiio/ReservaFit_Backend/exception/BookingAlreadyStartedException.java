package cl.nxiiio.ReservaFit_Backend.exception;

public class BookingAlreadyStartedException extends RuntimeException {

    public BookingAlreadyStartedException() {
        super("Booking has already started and can no longer be canceled");
    }
}
