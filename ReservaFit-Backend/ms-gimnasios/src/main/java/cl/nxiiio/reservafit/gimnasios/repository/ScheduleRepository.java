package cl.nxiiio.reservafit.gimnasios.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cl.nxiiio.reservafit.gimnasios.model.Horario;

public interface ScheduleRepository extends JpaRepository<Horario, Long> {

    List<Horario> findByGimnasioIdOrderByHoraInicioAsc(Long gimnasioId);

    // One schedule with its gym already loaded (Horario.gimnasio is LAZY)
    @Query("select h from Horario h join fetch h.gimnasio where h.id = :id")
    Optional<Horario> findByIdWithGym(@Param("id") Long id);
}
