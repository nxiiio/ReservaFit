package cl.nxiiio.ReservaFit_Backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.nxiiio.ReservaFit_Backend.model.Gimnasio;

public interface GymRepository extends JpaRepository<Gimnasio, Long> {

    List<Gimnasio> findByComunaIgnoreCaseOrderByNombreAsc(String comuna);

    List<Gimnasio> findAllByOrderByNombreAsc();
}
