package cl.nxiiio.reservafit.notificaciones.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.nxiiio.reservafit.notificaciones.model.Notificacion;
import cl.nxiiio.reservafit.notificaciones.model.TipoNotificacion;

public interface NotificationRepository extends JpaRepository<Notificacion, Long> {

    // Newest first
    List<Notificacion> findByUsuarioOidOrderByFechaCreacionDesc(String usuarioOid);

    List<Notificacion> findByUsuarioOidAndLeidaFalse(String usuarioOid);

    // Only the user's own: another user's notification looks like it does not exist
    Optional<Notificacion> findByIdAndUsuarioOid(Long id, String usuarioOid);

    boolean existsByReservaIdAndTipo(Long reservaId, TipoNotificacion tipo);
}
