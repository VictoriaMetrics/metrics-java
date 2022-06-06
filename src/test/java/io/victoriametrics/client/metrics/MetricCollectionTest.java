package io.victoriametrics.client.metrics;

import com.victoriametrics.validator.InvalidMetricNameException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MetricCollectionTest {

    @Test
    public void createCounterMetricWithBuilder() {
        MetricCollection collection = MetricCollection.create();
        Counter counterNoLabels = collection.createCounter()
                .name("foo")
                .then()
                .register();

        assertEquals("foo", counterNoLabels.getName());

        Counter counterWithLabels = collection.createCounter()
                .name("foo")
                .addLabel("bar", "value1")
                .then()
                .register();

        assertEquals("foo{bar=\"value1\"}", counterWithLabels.getName());
    }

    @Test
    public void createGaugeMetricWithBuilder() {
        MetricCollection collection = MetricCollection.create();
        Gauge gaugeNoLabels = collection.createGauge()
                .withSupplier(() -> 1.0)
                .name("foo")
                .then()
                .register();

        assertEquals("foo", gaugeNoLabels.getName());

        Gauge gaugeWithLabels = collection.createGauge()
                .withSupplier(() -> 1.0)
                .name("foo")
                .addLabel("bar", "value1")
                .then()
                .register();

        assertEquals("foo{bar=\"value1\"}", gaugeWithLabels.getName());
    }

    @Test
    public void createHistogramMetricWithBuilder() {
        MetricCollection collection = MetricCollection.create();
        Histogram histogramNoLabels = collection.createHistogram()
                .name("foo")
                .then()
                .register();

        assertEquals("foo", histogramNoLabels.getName());

        Histogram histogramWithLabels = collection.createHistogram()
                .name("foo")
                .addLabel("bar", "value1")
                .then()
                .register();

        assertEquals("foo{bar=\"value1\"}", histogramWithLabels.getName());
    }

    @Test
    public void createMetricsByName() {
        MetricCollection collection = MetricCollection.create();
        Counter counter = collection.getOrCreateCounter("foo");
        Gauge gauge = collection.getOrCreateGauge("foo{bar=\"value1\"}", () -> 2.0);
        Histogram histogram = collection.getOrCreateHistogram("foo{bar=\"value1\", baz=\"value2\"}");

        assertEquals(3, collection.size());
        assertEquals("foo", counter.getName());
        assertEquals("foo{bar=\"value1\"}", gauge.getName());
        assertEquals("foo{bar=\"value1\", baz=\"value2\"}", histogram.getName());
    }

    @Test
    public void createMetricWithInvalidName_thenThrowException() {
        MetricCollection collection = MetricCollection.create();
        assertThrowsExactly(InvalidMetricNameException.class, () -> collection.getOrCreateHistogram("foo{"));
        assertThrowsExactly(InvalidMetricNameException.class, () -> collection.getOrCreateHistogram("foo{label=}"));
    }

    @Test
    public void testGetOrCreateMetric() {
        MetricCollection collection = MetricCollection.create();
        Counter counter1 = collection.getOrCreateCounter("foo");
        Counter counter2 = collection.getOrCreateCounter("foo");
        Counter counter3 = collection.getOrCreateCounter("foo");


        assertEquals(counter1, counter2);
        assertEquals(counter1, counter3);
        assertEquals(counter2, counter3);
        assertEquals(1, collection.size());

        counter1.inc();
        counter2.inc();

        assertEquals(2, counter1.get());
        assertEquals(2, counter2.get());
        assertEquals(2, counter3.get());
    }

    @Test
    public void tesGetMetricByName() {
        MetricCollection collection = MetricCollection.create();
        collection.getOrCreateCounter("foo");
        Counter counter = collection.getMetric("foo");
        assertNotNull(counter);

        Gauge gauge = collection.getMetric("bar");
        assertNull(gauge);
    }
}
