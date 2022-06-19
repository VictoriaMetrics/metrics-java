/*
 * Copyright (c) 2022 Victoria Metrics Inc.
 */

package io.victoriametrics.client.metrics;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;

/**
 *  {@link Summary} can be used to monitor distributions like latancies or request size.
 * @author Valery Kantor
 */
public class Summary implements Metric {

    public final static double[] DEFAULT_QUANTILES = {0.5, 0.9, 0.97, 0.99, 1.0};

    public final static long DEFAULT_WINDOW_DURATION_SECONDS = TimeUnit.MINUTES.toSeconds(5);

    public final static int DEFAULT_WINDOWS_COUNT = 2;

    private final String name;

    private final double[] quantiles;

    private final LongAdder count = new LongAdder();

    private final DoubleAdder sum = new DoubleAdder();

    private final TimeWindowQuantile timeWindowQuantile;

    public Summary(String name) {
        this(name, DEFAULT_QUANTILES, DEFAULT_WINDOWS_COUNT, DEFAULT_WINDOW_DURATION_SECONDS);
    }

    public Summary(String name, double[] quantiles, int windows, long windowDurationSeconds) {
        this.name = name;
        validateQuantiles(quantiles);
        this.quantiles = quantiles;
        this.timeWindowQuantile = new TimeWindowQuantile(windowDurationSeconds, windows);
    }

    private void validateQuantiles(double[] quantiles) {
        for (double quantile : quantiles) {
            if (quantile < 0 || quantile > 1 ) throw new IllegalArgumentException("Quantile must be in range 0 and 1");
        }
    }

    public void update(double value) {
        sum.add(value);
        count.increment();
        timeWindowQuantile.insert(value);
    }

    public double getSum() {
        return sum.doubleValue();
    }

    public long getCount() {
        return count.longValue();
    }

    /**
     * Get the estimated value at the specified quantile.
     * @param quantile Requested quantile
     */
    public double getQuantil(double quantile) {
        return timeWindowQuantile.get(quantile);
    }

    /**
     * Get estimated values by configured quantiles.
     */
    public SortedMap<Double, Double> getQuantilValues() {
        SortedMap<Double, Double> result = new TreeMap<>();
        for (double quantile : quantiles) {
            result.put(quantile, timeWindowQuantile.get(quantile));
        }

        return result;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void accept(MetricVisitor visitor) {
        visitor.visit(this);
    }

    private static class TimeWindowQuantile {
        private final TimeWindow[] timeWindow;

        private final long rotationDurationMillis;

        private long lastRotationTimestamp;

        private int currentWindow = 0;

        private TimeWindowQuantile(long windowDurationSeconds, int timeWindows) {
            this.timeWindow = new TimeWindow[timeWindows];
            for (int i = 0; i < timeWindows; i++) {
                timeWindow[i] = new TimeWindow();
            }

            this.rotationDurationMillis = TimeUnit.SECONDS.toMillis(windowDurationSeconds) / timeWindows;
        }

        public synchronized void insert(double value) {
            rotate();
            for (TimeWindow timeFrame : timeWindow) {
                timeFrame.insert(value);
            }
        }

        public synchronized double get(double phi) {
            TimeWindow window = rotate();
            return window.get(phi);
        }

        public TimeWindow rotate() {
            long elapsedFromLastRotation = System.currentTimeMillis() - lastRotationTimestamp;

            while(elapsedFromLastRotation > rotationDurationMillis) {
                timeWindow[currentWindow] = new TimeWindow();
                if (++currentWindow >= timeWindow.length) {
                    currentWindow = 0;
                }

                elapsedFromLastRotation -= rotationDurationMillis;
                lastRotationTimestamp += rotationDurationMillis;
            }

            return timeWindow[currentWindow];
        }
    }

    private static class TimeWindow {

        private final List<Double> samples = new ArrayList<>();

        public void insert(double value) {
           samples.add(value);
        }

        public double get(double phi) {
            if (samples.size() == 0) {
                return Double.NaN;
            }

            List<Double> temp = new ArrayList<>(samples);
            temp.sort(Comparator.naturalOrder());

            if (phi <= 0.0) {
                return temp.get(0);
            }

            if (phi >= 1.0) {
                return temp.get(temp.size() - 1);
            }

            int rank = (int) Math.ceil(phi * temp.size());
            return temp.get(rank);
        }
    }
}
