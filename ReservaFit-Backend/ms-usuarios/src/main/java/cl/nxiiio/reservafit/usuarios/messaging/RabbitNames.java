package cl.nxiiio.reservafit.usuarios.messaging;

// RabbitMQ names used by ms-usuarios. ms-reservas has the same values in its own RabbitNames:
// if one changes, change it there too, or the messages stop arriving.
public final class RabbitNames {

    public static final String EVENTS_EXCHANGE = "reservafit.events";

    // Routing key of the message this service publishes
    public static final String USER_REGISTERED = "user.registered";

    private RabbitNames() {
    }
}
