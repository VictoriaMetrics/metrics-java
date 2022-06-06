package io.victoriametrics.client.validator;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MetricNameValidatorTest {

    @Test
    public void whenMetricNameIsNull_thenThrowNullPointerException() {
        MetricNameValidator validator = new MetricNameValidator();

        Exception exception = assertThrows(NullPointerException.class, () -> validator.validate(null));

        String actualMessage = exception.getMessage();
        assertEquals("Metric name cannot be null", actualMessage);
    }

    @Test
    public void whenMetricNameIsEmpty_thenAssertionSucceeds() {
        MetricNameValidator validator = new MetricNameValidator();
        Exception exception = assertThrows(InvalidMetricNameException.class,() -> validator.validate(""));

        String actualMessage = exception.getMessage();
        assertEquals("Metric name cannot be empty", actualMessage);
    }

    @Test
    public void whenMetricNameSuccess() {
        MetricNameValidator validator = new MetricNameValidator();
        validator.validate("a");
        validator.validate("_9:8");
        validator.validate("a{}");
        validator.validate("a{foo=\"bar\"}");
        validator.validate("foo{bar=\"baz\", x=\"y\\\"z\"}");
        validator.validate("foo{bar=\"b}az\"}");
        validator.validate(":foo:bar{bar=\"a\",baz=\"b\"}");
    }

    @Test
    public void whenInvalidMetricNameFails() {
        MetricNameValidator validator = new MetricNameValidator();
        List<String> invalidMetricNames = Arrays.asList(
                "{}", "foo{bar}", "foo{=}", "foo{=\"\"}",
                "foo{", "foo}", "foo{bar=}", "foo{bar=\"",
                "foo{bar=\"}", "foo{bar=\"val\",}", "foo{bar=\"val\", x",
                "foo{bar=\"val\", x=", "foo{bar=\"val\", x=\"",
                "foo{bar=\"val\", x=\"}"
        );

        invalidMetricNames.forEach(metricName -> {
            Exception exception = assertThrows(InvalidMetricNameException.class, () -> validator.validate(metricName));
            assertInstanceOf(InvalidMetricNameException.class, exception);
        });
    }
}
