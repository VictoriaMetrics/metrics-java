package com.victoriametrics.validator;

public class InvalidMetricNameException extends RuntimeException {

    public InvalidMetricNameException(String message) {
        super(message);
    }
}
