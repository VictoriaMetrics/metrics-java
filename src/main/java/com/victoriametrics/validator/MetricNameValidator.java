package com.victoriametrics.validator;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;

/**
 * Prometheus-compatible metric name validator.
 *
 * <p><b>Example of valid metrics:</b></p>
 * <blockquote><pre>
 *  foo
 *  foo{bar="baz"}
 *  foo{bar="baz", a="b"}
 *  </pre></blockquote>
 */
public class MetricNameValidator {

    private static final Pattern metricNamePattern = Pattern.compile("^[a-zA-Z_:][a-zA-Z0-9_:]*$");
    private static final Pattern labelPattern = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

    /**
     * Validate metric name.
     *
     * @param name
     *        A name to be validated
     *
     * @throws InvalidMetricNameException
     *          If the metric name is invalid
     */
    public void validate(String name) {
        Objects.requireNonNull(name, "Metric name cannot be null");

        if (name.isEmpty()) {
            throw new InvalidMetricNameException("Metric name cannot be empty");
        }

        int index = name.indexOf("{");

        if (index < 0) {
            validateMetricName(name);
            return;
        }

        String metricName = name.substring(0, index);
        validateMetricName(metricName);

        if (!name.endsWith("}")) {
            throw new InvalidMetricNameException(format("No closing curly brace %s", name));
        }

        String labels = name.substring(index + 1, name.lastIndexOf("}"));

        if (labels.isEmpty()) return;
        validateLabels(labels);
    }

    private void validateLabels(String input) {
        String[] labels = input.split(",", -1);

        for (String label : labels) {
            int delimiterIndex = label.indexOf("=");
            if (delimiterIndex < 0) {
                throw new InvalidMetricNameException(format("Missing delimiter '=' after '%s'", label));
            }

            String labelName = label.substring(0, delimiterIndex).trim();
            validateLabelName(labelName);

            String labelValue = label.substring(delimiterIndex + 1);

            if (labelValue.isEmpty() || labelValue.charAt(0) != '"') {
                throw new InvalidMetricNameException(format("Missing starring '\"' for '%s' label, '%s'", labelName, labelValue));
            }

            if (!labelValue.substring(1).contains("\"")) {
                throw new InvalidMetricNameException(format("Missing tailing '\"' for '%s' label, '%s' ", labelName, labelValue));
            }
        }
    }

    private void validateMetricName(String name) throws InvalidMetricNameException {
        Matcher matcher = metricNamePattern.matcher(name);
        if (!matcher.matches()) {
            throw new InvalidMetricNameException(format("Invalid metric name %s", name));
        }
    }

    private void validateLabelName(String name) {
        Matcher matcher = labelPattern.matcher(name);
        if (!matcher.matches()) {
            throw new InvalidMetricNameException(format("Invalid label name %s", name));
        }
    }

}
