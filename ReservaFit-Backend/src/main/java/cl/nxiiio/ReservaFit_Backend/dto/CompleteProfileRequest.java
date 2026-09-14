package cl.nxiiio.ReservaFit_Backend.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompleteProfileRequest {

    @NotBlank(message = "Ingresa tu RUT")
    @Pattern(regexp = "^[0-9]{7,8}[0-9Kk]$", message = "RUT sin puntos ni guion, ej: 123456785")
    private String rut;

    @NotNull(message = "Ingresa tu fecha de nacimiento")
    @Past(message = "La fecha de nacimiento debe ser pasada")
    private LocalDate birthDate;

    @NotBlank(message = "Ingresa el número de tarjeta")
    @Pattern(regexp = "^[0-9]{13,19}$", message = "El número de tarjeta debe tener entre 13 y 19 dígitos")
    private String cardNumber;

    @NotBlank(message = "Ingresa el nombre del titular")
    @Size(max = 100, message = "Máximo 100 caracteres")
    private String cardHolderName;

    @NotBlank(message = "Ingresa el vencimiento")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/[0-9]{2}$", message = "Vencimiento en formato MM/AA")
    private String cardExpiry;
}
