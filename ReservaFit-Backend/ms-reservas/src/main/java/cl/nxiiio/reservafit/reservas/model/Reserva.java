package cl.nxiiio.reservafit.reservas.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

// One active booking per slot and date. "activa" is TRUE while CONFIRMADA and NULL once
// canceled: SQL UNIQUE treats NULLs as distinct, so canceled rows never block a re-booking.
@Entity
@Table(
    name = "reserva",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_reserva_slot",
        columnNames = {"gimnasio_id", "horario_id", "fecha", "activa"}
    )
)
@Getter
@Setter
@NoArgsConstructor
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Plain ids: users, gyms and schedules live in other services, so there are no JPA
    // relations or foreign keys to them
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "gimnasio_id", nullable = false)
    private Long gimnasioId;

    @Column(name = "horario_id", nullable = false)
    private Long horarioId;

    @Column(name = "fecha")
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20)
    private EstadoReserva estado;

    // TRUE while confirmed, NULL when canceled (never FALSE); see uq_reserva_slot
    @Column(name = "activa")
    private Boolean activa;

    // Set by Hibernate on insert, so it is available right after save()
    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;
}
