package cl.nxiiio.reservafit.reservas.messaging;

// RabbitMQ names used by ms-reservas. The exchange and routing keys must match the ones in
// ms-usuarios' and ms-gimnasios' own RabbitNames, or the messages never reach their queues.
public final class RabbitNames {

    // Exchanges
    public static final String EVENTS_EXCHANGE = "reservafit.events";
    public static final String DEAD_LETTER_EXCHANGE = "reservafit.dlx";

    // Routing key of the message ms-usuarios sends
    public static final String USER_REGISTERED = "user.registered";

    // Routing keys of the messages this service sends (ms-gimnasios reads them)
    public static final String BOOKING_CREATED = "booking.created";
    public static final String BOOKING_CANCELLED = "booking.cancelled";

    // Binding key: which routing keys the users queue accepts ("*" = exactly one word)
    public static final String USERS_BINDING = "user.*";

    // Queue read by ms-reservas, and its dead letter queue
    public static final String USERS_QUEUE = "reservas.users";
    public static final String USERS_DLQ = "reservas.users.dlq";

    private RabbitNames() {
    }
}
