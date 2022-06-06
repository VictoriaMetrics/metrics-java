package io.victoriametrics.client.metrics;

/**
 * Prometheus-compatible metric.
 *
 */
public interface Metric {

    String getName();

    void accept(MetricVisitor visitor);

}
