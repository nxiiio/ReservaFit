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

    // Oldest first. Same-day bookings are ordered by start time in BookingService,
    // because start times live in ms-gimnasios.
    List<Reserva> findByUsuarioOidOrderByFechaAsc(String usuarioOid);
}
