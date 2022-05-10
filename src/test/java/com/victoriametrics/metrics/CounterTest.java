package com.victoriametrics.metrics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CounterTest {

    @Test
    void inc() {
        final String name = "requests_total{method=\"POST\", path=\"/foo/bar\"}";
        Counter counter = new Counter(name);

        counter.inc();
        assertEquals(1, counter.get());

        counter.inc();
        counter.inc();
        counter.inc();
        counter.inc();
        assertEquals(5, counter.get());

        String actualName = counter.getName();
        assertEquals(name, actualName);
    }

    @Test
    void dec() {
        final String name = "requests_total{method=\"POST\", path=\"/foo/bar\"}";

        Counter counter = new Counter(name);

        counter.set(10);
        counter.dec();
        assertEquals(9, counter.get());
    }

    @Test
    void add() {
        final String name = "requests_total{method=\"POST\", path=\"/foo/bar\"}";
        Counter counter = new Counter(name);

        counter.inc(10);
        assertEquals(10, counter.get());
    }
}