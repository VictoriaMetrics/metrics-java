/*
 * Copyright (c) 2022 Victoria Metrics Inc.
 */

package io.victoriametrics.client.metrics;

/**
 * @author Valery Kantor
 */
public interface MetricVisitor {

    void visit(Counter counter);

    void visit(Gauge gauge);

    void visit(Histogram histogram);

    void visit(Summary summary);
}
