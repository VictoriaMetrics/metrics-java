/*
 * Copyright (c) 2023 Victoria Metrics Inc.
 */

package io.victoriametrics.client.springboot;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author Valery Kantor
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(VictoriaMetricsEndpointConfiguration.class)
public @interface EnableVictoriaMetricsEndpoint {
}
