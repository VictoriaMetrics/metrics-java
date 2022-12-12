package io.victoriametrics.client.validator;

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
            throw new InvalidMetricNameException("No closing curly brace " + name);
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
                throw new InvalidMetricNameException("Missing delimiter '=' after '" + label + ",");
            }

            String labelName = label.substring(0, delimiterIndex).trim();
            validateLabelName(labelName);

            String labelValue = label.substring(delimiterIndex + 1);

            if (labelValue.isEmpty() || labelValue.charAt(0) != '"') {
                String message = "Missing starring '\"' for '" +
                        labelName + "' label, '" + labelValue + "'";

                throw new InvalidMetricNameException(message);
            }

            if (!labelValue.substring(1).contains("\"")) {
                String message = "Missing tailing '\"' for '" +
                        labelName + "' label, '" + labelValue + "'";
                throw new InvalidMetricNameException(message);
            }
        }
    }

    private void validateMetricName(String name) throws InvalidMetricNameException {
        Matcher matcher = metricNamePattern.matcher(name);
        if (!matcher.matches()) {
            throw new InvalidMetricNameException("Invalid metric name " + name);
        }
    }

    private void validateLabelName(String name) {
        Matcher matcher = labelPattern.matcher(name);
        if (!matcher.matches()) {
            throw new InvalidMetricNameException("Invalid label name " + name);
        }
    }

}
