package cl.nxiiio.reservafit.gimnasios.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.nxiiio.reservafit.gimnasios.model.Gimnasio;

public interface GymRepository extends JpaRepository<Gimnasio, Long> {

    List<Gimnasio> findByComunaIgnoreCaseOrderByNombreAsc(String comuna);

    List<Gimnasio> findAllByOrderByNombreAsc();
}
