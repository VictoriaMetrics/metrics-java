package com.victoriametrics.metrics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HistogramTest {

    @Test
    public void test_update() {
        Histogram histogram = new Histogram("response_size");
        histogram.update(60);
        histogram.update(60);
        histogram.visit((vmrange, count) -> assertEquals(2, count));

        assertEquals(120.0, histogram.getSum());
    }

    @Test
    public void test_reset() {
        Histogram histogram = new Histogram("response_size");
        histogram.update(60);
        histogram.visit((vmrange, count) -> assertEquals(1, count));

        assertEquals(60.0, histogram.getSum());

        histogram.reset();

        assertEquals(00.0, histogram.getSum());
    }

    @Test
    public void test_getRange() {
        Histogram histogram = new Histogram("response_size");

        assertEquals("1.000e-09...1.136e-09", histogram.getRange(0));
        assertEquals("1.136e-09...1.292e-09", histogram.getRange(1));
        assertEquals("8.799e+17...1.000e+18", histogram.getRange(485));
    }

}