package cl.nxiiio.reservafit.gimnasios.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.nxiiio.reservafit.gimnasios.model.Horario;

public interface ScheduleRepository extends JpaRepository<Horario, Long> {

    List<Horario> findByGimnasioIdOrderByHoraInicioAsc(Long gimnasioId);
}
