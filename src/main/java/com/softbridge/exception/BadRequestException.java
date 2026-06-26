package com.softbridge.exception;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) { super(message); }
}
