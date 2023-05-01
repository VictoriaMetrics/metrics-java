/*
 * Copyright (c) 2022 Victoria Metrics Inc.
 */

package io.victoriametrics.client.benchmark;

import io.victoriametrics.client.metrics.Counter;
import io.victoriametrics.client.metrics.MetricRegistry;
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
public class CounterBenchmark {

    private Counter victoriaMetricsCounter;

    private Counter victoriaMetricsCounterNoLabels;

    private io.prometheus.client.Counter prometheusCounter;

    private io.prometheus.client.Counter prometheusCounterNoLabels;

    @Setup
    public void setup() {
        MetricRegistry metricRegistry = MetricRegistry.create();
        this.victoriaMetricsCounter = metricRegistry.createCounter()
                                                    .name("foo")
                                                    .addLabel("bar", "value")
                                                    .register();

        this.victoriaMetricsCounterNoLabels = metricRegistry.createCounter()
                                                            .name("foo")
                                                            .register();

        prometheusCounter = io.prometheus.client.Counter.build()
                                                        .name("name")
                                                        .help("some description..")
                                                        .labelNames("some", "group").create();

        prometheusCounterNoLabels = io.prometheus.client.Counter.build()
                                                                .name("name")
                                                                .help("some description..")
                                                                .create();
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void victoriaMetricsCounterIncBenchmark() {
        this.victoriaMetricsCounter.inc();
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void prometheuseCounterIncBenchmark() {
        this.prometheusCounter.labels("bar", "value").inc();
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void victoriaMetricsCounterNoLabelsIncBenchmark() {
        this.victoriaMetricsCounterNoLabels.inc();
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void prometheuseCounterNoLabelsIncBenchmark() {
        this.prometheusCounterNoLabels.inc();
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(CounterBenchmark.class.getSimpleName())
                .warmupIterations(5)
                .measurementIterations(4)
                .threads(4)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
