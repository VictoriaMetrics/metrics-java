package io.victoriametrics.client.metrics;

import io.victoriametrics.client.serialization.PrometheusSerializationStrategy;
import io.victoriametrics.client.serialization.SerializationStrategy;
import io.victoriametrics.client.validator.MetricNameValidator;

import java.io.Writer;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * The registry groups all metrics.
 *
 */
public final class MetricRegistry {

    private final Map<String, Metric> collection = new ConcurrentHashMap<>();
    private final MetricNameValidator validator = new MetricNameValidator();

    private SerializationStrategy serializationStrategy = new PrometheusSerializationStrategy();

    private MetricRegistry() {
    }

    /**
     * Create a metric registry instance.
     *
     * @return {@link MetricRegistry}
     */
    public static MetricRegistry create() {
        return new MetricRegistry();
    }

    /**
     * Size of the registry.
     */
    public int size() {
        return collection.size();
    }

    /**
     * Define a Counter builder.
     *
     * @return {@link CounterBuilder}
     */
    public CounterBuilder createCounter() {
        return new CounterBuilder();
    }

    /**
     * Define a Gauge builder.
     *
     * @return {@link GaugeBuilder}
     */
    public GaugeBuilder createGauge() {
        return new GaugeBuilder();
    }

    /**
     * Define a Histogram builder.
     *
     * @return {@link HistogramBuilder}
     */
    public HistogramBuilder createHistogram() {
        return new HistogramBuilder();
    }

    /**
     * Get {@link Counter} metric or create a new one if it doesn't exist.
     * @param name A metric name
     * @return {@link Counter} if metric name is valid.
     */
    public Counter getOrCreateCounter(String name) {
        return (Counter) collection.computeIfAbsent(name, key -> {
            validator.validate(key);
            return new Counter(key);
        });
    }

    /**
     * Get {@link Gauge} metric or create a new one if it doesn't exist.
     * @param name A metric name
     * @return {@link Gauge} if metric name is valid.
     */
    public Gauge getOrCreateGauge(String name, Supplier<Double> supplier) {
        return (Gauge) collection.computeIfAbsent(name, key -> {
            validator.validate(key);
            return new Gauge(key, supplier);
        });
    }

    /**
     * Get {@link Histogram} metric or create a new one if it doesn't exist.
     * @param name A metric name
     * @return {@link Histogram} if metric name is valid.
     */
    public Histogram getOrCreateHistogram(String name) {
        return (Histogram) collection.computeIfAbsent(name, key -> {
            validator.validate(name);
            return new Histogram(key);
        });
    }

    /**
     * Get {@link Summary} metric or create a new one if it doesn't exist.
     * Creates {@link Summary} with default configuration.
     * @param name A metric name
     * @return {@link Histogram} if metric name is valid.
     */
    public Summary getOrCreateSummary(String name) {
        return (Summary) collection.computeIfAbsent(name, key-> {
            validator.validate(key);
            return new Summary(key);
        });
    }

    /**
     * Get {@link Summary} metric or create a new one if it doesn't exist.
     * Creates configurable  {@link Summary} if doesn't exists.
     * @param name A metric name
     * @return {@link Histogram} if metric name is valid.
     */
    public Summary getOrCreateSummary(String name, double[] quantiles, int windows, long windowDurationSeconds) {
        return (Summary) collection.computeIfAbsent(name, key-> {
            validator.validate(key);
            return new Summary(key, quantiles, windows, windowDurationSeconds);
        });
    }

    /**
     * Write metricts
     * @param writer
     */
    public void write(Writer writer) {
        Collection<Metric> metrics = collection.values();
        metrics.forEach(metric -> serializationStrategy.serialize(metric, writer));
    }

    /**
     * Set strategy which applies when serialize a metric.
     * @param strategy  Implementation of serialization strategy
     */
    public void setSerializationStrategy(SerializationStrategy strategy) {
        this.serializationStrategy = strategy;
    }

    public interface MetricBuilder<T> {

        MetricBuilder<T> name(String name);

        MetricBuilder<T> addLabel(String name, String value);

        /**
         * Register a metric in collection.
         */
        T register();
    }

    private abstract static class AbstractMetricBuilder<T> implements MetricBuilder<T> {
        private String name;
        private final Map<String, String> labels = new LinkedHashMap<>();

        @Override
        public MetricBuilder<T> name(String name) {
            this.name = name;
            return this;
        }

        @Override
        public MetricBuilder<T> addLabel(String name, String value) {
            labels.put(name, value);
            return this;
        }

        protected String getMetricName() {
            return name + buildLabels();
        }

        private String buildLabels() {
            if (labels.isEmpty()) {
                return "";
            }

            int size = labels.size();

            StringBuilder sb = new StringBuilder("{");

            final int[] i = {0};
            labels.forEach((labelName, value) -> {
                sb.append(labelName)
                  .append("=")
                  .append(("\""))
                  .append(value)
                  .append(("\""));

                if (i[0] < size - 1) {
                    sb.append(", ");
                }
                i[0]++;
            });

            sb.append("}");
            return sb.toString();
        }
    }

    public class CounterBuilder extends AbstractMetricBuilder<Counter> {

        @Override
        public Counter register() {
            return (Counter) collection.computeIfAbsent(getMetricName(), Counter::new);
        }

    }

    public class GaugeBuilder extends AbstractMetricBuilder<Gauge> {

        private Supplier<Double> supplier;

        public GaugeBuilder withSupplier(Supplier<Double> supplier) {
            this.supplier = supplier;
            return this;
        }

        @Override
        public Gauge register() {
            return (Gauge) collection.computeIfAbsent(getMetricName(), name -> new Gauge(name, supplier));
        }
    }

    public class HistogramBuilder extends AbstractMetricBuilder<Histogram> {
        @Override
        public Histogram register() {
            return (Histogram) collection.computeIfAbsent(getMetricName(), Histogram::new);
        }
    }

}
