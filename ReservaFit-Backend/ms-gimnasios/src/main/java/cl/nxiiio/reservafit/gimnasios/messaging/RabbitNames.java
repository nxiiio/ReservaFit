package cl.nxiiio.reservafit.gimnasios.messaging;

// RabbitMQ names used by ms-gimnasios. ms-reservas publishes with the same exchange and
// routing keys in its own RabbitNames: if one changes, change it there too.
public final class RabbitNames {

    // Exchanges
    public static final String EVENTS_EXCHANGE = "reservafit.events";
    public static final String DEAD_LETTER_EXCHANGE = "reservafit.dlx";

    // Routing keys of the messages ms-reservas sends
    public static final String BOOKING_CREATED = "booking.created";
    public static final String BOOKING_CANCELLED = "booking.cancelled";

    // Binding key: the queue accepts both ("*" = exactly one word)
    public static final String BOOKINGS_BINDING = "booking.*";

    // Queue read by ms-gimnasios, and its dead letter queue
    public static final String BOOKINGS_QUEUE = "gimnasios.bookings";
    public static final String BOOKINGS_DLQ = "gimnasios.bookings.dlq";

    private RabbitNames() {
    }
}
