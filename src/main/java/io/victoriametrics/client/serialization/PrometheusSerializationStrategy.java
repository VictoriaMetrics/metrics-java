/*
 * Copyright (c) 2022 Victoria Metrics Inc.
 */

package io.victoriametrics.client.serialization;

import io.victoriametrics.client.metrics.MetricVisitor;
import io.victoriametrics.client.metrics.Counter;
import io.victoriametrics.client.metrics.Gauge;
import io.victoriametrics.client.metrics.Histogram;
import io.victoriametrics.client.metrics.Metric;
import io.victoriametrics.client.utils.Pair;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author Valery Kantor
 */
public class PrometheusSerializationStrategy implements SerializationStrategy {

    public void serialize(Collection<Metric> metrics, Writer writer) {
        MetricVisitor visitor = new MetricVisitor() {
            @Override
            public void visit(Counter counter) {
                String prefix = counter.getName();
                long value = counter.get();

                StringBuilder sb = new StringBuilder(prefix)
                        .append(" ")
                        .append(value)
                        .append("\n");
                try {
                    writer.write(sb.toString());
                } catch (IOException e) {
                    throw new MetricSerializationException("Unable to serialize Counter metric: " + sb, e);
                }
            }

            @Override
            public void visit(Gauge gauge) {
                String prefix = gauge.getName();
                double value = gauge.get();

                StringBuilder sb = new StringBuilder(prefix)
                        .append(" ")
                        .append(value)
                        .append("\n");
                try {
                    writer.write(sb.toString());
                } catch (IOException e) {
                    throw new MetricSerializationException("Unable to serialize Gauge metric: " + sb, e);
                }
            }

            @Override
            public void visit(Histogram histogram) {
                writeHistogram(writer, histogram);
            }
        };

        metrics.forEach(metric -> metric.accept(visitor));
    }

    private void writeHistogram(Writer writer, Histogram histogram) {
        String prefix = histogram.getName();
        LongAdder countTotal = new LongAdder();

        histogram.visit((vmrange, count) -> {
            String tag = String.format("vmrange=%s", vmrange);
            String metricName = applyTag(prefix, tag);
            Pair<String, String> metricPair = splitMetricName(metricName);

            String name = metricPair.getKey();
            String labels = metricPair.getValue();

            String value = String.format("%s_bucket%s %d\n", name, labels, count);
            try {
                writer.write(value);
            }  catch (IOException e) {
                throw new MetricSerializationException("Unable to serialize Histogram metric: " + value, e);
            }

            countTotal.increment();
        });

        Pair<String, String> metricPair = splitMetricName(prefix);
        String name = metricPair.getKey();
        String labels = metricPair.getValue();
        double sum = histogram.getSum();

        try {
            writer.write(String.format("%s_sum%s %f\n", name, labels, sum));
            writer.write(String.format("%s_count%s %d\n", name, labels, countTotal.sum()));
        } catch (IOException e) {
            throw new MetricSerializationException("Unable to serialize Histogram sum", e);
        }
    }

    private String applyTag(String name, String tag) {
        if (!name.endsWith("}")) {
            return String.format("%s{%s}", name, tag);
        }

        return String.format("%s,%s}", name.substring(0, name.length() - 1), tag);
    }

    private Pair<String, String> splitMetricName(String name) {
        int index = name.indexOf('{');
        if (index < 0) {
            return Pair.of(name, "");
        }

        String metricName = name.substring(0, index);
        String labels = name.substring(index);
        return Pair.of(metricName, labels);
    }
}
