package cl.nxiiio.ReservaFit_Backend.service;

public class RutAlreadyRegisteredException extends RuntimeException {

    public RutAlreadyRegisteredException() {
        super("RUT is already registered to another user");
    }
}
