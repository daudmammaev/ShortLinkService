package com.ShortLink.exceptions;

import com.ShortLink.exceptions.dto.ApiError;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@ControllerAdvice
@RestController
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ShortLinkNotFoundException.class)
    public ResponseEntity<ApiError> handleShortLinkNotFound(ShortLinkNotFoundException ex) {
        ApiError error = new ApiError(HttpStatus.NOT_FOUND, ex.getMessage());
        logger.warn("Ссылка не найдена: {}", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ShortLinkCreationException.class)
    public ResponseEntity<ApiError> handleShortLinkCreationException(ShortLinkCreationException ex) {
        ApiError error = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage());
        logger.error("Ошибка создания ссылки: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({DataIntegrityViolationException.class, ConstraintViolationException.class})
    public ResponseEntity<ApiError> handleDataIntegrityViolation(Exception ex) {
        String message = "Ошибка целостности данных. Возможно, такая короткая ссылка уже существует";
        ApiError error = new ApiError(HttpStatus.CONFLICT, message);
        logger.error("Ошибка целостности данных: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        ApiError error = new ApiError(HttpStatus.BAD_REQUEST, message);
        logger.warn("Ошибка валидации: {}", message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAllExceptions(Exception ex) {
        ApiError error = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR,
                "Внутренняя ошибка сервера");
        logger.error("Необработанное исключение: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}