package cl.nxiiio.ReservaFit_Backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import cl.nxiiio.ReservaFit_Backend.dto.BookingResponse;
import cl.nxiiio.ReservaFit_Backend.dto.CreateBookingRequest;
import cl.nxiiio.ReservaFit_Backend.service.BookingService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/reservas")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse createBooking(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateBookingRequest request) {
        return bookingService.create(jwt.getClaimAsString("oid"), request);
    }

    @GetMapping("/mias")
    public List<BookingResponse> listMyBookings(@AuthenticationPrincipal Jwt jwt) {
        return bookingService.findMine(jwt.getClaimAsString("oid"));
    }

    // Soft cancel: the row stays so it shows up in the user's history
    @DeleteMapping("/{id}")
    public BookingResponse cancelBooking(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return bookingService.cancel(jwt.getClaimAsString("oid"), id);
    }
}
