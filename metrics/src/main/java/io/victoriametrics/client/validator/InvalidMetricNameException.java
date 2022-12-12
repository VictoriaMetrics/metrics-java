package io.victoriametrics.client.validator;

public class InvalidMetricNameException extends RuntimeException {

    public InvalidMetricNameException(String message) {
        super(message);
    }
}
