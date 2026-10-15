package cl.nxiiio.reservafit.usuarios.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.nxiiio.reservafit.usuarios.model.Usuario;

public interface UserRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByMicrosoftOid(String microsoftOid);
}
