package cl.nxiiio.reservafit.gimnasios.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import cl.nxiiio.reservafit.gimnasios.model.Horario;

public interface ScheduleRepository extends JpaRepository<Horario, Long> {

    List<Horario> findByGimnasioIdOrderByHoraInicioAsc(Long gimnasioId);

    // Every schedule with its gym already loaded (Horario.gimnasio is LAZY)
    @Query("select h from Horario h join fetch h.gimnasio")
    List<Horario> findAllWithGym();
}
