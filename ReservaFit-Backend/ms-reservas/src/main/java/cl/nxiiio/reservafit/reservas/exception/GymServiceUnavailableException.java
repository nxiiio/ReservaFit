package cl.nxiiio.reservafit.reservas.exception;

public class GymServiceUnavailableException extends RuntimeException {

    public GymServiceUnavailableException(Throwable cause) {
        super("Could not reach ms-gimnasios", cause);
    }
}
