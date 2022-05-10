package com.victoriametrics.metrics;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Counter metric.
 *
 * E.g. the metric allows to track number of processed requests.
 */
public class Counter implements Metric {

    private final AtomicLong value = new AtomicLong(0);
    private final String name;

    public Counter(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public void inc() {
        value.incrementAndGet();
    }

    public void inc(long value) {
        this.value.addAndGet(value);
    }

    public void dec() {
        value.decrementAndGet();
    }

    public void set(long value) {
        this.value.set(value);
    }

    public long get() {
        return value.get();
    }

}
