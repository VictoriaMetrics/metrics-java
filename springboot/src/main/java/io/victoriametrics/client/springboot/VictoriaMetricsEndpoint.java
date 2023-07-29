/*
 * Copyright (c) 2023 Victoria Metrics Inc.
 */

package io.victoriametrics.client.springboot;

import io.victoriametrics.client.metrics.MetricRegistry;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.io.StringWriter;


/**
 * @Endpoint to expose metrics.
 *
 * @author Valery Kantor
 */
@Endpoint(id = "victoriametrics")
public class VictoriaMetricsEndpoint {

    private final MetricRegistry metricRegistry;

    public VictoriaMetricsEndpoint(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    @ReadOperation
    public String metrics() {
        StringWriter writer = new StringWriter();
        metricRegistry.write(writer);
        return writer.toString();
    }
}
