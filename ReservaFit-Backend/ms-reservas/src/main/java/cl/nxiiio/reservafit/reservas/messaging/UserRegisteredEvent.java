package cl.nxiiio.reservafit.reservas.messaging;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Copy of the message class of the publishing service (same field names). Published by ms-usuarios on every login. It is a full snapshot of the user, not a diff,
// so processing it twice leaves the same result. RUT and card data never travel here.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisteredEvent {

    // ms-usuarios' own id: ms-reservas uses it as the id of its copy
    private Long userId;
    private String microsoftOid;
    private String name;
    private String email;
}
