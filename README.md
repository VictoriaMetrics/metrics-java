
# Java lightweight library for exporting metrics in Prometheus format

### Install 

Checkout the repository and install to local Maven repository.

```shell
./mvnw install
./mvnw source:jar install
```

Add dependecy in to your pom.xml

```xml
<dependency>
    <groupId>io.victoriametrics.client</groupId>
    <artifactId>metrics</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### Usage

```java
import io.victoriametrics.client.export.HTTPServer;
import io.victoriametrics.client.metrics.Histogram;
import io.victoriametrics.client.metrics.MetricRegistry;
import io.victoriametrics.client.metrics.Summary;

public class Example {

    public static void main(String[] args) {
        MetricRegistry registry = MetricRegistry.create();
        Histogram histogram = registry.getOrCreateHistogram("response_size{path=\"/foo/bar\"}");
        histogram.update(Math.random() * 1000);

        Summary summary = registry.getOrCreateSummary("request_duration_seconds{path=\"/foo/bar\"}");
        for (int i = 0; i < 100000; i++) {
            summary.update(i);
        }

        HTTPServer server = new HTTPServer.Builder()
                .withPort(3000)
                .withMetricCollection(collection)
                .build();

        server.start();
    }
}
```
