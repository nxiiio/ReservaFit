package cl.nxiiio.ReservaFit_Backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "gimnasio")
@Getter
@Setter
@NoArgsConstructor
public class Gimnasio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", length = 150, nullable = false)
    private String nombre;

    @Column(name = "comuna", length = 80)
    private String comuna;

    @Column(name = "direccion", length = 200)
    private String direccion;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;
}
