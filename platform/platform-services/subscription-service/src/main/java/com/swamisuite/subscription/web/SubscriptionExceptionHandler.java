package com.swamisuite.subscription.web;

import com.swamisuite.common.web.ApiError;
import com.swamisuite.subscription.service.SubscriptionService.SubscriptionException;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class SubscriptionExceptionHandler {

    @ExceptionHandler(SubscriptionException.class)
    public ResponseEntity<ApiError> handleSubscriptionException(SubscriptionException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiError("SUBSCRIPTION_ERROR", ex.getMessage(), null, Instant.now()));
    }
}
