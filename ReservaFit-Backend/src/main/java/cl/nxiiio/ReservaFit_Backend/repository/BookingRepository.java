package cl.nxiiio.ReservaFit_Backend.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cl.nxiiio.ReservaFit_Backend.model.Reserva;

public interface BookingRepository extends JpaRepository<Reserva, Long> {

    boolean existsByHorarioIdAndFechaAndActivaTrue(Long horarioId, LocalDate fecha);

    @Query("select r.horario.id from Reserva r "
            + "where r.gimnasio.id = :gymId and r.fecha = :date and r.activa = true")
    List<Long> findActiveScheduleIds(@Param("gymId") Long gymId, @Param("date") LocalDate date);

    @Query("select r from Reserva r join fetch r.gimnasio join fetch r.horario "
            + "where r.usuario.id = :userId")
    List<Reserva> findAllWithDetailsByUserId(@Param("userId") Long userId);

    @Query("select r from Reserva r join fetch r.gimnasio join fetch r.horario join fetch r.usuario "
            + "where r.id = :id")
    Optional<Reserva> findWithDetailsById(@Param("id") Long id);
}
