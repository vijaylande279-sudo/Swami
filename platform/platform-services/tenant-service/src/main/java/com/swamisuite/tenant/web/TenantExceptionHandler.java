package com.swamisuite.tenant.web;

import com.swamisuite.common.web.ApiError;
import com.swamisuite.tenant.service.TenantService.TenantException;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TenantExceptionHandler {

    @ExceptionHandler(TenantException.class)
    public ResponseEntity<ApiError> handleTenantException(TenantException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError("NOT_FOUND", ex.getMessage(), null, Instant.now()));
    }
}
