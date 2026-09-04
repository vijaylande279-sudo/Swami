package com.swamisuite.catalog.web;

import com.swamisuite.catalog.service.CatalogService.CatalogException;
import com.swamisuite.common.web.ApiError;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CatalogExceptionHandler {

    @ExceptionHandler(CatalogException.class)
    public ResponseEntity<ApiError> handleCatalogException(CatalogException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError("NOT_FOUND", ex.getMessage(), null, Instant.now()));
    }
}
