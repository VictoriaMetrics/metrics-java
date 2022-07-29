/*
 * Copyright (c) 2022 Victoria Metrics Inc.
 */

package io.victoriametrics.client.metrics;

import org.junit.jupiter.api.Test;

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

}
