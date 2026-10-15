package cl.nxiiio.reservafit.reservas.model;

import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Local copy of the schedules owned by ms-gimnasios, with the gym data that booking
// responses show. The id is ms-gimnasios' own horario id. Filled from RabbitMQ messages.
@Entity
@Table(name = "horario_replica")
@Getter
@Setter
@NoArgsConstructor
public class HorarioReplica {

    @Id
    private Long id;

    @Column(name = "gimnasio_id", nullable = false)
    private Long gimnasioId;

    @Column(name = "gimnasio_nombre", length = 150)
    private String gimnasioNombre;

    @Column(name = "gimnasio_comuna", length = 80)
    private String gimnasioComuna;

    @Column(name = "gimnasio_imagen_url", length = 500)
    private String gimnasioImagenUrl;

    @Column(name = "hora_inicio")
    private LocalTime horaInicio;

    @Column(name = "hora_fin")
    private LocalTime horaFin;
}
