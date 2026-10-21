package cl.nxiiio.reservafit.notificaciones.model;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// A message shown to a user in the page's notification bell. One per booking and type, so a
// message RabbitMQ delivers twice never shows the same notification twice (uq_notificacion_reserva).
@Entity
@Table(
    name = "notificacion",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_notificacion_reserva",
        columnNames = {"reserva_id", "tipo"}
    )
)
@Getter
@Setter
@NoArgsConstructor
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The user's Microsoft oid, as in ms-reservas' bookings
    @Column(name = "usuario_oid", length = 36, nullable = false)
    private String usuarioOid;

    // ms-reservas' booking id (plain id, no relation: bookings live in another service)
    @Column(name = "reserva_id", nullable = false)
    private Long reservaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", length = 20, nullable = false)
    private TipoNotificacion tipo;

    @Column(name = "mensaje", length = 300, nullable = false)
    private String mensaje;

    @Column(name = "leida", nullable = false)
    private boolean leida;

    // Set by Hibernate on insert. An Instant travels as UTC ("...Z"), so the page shows the
    // right "5 min ago" whatever time zone the server runs in
    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private Instant fechaCreacion;
}
