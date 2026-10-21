package cl.nxiiio.reservafit.notificaciones.dto;

import java.time.Instant;

import cl.nxiiio.reservafit.notificaciones.model.Notificacion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private Long bookingId;
    private String type;
    private String message;
    private boolean read;
    private Instant createdAt;

    public static NotificationResponse from(Notificacion notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getReservaId(),
                notification.getTipo().name(),
                notification.getMensaje(),
                notification.isLeida(),
                notification.getFechaCreacion());
    }
}
