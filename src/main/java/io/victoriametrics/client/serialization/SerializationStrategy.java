/*
 * Copyright (c) 2022 Victoria Metrics Inc.
 */

package io.victoriametrics.client.serialization;

import io.victoriametrics.client.metrics.Metric;

import java.io.Writer;
import java.util.Collection;

/**
 * @author Valery Kantor
 */
public interface SerializationStrategy {

    void serialize(Metric metric, Writer writer);

}
