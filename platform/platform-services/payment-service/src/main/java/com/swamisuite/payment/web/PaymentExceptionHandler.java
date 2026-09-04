package com.swamisuite.payment.web;

import com.swamisuite.common.web.ApiError;
import com.swamisuite.payment.service.PaymentService.PaymentException;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PaymentExceptionHandler {

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ApiError> handlePaymentException(PaymentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiError("PAYMENT_ERROR", ex.getMessage(), null, Instant.now()));
    }
}
