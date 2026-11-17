package com.example.primeiro_projeto.controllers;

import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<?> regra(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("erro", ex.getReason() == null ? "Não foi possível concluir a operação." : ex.getReason()));
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<?> conflito(DataIntegrityViolationException ex) {
        return ResponseEntity.status(409).body(Map.of("erro", "O registro já existe ou está vinculado a outros dados. Remova os vínculos antes de excluir."));
    }
    @ExceptionHandler({HttpMessageNotReadableException.class, org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class})
    ResponseEntity<?> dadosInvalidos(Exception ex) {
        return ResponseEntity.badRequest().body(Map.of("erro", "Confira os campos enviados e os números informados."));
    }
}
