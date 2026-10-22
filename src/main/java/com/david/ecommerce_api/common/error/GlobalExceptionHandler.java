package com.david.ecommerce_api.common.error;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiErrorResponse> handleDomainException(
            DomainException exception,
            HttpServletRequest request,
            Locale locale
    ) {
        ErrorCode errorCode = exception.getErrorCode();

        String message = messageSource.getMessage(
                errorCode.messageKey(),
                exception.getArgs(),
                errorCode.name(),
                locale
        );

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                errorCode.httpStatus().value(),
                errorCode.httpStatus().getReasonPhrase(),
                errorCode.name(),
                message,
                request.getRequestURI(),
                List.of()
        );

        return ResponseEntity
                .status(errorCode.httpStatus())
                .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request,
            Locale locale
    ) {
        List<ApiErrorResponse.FieldErrorResponse> fields = exception
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ApiErrorResponse.FieldErrorResponse(
                        error.getField(),
                        error.getDefaultMessage()
                ))
                .toList();

        String message = messageSource.getMessage(
                ErrorCode.VALIDATION_ERROR.messageKey(),
                null,
                ErrorCode.VALIDATION_ERROR.name(),
                locale
        );

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                ErrorCode.VALIDATION_ERROR.httpStatus().value(),
                ErrorCode.VALIDATION_ERROR.httpStatus().getReasonPhrase(),
                ErrorCode.VALIDATION_ERROR.name(),
                message,
                request.getRequestURI(),
                fields
        );

        return ResponseEntity
                .status(ErrorCode.VALIDATION_ERROR.httpStatus())
                .body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataConflict(
            DataIntegrityViolationException exception,
            HttpServletRequest request,
            Locale locale
    ) {
        return respond(ErrorCode.DATA_CONFLICT, request, locale, List.of());
    }

    @ExceptionHandler({
            ConstraintViolationException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiErrorResponse> handleBadRequest(
            Exception exception,
            HttpServletRequest request,
            Locale locale
    ) {
        return respond(ErrorCode.VALIDATION_ERROR, request, locale, List.of());
    }

    private ResponseEntity<ApiErrorResponse> respond(
            ErrorCode errorCode,
            HttpServletRequest request,
            Locale locale,
            List<ApiErrorResponse.FieldErrorResponse> fields
    ) {
        String message = messageSource.getMessage(errorCode.messageKey(), null, errorCode.name(), locale);

        return ResponseEntity
                .status(errorCode.httpStatus())
                .body(new ApiErrorResponse(
                        Instant.now(),
                        errorCode.httpStatus().value(),
                        errorCode.httpStatus().getReasonPhrase(),
                        errorCode.name(),
                        message,
                        request.getRequestURI(),
                        fields
                ));
    }
}
