package cl.nxiiio.ReservaFit_Backend.exception;

public class GymNotFoundException extends RuntimeException {

    public GymNotFoundException(Long id) {
        super("Gym " + id + " does not exist");
    }
}
