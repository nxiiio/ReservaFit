package cl.nxiiio.reservafit.reservas.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cl.nxiiio.reservafit.reservas.model.Reserva;

public interface BookingRepository extends JpaRepository<Reserva, Long> {

    boolean existsByHorarioIdAndFechaAndActivaTrue(Long horarioId, LocalDate fecha);

    @Query("select r.horarioId from Reserva r "
            + "where r.gimnasioId = :gymId and r.fecha = :date and r.activa = true")
    List<Long> findActiveScheduleIds(@Param("gymId") Long gymId, @Param("date") LocalDate date);

    // A user's bookings ordered by date and then by start time, oldest first.
    // The start time lives in the schedule copy, so the query joins both tables by id.
    @Query("select r from Reserva r, HorarioReplica h "
            + "where r.horarioId = h.id and r.usuarioId = :userId "
            + "order by r.fecha asc, h.horaInicio asc")
    List<Reserva> findByUserOrderedByDateAndTime(@Param("userId") Long userId);
}
