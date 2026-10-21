package cl.nxiiio.reservafit.gimnasios.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// The gym's record of a booking made in ms-reservas, filled from RabbitMQ messages.
// The id is ms-reservas' own booking id; the user is their Microsoft oid. Plain values,
// no relations: bookings and users belong to other services.
@Entity
@Table(name = "reserva_gimnasio")
@Getter
@Setter
@NoArgsConstructor
public class ReservaGimnasio {

    @Id
    private Long id;

    @Column(name = "usuario_oid", length = 36)
    private String usuarioOid;

    @Column(name = "gimnasio_id", nullable = false)
    private Long gimnasioId;

    @Column(name = "horario_id")
    private Long horarioId;

    @Column(name = "fecha")
    private LocalDate fecha;

    // "CONFIRMADA" or "CANCELADA"
    @Column(name = "estado", length = 20)
    private String estado;

    // When the last message about this booking arrived
    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}
