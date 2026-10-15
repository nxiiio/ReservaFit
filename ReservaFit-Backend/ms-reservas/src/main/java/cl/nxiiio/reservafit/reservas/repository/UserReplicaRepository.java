package cl.nxiiio.reservafit.reservas.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.nxiiio.reservafit.reservas.model.UsuarioReplica;

public interface UserReplicaRepository extends JpaRepository<UsuarioReplica, Long> {

    Optional<UsuarioReplica> findByMicrosoftOid(String microsoftOid);
}
