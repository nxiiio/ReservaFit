package cl.nxiiio.ReservaFit_Backend.exception;

import lombok.Getter;

// Carries the request field and a user-facing (Spanish) message for the 400 body
@Getter
public class InvalidBookingDateException extends RuntimeException {

    private final String field;

    public InvalidBookingDateException(String field, String message) {
        super(message);
        this.field = field;
    }
}
