/*
 * Copyright (c) 2022 Victoria Metrics Inc.
 */

package io.victoriametrics.client.metrics;

import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Valery Kantor
 */
public class SummaryTest {

    @Test
    public void testSummarySerial() {
        final Summary summary = MetricCollection.create().getOrCreateSummary("TestSerial");

        int total = 2000;
        double sum = 0;
        for (int i = 0; i < total; i++) {
            summary.update(i);
            sum += i;
        }

        assertEquals(total, summary.getCount());
        assertEquals(sum, summary.getSum());

        assertEquals(total * 0.5, summary.getQuantile(0.5));
        assertEquals(total * 0.9, summary.getQuantile(0.9));
        assertEquals(total * 0.99, summary.getQuantile(0.99));
        assertEquals(total * 1.0 - 1, summary.getQuantile(1.0));
    }

    @Test
    public void testSummarySmallWindow() throws InterruptedException {
        double[] quantiles = new double[] {0.1, 0.2, 0.3};
        long windowDurationSeconds = 5;
        MetricCollection collection = MetricCollection.create();
        final Summary summary = collection.getOrCreateSummary("SmallWindow", quantiles, 2, windowDurationSeconds);

        for (int i = 0; i < 10000; i++) {
            summary.update(123);
        }

        // Wait for window update and verify that the summary has been cleared.
        Thread.sleep(2 * TimeUnit.SECONDS.toMillis(windowDurationSeconds));
        assertEquals(Double.NaN, summary.getQuantile(0.1));
        assertEquals(Double.NaN, summary.getQuantile(0.2));
        assertEquals(Double.NaN, summary.getQuantile(0.3));
    }
}
