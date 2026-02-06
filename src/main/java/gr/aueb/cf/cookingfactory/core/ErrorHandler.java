package gr.aueb.cf.cookingfactory.core;

import gr.aueb.cf.cookingfactory.core.exceptions.*;
import gr.aueb.cf.cookingfactory.dto.ResponseMessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class ErrorHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(ValidationException e) {
        log.warn("validation failed. Message={}", e.getMessage());
        BindingResult bindingResult = e.getBindingResult();

        Map<String , String > errors = new HashMap<>();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AppObjectNotFoundException.class)
    public ResponseEntity<ResponseMessageDTO> handleConstraintViolationException(AppObjectNotFoundException e) {
        log.warn("Entity not found. Message={}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ResponseMessageDTO(e.getCode(), e.getMessage()));
    }


    @ExceptionHandler(AppObjectInvalidArgumentException.class)
    public ResponseEntity<ResponseMessageDTO> handleConstraintViolationException(AppObjectInvalidArgumentException e) {
        log.warn("Invalid Argument. Message={}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ResponseMessageDTO(e.getCode(), e.getMessage()));
    }


    @ExceptionHandler(AppObjectAlreadyExistsException.class)
    public ResponseEntity<ResponseMessageDTO> handleConstraintViolationException(AppObjectAlreadyExistsException e) {
        log.warn("Entity already exists. Message={}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ResponseMessageDTO(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(AppObjectNotAuthorizedException.class)
    public ResponseEntity<ResponseMessageDTO> handleConstraintViolationException(AppObjectNotAuthorizedException e, WebRequest request) {
        log.warn("Authorization failed for URI={}. Message={}", request.getDescription(false), e.getMessage()); // uri=/api/user/...
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)   // 403
                .body(new ResponseMessageDTO(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(AppServerException.class)
    public ResponseEntity<ResponseMessageDTO> handleConstraintViolationException(AppServerException e) {
        log.warn("Server error with message={}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseMessageDTO(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)    // 403
    public ResponseEntity<ResponseMessageDTO> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ResponseMessageDTO("ACCESS_DENIED", e.getMessage()));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ResponseMessageDTO> handleDatabaseErrors(DataAccessException e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseMessageDTO("DATABASE_ERROR", e.getMessage()));
    }
}
