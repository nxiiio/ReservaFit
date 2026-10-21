package cl.nxiiio.reservafit.reservas.messaging;

// RabbitMQ names used by ms-reservas. ms-gimnasios has the same values in its own RabbitNames:
// if one changes, change it there too, or the messages stop arriving.
public final class RabbitNames {

    public static final String EVENTS_EXCHANGE = "reservafit.events";

    // Routing keys of the messages this service sends (ms-gimnasios reads them)
    public static final String BOOKING_CREATED = "booking.created";
    public static final String BOOKING_CANCELLED = "booking.cancelled";

    private RabbitNames() {
    }
}
