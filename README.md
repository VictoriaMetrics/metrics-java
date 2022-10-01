
# Java lightweight library for exporting metrics in Prometheus format

### Usage

```java
import io.victoriametrics.client.export.HTTPServer;
import io.victoriametrics.client.metrics.Histogram;
import io.victoriametrics.client.metrics.MetricCollection;
import io.victoriametrics.client.metrics.Summary;

public class Example {

    public static void main(String[] args) throws Exception {
        MetricCollection collection = MetricCollection.create();
        Histogram histogram = collection.getOrCreateHistogram("response_size{path=\"/foo/bar\"}");
        histogram.update(Math.random() * 1000);

        Summary summary = collection.getOrCreateSummary("request_duration_seconds{path=\"/foo/bar\"}");
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
