package cl.nxiiio.reservafit.gimnasios.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.nxiiio.reservafit.gimnasios.model.ReservaGimnasio;

public interface GymBookingRepository extends JpaRepository<ReservaGimnasio, Long> {

    long countByGimnasioIdAndEstado(Long gimnasioId, String estado);
}
