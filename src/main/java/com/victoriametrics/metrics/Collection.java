package com.victoriametrics.metrics;

import com.victoriametrics.utils.Pair;
import com.victoriametrics.validator.MetricNameValidator;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Collection of grouped metrics
 */
public final class Collection {

    private final Map<String, Metric> collection = new ConcurrentHashMap<>();
    private final MetricNameValidator validator = new MetricNameValidator();

    private Collection() {
    }

    public static Collection create() {
        return new Collection();
    }

    /**
     * Get metric instance from the collection.
     * @param name A metric name.
     * @return Instance of metric. If a metric not found return null
     */
    @SuppressWarnings("unchecked")
    public <T extends Metric> T getMetric(String name) {
        return (T) collection.get(name);
    }

    /**
     * Size of collection.
     */
    public int size() {
        return collection.size();
    }

    public CounterBuilder createCounter() {
        return new CounterBuilder();
    }

    public GaugeBuilder createGauge() {
        return new GaugeBuilder();
    }

    public HistogramBuilder createHistogram() {
        return new HistogramBuilder();
    }

    /**
     * Get {@link Counter} metric or create a new one if it doesn't exist.
     * @param name A metric name
     * @return {@link Counter} if metric name is valid.
     */
    public Counter getOrCreateCounter(String name) {
        validator.validate(name);
        return (Counter) collection.computeIfAbsent(name, Counter::new);
    }

    /**
     * Get {@link Gauge} metric or create a new one if it doesn't exist.
     * @param name A metric name
     * @return {@link Gauge} if metric name is valid.
     */
    public Gauge getOrCreateGauge(String name, Supplier<Double> supplier) {
        validator.validate(name);
        return (Gauge) collection.computeIfAbsent(name, key -> new Gauge(key, supplier));
    }

    /**
     * Get {@link Histogram} metric or create a new one if it doesn't exist.
     * @param name A metric name
     * @return {@link Histogram} if metric name is valid.
     */
    public Histogram getOrCreateHistogram(String name) {
        validator.validate(name);
        return (Histogram) collection.computeIfAbsent(name, Histogram::new);
    }

    public interface MetricBuilder<T> {

        LabelBuilder<T> name(String name);

        /**
         * Register a metric in collection.
         */
        T register();
    }

    private abstract static class AbstractMetricBuilder<T> implements MetricBuilder<T> {
        private final NameBuilder<T> nameBuilder = new DefaultBuilder<>(this);

        @Override
        public LabelBuilder<T> name(String name) {
            return nameBuilder.name(name);
        }

        protected String getMetricName() {
            return nameBuilder.build();
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

    public static class DefaultBuilder<T> implements NameBuilder<T> {
        private String name;
        private final Set<Pair<String, String>> labels = new HashSet<>();

        private final MetricBuilder<T> metricBuilder;

        private DefaultBuilder(MetricBuilder<T> metricBuilder) {
            this.metricBuilder = metricBuilder;
        }

        @Override
        public LabelBuilder<T> name(String name) {
            this.name = name;
            return new DefaultLabelBuilder(metricBuilder);
        }

        @Override
        public String build() {
            final String labels = buildLabels();
            return name + labels;
        }

        private String buildLabels() {
            if (labels.isEmpty()) {
                return "";
            }

            int size = labels.size();

            StringBuilder sb = new StringBuilder("{");
            int i = 0;
            for (Pair<String, String> pair : labels) {
                String name = pair.getKey();
                String value = pair.getValue();

                sb.append(name)
                        .append("=")
                        .append(("\""))
                        .append(value)
                        .append(("\""));

                if (i < size - 1) {
                    sb.append(", ");
                }
                i++;
            }

            sb.append("}");
            return sb.toString();
        }

        public class DefaultLabelBuilder implements LabelBuilder<T> {

            private final MetricBuilder<T> metricBuilder;

            public DefaultLabelBuilder(MetricBuilder<T> metricBuilder) {
                this.metricBuilder = metricBuilder;
            }

            @Override
            public LabelBuilder<T> addLabel(String name, String value) {
                labels.add(Pair.of(name, value));
                return this;
            }

            @Override
            public MetricBuilder<T> then() {
                return metricBuilder;
            }
        }
    }

    interface NameBuilder<T> {

        LabelBuilder<T> name(String name);

        String build();

    }

    interface LabelBuilder<T> {
        LabelBuilder<T> addLabel(String name, String value);

        MetricBuilder<T> then();
    }

}
