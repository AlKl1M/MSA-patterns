package com.alkl1m.circuitbreaker.exception;

public class CircuitBreakerOpenException extends RuntimeException {

    public CircuitBreakerOpenException(String message) {
        super(message);
    }

}
