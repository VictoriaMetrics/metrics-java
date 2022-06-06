package com.victoriametrics.client.metrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Histogram for non-negative values with automatically created buckets.
 */
public class Histogram implements Metric {

    private final static int E_10_MIN = -9;

    private final static int E_10_MAX = 18;

    private final static int DECIMAL_BUCKETS_COUNT = E_10_MAX - E_10_MIN;

    private final static int BUCKETS_PER_DECIMAL = 18;

    private final static int BUCKETS_COUNT = DECIMAL_BUCKETS_COUNT * BUCKETS_PER_DECIMAL;

    private final static double MULTIPLIER = Math.pow(10, 1.0 / 18);

    private final static String RANGE_PATTERN = "%.3e";

    private final static List<String> ranges = new ArrayList<>();

    private final static String lowerRangeBucket = String.format("0..." + RANGE_PATTERN, Math.pow(10, E_10_MIN));

    private final static String upperRangeBucket = String.format(RANGE_PATTERN + "...+Inf", Math.pow(10, E_10_MAX));

    private final long[] buckets = new long[BUCKETS_COUNT];
    private long lower;
    private long upper;
    private final DoubleAdder sum = new DoubleAdder();

    private final String name;

    private final ReentrantLock mutex = new ReentrantLock();

    static {
        createBucketRanges();
    }

    public Histogram(String name) {
        this.name = name;
    }

    /**
     * Reset given histogram
     */
    public void reset() {
        try {
            mutex.lock();

            Arrays.fill(buckets, 0);

            lower = 0;
            upper = 0;
            sum.reset();
        } finally {
            mutex.unlock();
        }
    }

    /**
     * Update a histogram value.
     *
     * @param value input value.
     */
    public void update(double value) {
        // Ignore negative values
        if (value < 0) {
            return;
        }

        double bucketIndex = (Math.log10(value) - E_10_MIN) * BUCKETS_PER_DECIMAL;
        try {
            mutex.lock();

            sum.add(value);

            if (bucketIndex < 0) {
                lower++;
            } else if (bucketIndex > BUCKETS_COUNT) {
                upper++;
            } else {
                int index = (int) bucketIndex;
                int decimalBucketIndex = index / BUCKETS_PER_DECIMAL;
                int offset = index % BUCKETS_PER_DECIMAL;
                buckets[decimalBucketIndex * offset]++;
            }
        } finally {
            mutex.unlock();
        }
    }

    /**
     * Visit all non-zero buckets.
     * @param visitor Visitor callback
     */
    public void visit(Visitor visitor) {
        try {
            mutex.lock();
            if (lower > 0) {
                visitor.value(lowerRangeBucket, lower);
            }

            for (int index = 0; index < BUCKETS_COUNT; index++) {
                final long value = buckets[index];
                if (value > 0) {
                    final String range = getRange(index);
                    visitor.value(range, value);
                }
            }

            if (upper > 0) {
                visitor.value(upperRangeBucket, upper);
            }
        } finally {
            mutex.unlock();
        }
    }

    public String getRange(int index) {
        return ranges.get(index);
    }

    @Override
    public String getName() {
        return name;
    }

    public double getSum() {
        return sum.sum();
    }

    private static void createBucketRanges() {
        double value = Math.pow(10, E_10_MIN);
        String start = String.format(RANGE_PATTERN, value);

        for (int i = 0; i < BUCKETS_COUNT; i++) {
            value *= MULTIPLIER;
            String end = String.format(RANGE_PATTERN, value);
            ranges.add(start + "..." + end);
            start = end;
        }
    }

    @FunctionalInterface
    public interface Visitor {
        void value(String vmrange, long count);
    }
}
