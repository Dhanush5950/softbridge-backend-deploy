package com.softbridge.exception;

// ── ResourceNotFoundException ──
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) { super(message); }
}
