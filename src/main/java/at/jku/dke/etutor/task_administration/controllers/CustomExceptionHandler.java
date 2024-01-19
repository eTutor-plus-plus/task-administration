package at.jku.dke.etutor.task_administration.controllers;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import org.hibernate.JDBCException;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.sql.SQLException;
import java.util.Map;

/**
 * Controller advice for handling exceptions.
 */
@RestControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {
    /**
     * The base URL for problem details types.
     */
    protected static final String BASE_URL = "http://etutor.dke.uni-linz.ac.at/errors/";

    /**
     * Creates a new instance of class {@link CustomExceptionHandler}.
     */
    public CustomExceptionHandler() {
    }

    /**
     * Handle exceptions of type {@link BadCredentialsException}, {@link DisabledException} and {@link LockedException}.
     *
     * @param ex      The exception to handle.
     * @param request The current request.
     * @return The response entity.
     */
    @ExceptionHandler({BadCredentialsException.class, DisabledException.class, LockedException.class})
    public ResponseEntity<Object> handleBadCredentialsException(AuthenticationException ex, WebRequest request) {
        var status = HttpStatus.BAD_REQUEST;
        var body = this.createProblemDetail(ex, status, ex.getLocalizedMessage(), null, null, request);
        body.setType(URI.create(BASE_URL + "bad-credentials"));
        return handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    /**
     * Handle exceptions of type {@link InsufficientAuthenticationException}.
     *
     * @param ex      The exception to handle.
     * @param request The current request.
     * @return The response entity.
     */
    @ExceptionHandler({InsufficientAuthenticationException.class})
    public ResponseEntity<Object> handleInsufficientAuthenticationException(InsufficientAuthenticationException ex, WebRequest request) {
        var status = request.getUserPrincipal() == null ? HttpStatus.UNAUTHORIZED : HttpStatus.FORBIDDEN;
        var body = this.createProblemDetail(ex, status, ex.getLocalizedMessage(), null, null, request);
        body.setType(URI.create(BASE_URL + "access-denied"));
        return handleExceptionInternal(ex, body, new HttpHeaders(), status, request);
    }

    /**
     * Handle exceptions of type {@link AccessDeniedException}.
     *
     * @param ex      The exception to handle.
     * @param request The current request.
     * @return The response entity.
     */
    @ExceptionHandler({AccessDeniedException.class})
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        var status = HttpStatus.FORBIDDEN;
        var body = this.createProblemDetail(ex, status, ex.getLocalizedMessage(), null, null, request);
        body.setType(URI.create(BASE_URL + "access-denied"));
        return handleExceptionInternal(ex, body, new HttpHeaders(), status, request);
    }

    /**
     * Handle exceptions of type {@link ConcurrencyFailureException}.
     *
     * @param ex      The exception to handle.
     * @param request The current request.
     * @return The response entity.
     */
    @ExceptionHandler({ConcurrencyFailureException.class})
    public ResponseEntity<Object> handleConcurrencyFailureException(ConcurrencyFailureException ex, WebRequest request) {
        var status = HttpStatus.CONFLICT;
        var body = this.createProblemDetail(ex, status, ex.getLocalizedMessage(), null, null, request);
        body.setType(URI.create(BASE_URL + "concurrency-failure"));
        return handleExceptionInternal(ex, body, new HttpHeaders(), status, request);
    }

    /**
     * Handle exceptions of type {@link EntityNotFoundException}.
     *
     * @param ex      The exception to handle.
     * @param request The current request.
     * @return The response entity.
     */
    @ExceptionHandler({EntityNotFoundException.class})
    public ResponseEntity<Object> handleEntityNotFoundException(EntityNotFoundException ex, WebRequest request) {
        var status = HttpStatus.NOT_FOUND;
        var body = this.createProblemDetail(ex, status, ex.getLocalizedMessage(), null, null, request);
        body.setType(URI.create(BASE_URL + "not-found"));
        return handleExceptionInternal(ex, body, new HttpHeaders(), status, request);
    }

    /**
     * Handle exceptions of type {@link ConstraintViolationException}.
     *
     * @param ex      The exception to handle.
     * @param request The current request.
     * @return The response entity.
     */
    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex, WebRequest request) {
        var body = this.createProblemDetail(ex, HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), null, null, request);
        if (ex.getConstraintViolations() != null)
            body.setProperty("violations", ex.getConstraintViolations().stream().map(x -> Map.of(
                    "message", x.getMessage(),
                    "path", x.getPropertyPath().toString(),
                    "type", x.getConstraintDescriptor() != null ? x.getConstraintDescriptor().getAnnotation().annotationType().getName() : ""))
                .toList());
        body.setType(URI.create(BASE_URL + "validation-error"));
        return handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    /**
     * Handle exceptions of type {@link org.hibernate.exception.ConstraintViolationException}.
     *
     * @param ex      The exception to handle.
     * @param request The current request.
     * @return The response entity.
     */
    @ExceptionHandler({org.hibernate.exception.ConstraintViolationException.class})
    public ResponseEntity<Object> handleConstraintViolationException(org.hibernate.exception.ConstraintViolationException ex, WebRequest request) {
        var body = this.createProblemDetail(ex, HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), null, null, request);
        body.setProperty("constraint", ex.getConstraintName());
        body.setProperty("errorCode", ex.getErrorCode());
        body.setType(URI.create(BASE_URL + "validation-error/db-constraint"));
        return handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    /**
     * Handle exceptions of type {@link ValidationException}.
     *
     * @param ex      The exception to handle.
     * @param request The current request.
     * @return The response entity.
     */
    @ExceptionHandler({ValidationException.class})
    public ResponseEntity<Object> handleValidationException(ValidationException ex, WebRequest request) {
        String detail = ex.getLocalizedMessage();
        var body = this.createProblemDetail(ex, HttpStatus.BAD_REQUEST, detail, null, null, request);
        body.setType(URI.create(BASE_URL + "validation-error"));
        return handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    /**
     * Handle exceptions of type {@link DataIntegrityViolationException}.
     *
     * @param ex      The exception to handle.
     * @param request The current request.
     * @return The response entity.
     */
    @ExceptionHandler({DataIntegrityViolationException.class})
    public ResponseEntity<Object> handleDataIntegrityViolationException(DataIntegrityViolationException ex, WebRequest request) {
        String detail = ex.getLocalizedMessage();
        if (ex.getRootCause() instanceof SQLException sqex) {
            SQLExceptionTranslator t = new SQLErrorCodeSQLExceptionTranslator();
            var tmp = t.translate("Executing SQL Statement", null, sqex);
            if (tmp != null)
                detail = tmp.getLocalizedMessage();
        }

        var body = this.createProblemDetail(ex, HttpStatus.BAD_REQUEST, detail, null, null, request);
        body.setType(URI.create(BASE_URL + "validation-error/data-integrity"));
        return handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    /**
     * Handle exceptions of type {@link JDBCException}.
     *
     * @param ex      The exception to handle.
     * @param request The current request.
     * @return The response entity.
     */
    @ExceptionHandler({JDBCException.class})
    public ResponseEntity<Object> handleJDBCException(JDBCException ex, WebRequest request) {
        var body = this.createProblemDetail(ex, HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage(), null, null, request);
        body.setProperty("errorCode", ex.getErrorCode());
        body.setProperty("sqlState", ex.getSQLState());
        body.setType(URI.create(BASE_URL + "database-error"));
        return handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}
