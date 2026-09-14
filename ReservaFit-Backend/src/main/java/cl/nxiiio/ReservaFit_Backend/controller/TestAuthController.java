package cl.nxiiio.ReservaFit_Backend.controller;

import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@RestController
@RequestMapping("/api")
public class TestAuthController {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataDto {
        private LocalDateTime timestamp;
        private String message;
    }

    @GetMapping("/home")
    public ResponseEntity<DataDto> getData() {
        return ResponseEntity.ok(new DataDto(LocalDateTime.now(), "Hello, World!"));
    }
}
