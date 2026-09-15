package cl.nxiiio.ReservaFit_Backend.service;

import java.util.Locale;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import cl.nxiiio.ReservaFit_Backend.dto.CompleteProfileRequest;
import cl.nxiiio.ReservaFit_Backend.exception.RutAlreadyRegisteredException;
import cl.nxiiio.ReservaFit_Backend.exception.UserNotFoundException;
import cl.nxiiio.ReservaFit_Backend.model.Usuario;
import cl.nxiiio.ReservaFit_Backend.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Service
public class UserService {

    @Getter
    @AllArgsConstructor
    public static class RegistrationResult {
        private final Usuario user;
        private final boolean created;
    }

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    // Not @Transactional on purpose: saveAndFlush runs in its own transaction, so a
    // unique-constraint violation from a concurrent first login leaves us free to re-read.
    public RegistrationResult getOrCreate(Jwt jwt) {
        String oid = jwt.getClaimAsString("oid");
        return repository.findByMicrosoftOid(oid)
                .map(user -> new RegistrationResult(user, false))
                .orElseGet(() -> create(jwt, oid));
    }

    private RegistrationResult create(Jwt jwt, String oid) {
        Usuario user = new Usuario();
        user.setMicrosoftOid(oid);
        user.setNombre(jwt.getClaimAsString("name"));
        user.setEmail(jwt.getClaimAsString("preferred_username"));
        try {
            return new RegistrationResult(repository.saveAndFlush(user), true);
        } catch (DataIntegrityViolationException e) {
            Usuario existing = repository.findByMicrosoftOid(oid).orElseThrow(() -> e);
            return new RegistrationResult(existing, false);
        }
    }

    public Usuario findByOid(String oid) {
        return repository.findByMicrosoftOid(oid).orElseThrow(UserNotFoundException::new);
    }

    public Usuario completeProfile(Jwt jwt, CompleteProfileRequest request) {
        Usuario user = findByOid(jwt.getClaimAsString("oid"));

        user.setRut(request.getRut().toUpperCase(Locale.ROOT));
        user.setFechaNacimiento(request.getBirthDate());
        user.setNumTarjeta(request.getCardNumber());
        user.setNombreTarjeta(request.getCardHolderName().trim());
        user.setFechaExpTarjeta(request.getCardExpiry());
        try {
            return repository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            throw new RutAlreadyRegisteredException();
        }
    }

    public static boolean isProfileComplete(Usuario user) {
        return user.getRut() != null
                && user.getFechaNacimiento() != null
                && user.getNumTarjeta() != null
                && user.getNombreTarjeta() != null
                && user.getFechaExpTarjeta() != null;
    }
}
