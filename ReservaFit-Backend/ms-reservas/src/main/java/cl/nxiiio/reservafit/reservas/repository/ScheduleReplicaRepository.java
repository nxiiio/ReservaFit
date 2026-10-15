package cl.nxiiio.reservafit.reservas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.nxiiio.reservafit.reservas.model.HorarioReplica;

public interface ScheduleReplicaRepository extends JpaRepository<HorarioReplica, Long> {

    List<HorarioReplica> findByGimnasioIdOrderByHoraInicioAsc(Long gimnasioId);
}
