/*
 * Copyright (c) 2023 Victoria Metrics Inc.
 */

package io.victoriametrics.client.springboot;

import io.victoriametrics.client.metrics.MetricRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Valery Kantor
 */
@Configuration
public class VictoriaMetricsEndpointConfiguration {

    @Bean
    public VictoriaMetricsEndpoint victoriaMetricsEndpoint(MetricRegistry metricRegistry) {
        return new VictoriaMetricsEndpoint(metricRegistry);
    }

    @Bean
    public MetricRegistry metricRegistry() {
        return MetricRegistry.create();
    }
}
