package cl.nxiiio.reservafit.notificaciones.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import cl.nxiiio.reservafit.notificaciones.dto.NotificationResponse;
import cl.nxiiio.reservafit.notificaciones.service.NotificationService;
import lombok.RequiredArgsConstructor;

// The current user's notifications; the page's bell polls GET every 30 s.
// Marking as read uses PUT (setting a flag twice changes nothing), which CORS already allows.
@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public List<NotificationResponse> listMine(@AuthenticationPrincipal Jwt jwt) {
        return notificationService.findMine(jwt.getClaimAsString("oid"));
    }

    @PutMapping("/{id}/leida")
    public NotificationResponse markRead(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return notificationService.markRead(jwt.getClaimAsString("oid"), id);
    }

    @PutMapping("/leidas")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAllRead(@AuthenticationPrincipal Jwt jwt) {
        notificationService.markAllRead(jwt.getClaimAsString("oid"));
    }
}
