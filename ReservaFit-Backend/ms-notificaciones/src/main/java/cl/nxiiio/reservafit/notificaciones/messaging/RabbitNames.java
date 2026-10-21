package cl.nxiiio.reservafit.notificaciones.messaging;

// RabbitMQ names used by ms-notificaciones. ms-reservas publishes with the same exchange and
// routing keys in its own RabbitNames: if one changes, change it there too.
public final class RabbitNames {

    // Exchanges
    public static final String EVENTS_EXCHANGE = "reservafit.events";
    public static final String DEAD_LETTER_EXCHANGE = "reservafit.dlx";

    // Binding key: the queue accepts booking.created and booking.cancelled ("*" = exactly one word)
    public static final String BOOKINGS_BINDING = "booking.*";

    // Queue read by ms-notificaciones, and its dead letter queue. ms-gimnasios has its own
    // queue with the same binding, so each service gets its own copy of every message.
    public static final String BOOKINGS_QUEUE = "notificaciones.bookings";
    public static final String BOOKINGS_DLQ = "notificaciones.bookings.dlq";

    private RabbitNames() {
    }
}
