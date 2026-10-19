package cl.nxiiio.reservafit.usuarios.messaging;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Published by ms-usuarios on every login. ms-reservas has a copy of this class with the same
// fields: the message travels as JSON, so both sides only need to agree on the field names. It is a full snapshot of the user, not a diff,
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
