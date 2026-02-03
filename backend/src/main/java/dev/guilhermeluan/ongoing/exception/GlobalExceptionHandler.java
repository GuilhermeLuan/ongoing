package dev.guilhermeluan.ongoing.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<DefaultErrorMessage> handleNotFoundException(NotFoundException ex) {
        var error = new DefaultErrorMessage(
                HttpStatus.NOT_FOUND.value(),
                ex.getReason()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValidException(MethodArgumentNotValidException e,
                                                                          HttpServletRequest request) {
        String defaultMessage = e.getBindingResult()
                .getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.joining(", "));

        ApiError apiError = ApiError.builder()
                .timestamp(OffsetDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .path(request.getRequestURI())
                .message(defaultMessage)
                .build();

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<DefaultErrorMessage> handleBadRequestException(BadRequestException ex) {
        var error = new DefaultErrorMessage(
                HttpStatus.BAD_REQUEST.value(),
                ex.getReason()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(InvalidCredentialException.class)
    public ResponseEntity<DefaultErrorMessage> handleInvalidCredentialException(InvalidCredentialException ex) {
        var error = new DefaultErrorMessage(
                HttpStatus.UNAUTHORIZED.value(),
                ex.getReason()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<DefaultErrorMessage> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        var error = new DefaultErrorMessage(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid request body"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
