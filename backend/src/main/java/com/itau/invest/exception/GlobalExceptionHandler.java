package com.itau.invest.exception;

import com.itau.invest.dto.common.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

/**
 * Tratamento global e centralizado de excecoes, convertendo-as em respostas
 * {@link ApiError} consistentes com o status HTTP adequado.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex,
                                                     HttpServletRequest request) {
        List<ApiError.FieldErrorItem> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldError)
                .toList();

        ApiError error = ApiError.of(HttpStatus.BAD_REQUEST.value(), "Bad Request",
                "Erro de validacao nos campos enviados", request.getRequestURI(), fieldErrors);
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                       HttpServletRequest request) {
        ApiError error = ApiError.of(HttpStatus.BAD_REQUEST.value(), "Bad Request",
                "Parametro invalido: " + ex.getName(), request.getRequestURI());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex,
                                                   HttpServletRequest request) {
        ApiError error = ApiError.of(HttpStatus.NOT_FOUND.value(), "Not Found",
                ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiError> handleDuplicate(DuplicateResourceException ex,
                                                    HttpServletRequest request) {
        ApiError error = ApiError.of(HttpStatus.CONFLICT.value(), "Conflict",
                ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ApiError> handleInsufficientBalance(InsufficientBalanceException ex,
                                                              HttpServletRequest request) {
        ApiError error = ApiError.of(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Unprocessable Entity",
                ex.getMessage(), request.getRequestURI());
        return ResponseEntity.unprocessableEntity().body(error);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusiness(BusinessException ex,
                                                   HttpServletRequest request) {
        ApiError error = ApiError.of(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Unprocessable Entity",
                ex.getMessage(), request.getRequestURI());
        return ResponseEntity.unprocessableEntity().body(error);
    }

    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<ApiError> handleBadCredentials(RuntimeException ex,
                                                         HttpServletRequest request) {
        ApiError error = ApiError.of(HttpStatus.UNAUTHORIZED.value(), "Unauthorized",
                "Credenciais invalidas", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiError> handleOptimisticLock(ObjectOptimisticLockingFailureException ex,
                                                         HttpServletRequest request) {
        ApiError error = ApiError.of(HttpStatus.CONFLICT.value(), "Conflict",
                "Conflito de concorrencia. Tente novamente.", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Erro nao tratado na requisicao {}", request.getRequestURI(), ex);
        ApiError error = ApiError.of(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "Ocorreu um erro inesperado. Contate o suporte.", request.getRequestURI());
        return ResponseEntity.internalServerError().body(error);
    }

    private ApiError.FieldErrorItem toFieldError(FieldError fieldError) {
        return new ApiError.FieldErrorItem(fieldError.getField(), fieldError.getDefaultMessage());
    }
}
