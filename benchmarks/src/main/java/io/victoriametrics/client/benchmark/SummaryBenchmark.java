/*
 * Copyright (c) 2023 Victoria Metrics Inc.
 */

package io.victoriametrics.client.benchmark;

import io.victoriametrics.client.metrics.Histogram;
import io.victoriametrics.client.metrics.MetricCollection;
import io.victoriametrics.client.metrics.Summary;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * @author Valery Kantor
 */
@State(Scope.Benchmark)
public class SummaryBenchmark {

    private Summary victoriaMetricsSummary;
    private Histogram victoriaMetricsHistogram;
    private io.prometheus.client.Summary prometheusSimpleSummary;
    private io.prometheus.client.Histogram prometheusSimpleHistogram;

    @Setup
    public void setup() {
        MetricCollection metricCollection = MetricCollection.create();

        victoriaMetricsSummary = metricCollection.getOrCreateSummary("summary");

        victoriaMetricsHistogram = metricCollection.createHistogram()
                                                   .name("histogram")
                                                   .addLabel("foo", "bar")
                                                   .then()
                                                   .register();

        prometheusSimpleSummary = io.prometheus.client.Summary.build()
                                                              .name("name")
                                                              .help("some description..")
                                                              .create();

        prometheusSimpleHistogram = io.prometheus.client.Histogram.build()
                                                                  .name("name")
                                                                  .help("some description..")
                                                                  .labelNames("foo", "bar").create();
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void victoriaMetricsSummaryBenchmark() {
        victoriaMetricsSummary.update(1);
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void victoriaMetricsHistogramBenchmark() {
        victoriaMetricsHistogram.update(1);
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void prometheusSimpleSummaryBenchmark() {
        prometheusSimpleSummary.observe(1);
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void prometheusSimpleHistogramBenchmark() {
        prometheusSimpleHistogram.labels("test", "test").observe(1) ;
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(SummaryBenchmark.class.getSimpleName())
                .warmupIterations(5)
                .measurementIterations(4)
                .threads(4)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
