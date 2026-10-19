package cl.nxiiio.reservafit.reservas.messaging;

// RabbitMQ names used by ms-reservas. The exchange and routing keys must match the ones in
// ms-usuarios' and ms-gimnasios' own RabbitNames, or their messages never reach these queues.
public final class RabbitNames {

    // Exchanges
    public static final String EVENTS_EXCHANGE = "reservafit.events";
    public static final String DEAD_LETTER_EXCHANGE = "reservafit.dlx";

    // Routing keys of the messages
    public static final String USER_REGISTERED = "user.registered";
    public static final String SCHEDULE_PUBLISHED = "schedule.published";

    // Binding keys: which routing keys each queue accepts ("*" = exactly one word)
    public static final String USERS_BINDING = "user.*";
    public static final String SCHEDULES_BINDING = "schedule.*";

    // Queues read by ms-reservas, and their dead letter queues
    public static final String USERS_QUEUE = "reservas.users";
    public static final String USERS_DLQ = "reservas.users.dlq";

    public static final String SCHEDULES_QUEUE = "reservas.schedules";
    public static final String SCHEDULES_DLQ = "reservas.schedules.dlq";

    private RabbitNames() {
    }
}
