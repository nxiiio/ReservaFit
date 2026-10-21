package cl.nxiiio.reservafit.reservas.exception;

public class GymNotFoundException extends RuntimeException {

    public GymNotFoundException(Long id) {
        super("Gym " + id + " does not exist");
    }
}
