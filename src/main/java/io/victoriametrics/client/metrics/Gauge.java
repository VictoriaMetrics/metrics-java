package io.victoriametrics.client.metrics;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Gauge is double gauge metric.
 */
public class Gauge implements Metric {

    private final String name;
    private final Supplier<Double> supplier;

    public Gauge(String name, Supplier<Double> supplier) {
        this.name = name;
        this.supplier = Objects.requireNonNull(supplier);
    }

    public double get() {
        return supplier.get();
    }

    @Override
    public String getName() {
        return name;
    }
}
