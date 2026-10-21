package cl.nxiiio.reservafit.notificaciones.exception;

public class NotificationNotFoundException extends RuntimeException {

    public NotificationNotFoundException(Long id) {
        super("Notification " + id + " does not exist for the current user");
    }
}
