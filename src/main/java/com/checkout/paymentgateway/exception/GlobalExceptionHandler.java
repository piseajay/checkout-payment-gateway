package com.checkout.paymentgateway.exception;

import java.util.List;

import com.checkout.paymentgateway.model.entity.PaymentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationFailure(
            MethodArgumentNotValidException ex) {

        List<String> errors =
                ex.getBindingResult().getAllErrors().stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .toList();

        ProblemDetail problem = problemDetail(
                HttpStatus.BAD_REQUEST, "Payment request validation failed");
        problem.setProperty("status_description", PaymentStatus.REJECTED.getName());
        problem.setProperty("errors", errors);

        log.info("Payment rejected — validation errors: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ProblemDetail> handlePaymentNotFound(PaymentNotFoundException ex) {
        log.info("Payment not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler(BankUnavailableException.class)
    public ResponseEntity<ProblemDetail> handleBankUnavailable(BankUnavailableException ex) {
        log.warn("Acquiring Bank unavailable: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(problemDetail(HttpStatus.BAD_GATEWAY,
                ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(Exception ex) {
        log.error("Unexpected error : ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please contact support."));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleMalformedJson(HttpMessageNotReadableException ex) {

        log.warn("Malformed JSON in request body {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(problemDetail(HttpStatus.BAD_REQUEST, "Request body is malformed or not valid JSON"));
    }

    private ProblemDetail problemDetail(HttpStatus status, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(null);
        return problem;
    }
}
