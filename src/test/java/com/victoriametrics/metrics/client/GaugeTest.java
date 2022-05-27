package com.victoriametrics.metrics.client;

import com.victoriametrics.client.metrics.Gauge;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.DoubleAdder;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GaugeTest {

    @Test
    public void callGaugeSupplierMultiple() {
        DoubleAdder value = new DoubleAdder();
        Gauge gauge = new Gauge("gauge", () -> {
            value.add(1.0);
            return value.doubleValue();
        });

        double actualValue = 0.0;
        for (int i = 0; i < 3; i++) {
            actualValue = gauge.get();
        }

        assertEquals(3.0, actualValue);
    }
}