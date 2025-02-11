package com.alkl1m.bulkhead.exception;

public class BulkheadException extends RuntimeException {

    public BulkheadException(String message) {
        super(message);
    }

}
