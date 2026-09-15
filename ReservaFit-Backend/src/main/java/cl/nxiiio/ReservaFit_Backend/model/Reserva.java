package cl.nxiiio.ReservaFit_Backend.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gimnasio_id")
    private Gimnasio gimnasio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "horario_id")
    private Horario horario;

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
