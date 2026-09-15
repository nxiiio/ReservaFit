package cl.nxiiio.ReservaFit_Backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.nxiiio.ReservaFit_Backend.model.Horario;

public interface ScheduleRepository extends JpaRepository<Horario, Long> {

    List<Horario> findByGimnasioIdOrderByHoraInicioAsc(Long gimnasioId);
}
