package cl.nxiiio.reservafit.reservas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Local copy of the users owned by ms-usuarios. The id is ms-usuarios' own id (not generated
// here), so bookings keep pointing at the same user. Filled from RabbitMQ messages.
@Entity
@Table(name = "usuario_replica")
@Getter
@Setter
@NoArgsConstructor
public class UsuarioReplica {

    @Id
    private Long id;

    @Column(name = "microsoft_oid", length = 36, nullable = false, unique = true)
    private String microsoftOid;

    @Column(name = "nombre", length = 100)
    private String nombre;

    @Column(name = "email", length = 150)
    private String email;
}
