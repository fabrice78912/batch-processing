package com.example.batch_processing.exception;

import com.example.batch_processing.domain.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import jakarta.validation.ConstraintViolationException;

import java.time.LocalDateTime;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Capture les erreurs de validation sur @RequestParam, @PathVariable
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Response> handleConstraintViolation(ConstraintViolationException ex) {

        // Tri des violations par nom de propriété pour rendre le message stable
        String message = ex.getConstraintViolations().stream()
                .sorted((cv1, cv2) -> cv1.getPropertyPath().toString()
                        .compareTo(cv2.getPropertyPath().toString()))
                .map(cv -> cv.getPropertyPath() + " : " + cv.getMessage())
                .reduce((m1, m2) -> m1 + "; " + m2)
                .orElse("Paramètres invalides");

        Response response = Response.builder()
                .time(LocalDateTime.now().toString())
                .code(HttpStatus.BAD_REQUEST.value())
                .status(HttpStatus.BAD_REQUEST)
                .message(message)
                .exception(ex.getClass().getSimpleName())
                .data(Map.of())
                .build();

        return ResponseEntity.badRequest().body(response);
    }
}