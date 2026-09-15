package cl.nxiiio.ReservaFit_Backend.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import cl.nxiiio.ReservaFit_Backend.exception.AlreadyCancelledException;
import cl.nxiiio.ReservaFit_Backend.exception.BookingAlreadyStartedException;
import cl.nxiiio.ReservaFit_Backend.exception.BookingNotFoundException;
import cl.nxiiio.ReservaFit_Backend.exception.GymNotFoundException;
import cl.nxiiio.ReservaFit_Backend.exception.InvalidBookingDateException;
import cl.nxiiio.ReservaFit_Backend.exception.RutAlreadyRegisteredException;
import cl.nxiiio.ReservaFit_Backend.exception.ScheduleNotFoundException;
import cl.nxiiio.ReservaFit_Backend.exception.SlotAlreadyBookedException;
import cl.nxiiio.ReservaFit_Backend.exception.UserNotFoundException;

// Error bodies are field -> message maps so the frontend can show each message next to its input.
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> errors = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return errors;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleUnreadableBody() {
        return Map.of("error", "Datos con formato inválido");
    }

    @ExceptionHandler(RutAlreadyRegisteredException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleDuplicateRut() {
        return Map.of("rut", "Este RUT ya está registrado");
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleUserNotFound() {
        return Map.of("error", "Usuario no registrado");
    }

    @ExceptionHandler(GymNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleGymNotFound() {
        return Map.of("error", "Gimnasio no encontrado");
    }

    // Missing or malformed query/path parameters, e.g. ?fecha=not-a-date
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleMissingParameter(MissingServletRequestParameterException e) {
        return Map.of(e.getParameterName(), "Este parámetro es obligatorio");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return Map.of(e.getName(), "Valor con formato inválido");
    }

    @ExceptionHandler(ScheduleNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleScheduleNotFound() {
        return Map.of("error", "Horario no encontrado");
    }

    @ExceptionHandler(BookingNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleBookingNotFound() {
        return Map.of("error", "Reserva no encontrada");
    }

    @ExceptionHandler(InvalidBookingDateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleInvalidBookingDate(InvalidBookingDateException e) {
        return Map.of(e.getField(), e.getMessage());
    }

    @ExceptionHandler(SlotAlreadyBookedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleSlotAlreadyBooked() {
        return Map.of("error", "Este horario ya está reservado para esa fecha");
    }

    @ExceptionHandler(AlreadyCancelledException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleAlreadyCancelled() {
        return Map.of("error", "Esta reserva ya fue cancelada");
    }

    @ExceptionHandler(BookingAlreadyStartedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleBookingAlreadyStarted() {
        return Map.of("error", "Esta reserva ya comenzó y no se puede cancelar");
    }
}
