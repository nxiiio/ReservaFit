package cl.nxiiio.ReservaFit_Backend.dto;

import cl.nxiiio.ReservaFit_Backend.model.Gimnasio;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GymResponse {

    private Long id;
    private String name;
    private String comuna;
    private String address;
    private String description;
    private String imageUrl;

    public static GymResponse from(Gimnasio gym) {
        return new GymResponse(
                gym.getId(),
                gym.getNombre(),
                gym.getComuna(),
                gym.getDireccion(),
                gym.getDescripcion(),
                gym.getImagenUrl());
    }
}
