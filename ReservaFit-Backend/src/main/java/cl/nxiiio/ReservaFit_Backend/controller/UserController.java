package cl.nxiiio.ReservaFit_Backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.nxiiio.ReservaFit_Backend.dto.CompleteProfileRequest;
import cl.nxiiio.ReservaFit_Backend.dto.UserResponse;
import cl.nxiiio.ReservaFit_Backend.service.UserService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/me")
    public ResponseEntity<UserResponse> registerCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        UserService.RegistrationResult result = userService.getOrCreate(jwt);
        HttpStatus status = result.isCreated() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(UserResponse.from(result.getUser()));
    }

    @PutMapping("/me/profile")
    public UserResponse completeProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CompleteProfileRequest request) {
        return UserResponse.from(userService.completeProfile(jwt, request));
    }
}
