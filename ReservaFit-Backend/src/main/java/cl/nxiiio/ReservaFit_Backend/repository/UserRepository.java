package cl.nxiiio.ReservaFit_Backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.nxiiio.ReservaFit_Backend.model.Usuario;

public interface UserRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByMicrosoftOid(String microsoftOid);
}
