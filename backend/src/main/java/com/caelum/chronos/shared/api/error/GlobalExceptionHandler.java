package com.caelum.chronos.shared.api.error;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.caelum.chronos.shared.exception.BusinessException;
import com.caelum.chronos.shared.exception.ConflictException;
import com.caelum.chronos.shared.exception.InvalidCredentialsException;
import com.caelum.chronos.shared.exception.NotFoundException;
import com.caelum.chronos.shared.infra.logging.LogContext;

/**
 * Classe responsável por interceptar e tratar exceções lançadas pelos
 * controladores da aplicação. Ela utiliza a anotação @RestControllerAdvice para
 * se registrar como um manipulador global de exceções, e define métodos
 * específicos para tratar diferentes tipos de exceções, retornando respostas
 * HTTP apropriadas com mensagens de erro detalhadas. O objetivo é fornecer uma
 * maneira centralizada e consistente de lidar com erros em toda a aplicação,
 * melhorando a experiência do usuário e facilitando a depuração. Cada método de
 * tratamento de exceção constrói um objeto ApiErrorResponse contendo
 * informações relevantes sobre o erro, como timestamp, status HTTP, mensagem de
 * erro, caminho da requisição e detalhes de validação, e retorna esse objeto
 * como resposta para o cliente.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        List<FieldErrorResponse> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toFieldError)
                .toList();

        return build(HttpStatus.BAD_REQUEST, "Dados inválidos", request, fieldErrors);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex,
            WebRequest request) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(BadCredentialsException ex, WebRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "Credenciais inválidas", request, List.of());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthentication(AuthenticationException ex, WebRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "Não autorizado", request, List.of());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        return build(HttpStatus.FORBIDDEN, "Acesso negado", request, List.of());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(NotFoundException ex, WebRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(ConflictException ex, WebRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex,
            WebRequest request) {
        return build(HttpStatus.CONFLICT, "Não foi possível concluir a operação", request, List.of());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusiness(BusinessException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFoundRoute(NoHandlerFoundException ex, WebRequest request) {
        return build(HttpStatus.NOT_FOUND, "Recurso não encontrado", request, List.of());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResourceFound(NoResourceFoundException ex, WebRequest request) {
        return build(HttpStatus.NOT_FOUND, "Recurso não encontrado", request, List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex, WebRequest request) {
        log.error("Erro interno não tratado interceptado pelo GlobalExceptionHandler:", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno", request, List.of());
    }

    private FieldErrorResponse toFieldError(FieldError error) {
        return new FieldErrorResponse(error.getField(), error.getDefaultMessage());
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String message, WebRequest request,
            List<FieldErrorResponse> fieldErrors) {
        String path = request instanceof ServletWebRequest sw ? sw.getRequest().getRequestURI() : null;

        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                LogContext.getOrCreateCorrelationId(
                        request instanceof ServletWebRequest sw ? sw.getRequest() : null),
                fieldErrors);

        return ResponseEntity.status(status).body(body);
    }
}