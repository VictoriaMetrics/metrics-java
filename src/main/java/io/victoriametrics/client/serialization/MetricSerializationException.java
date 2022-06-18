/*
 * Copyright (c) 2022 Victoria Metrics Inc.
 */

package io.victoriametrics.client.serialization;

/**
 * @author Valery Kantor
 */
public class MetricSerializationException extends RuntimeException {

    public MetricSerializationException(String message) {
        super(message);
    }

    public MetricSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
