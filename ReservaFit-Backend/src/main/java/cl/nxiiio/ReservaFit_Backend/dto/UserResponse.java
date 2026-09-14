package cl.nxiiio.ReservaFit_Backend.dto;

import java.time.LocalDateTime;

import cl.nxiiio.ReservaFit_Backend.model.Usuario;
import cl.nxiiio.ReservaFit_Backend.service.UserService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private boolean profileComplete;
    private LocalDateTime registeredAt;

    public static UserResponse from(Usuario user) {
        return new UserResponse(
                user.getId(),
                user.getNombre(),
                user.getEmail(),
                UserService.isProfileComplete(user),
                user.getFechaRegistro());
    }
}
