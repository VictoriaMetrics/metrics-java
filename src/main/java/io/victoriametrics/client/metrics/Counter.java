package io.victoriametrics.client.metrics;

import java.util.concurrent.atomic.LongAdder;

/**
 * Counter metric.
 *
 * E.g. the metric allows to track number of processed requests.
 */
public class Counter implements Metric {

    private final LongAdder value = new LongAdder();
    private final String name;

    public Counter(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Increment counter.
     */
    public void inc() {
        this.value.increment();
    }

    /**
     * Increment counter by {@code value}
     * @param value The value by which counter will be increased
     */
    public void inc(long value) {
        this.value.add(value);
    }

    /**
     * Increment counter by {@code value}
     */
    public void dec() {
        this.value.decrement();
    }

    /**
     * Decrement counter by {@code value}
     * @param value The value by which counter will be decreased
     */

    public void dec(long value) {
        this.value.add(-value);
    }

    public long get() {
        return value.sum();
    }

}
