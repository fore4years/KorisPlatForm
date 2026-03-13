package com.generator.rental.exception;

import com.generator.rental.common.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public Result<String> handleRuntimeException(RuntimeException ex) {
        // Determine status code based on message (Simple approach for now)
        if (ex.getMessage().contains("User not found") || ex.getMessage().contains("Invalid password")) {
            return Result.error(401, ex.getMessage());
        } else if (ex.getMessage().contains("already exists")) {
            return Result.error(409, ex.getMessage());
        }
        
        return Result.error(400, ex.getMessage());
    }
    
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception ex) {
        return Result.error(500, "Internal Server Error: " + ex.getMessage());
    }
}
