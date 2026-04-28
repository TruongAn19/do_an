package com.example.quanly.exception;

public class BusinessConflictException extends RuntimeException {
    public BusinessConflictException(String message) {
        super(message);
    }
}
