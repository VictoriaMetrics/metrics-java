/*
 * Copyright (c) 2022 Victoria Metrics Inc.
 */

package io.victoriametrics.client.export;

import com.sun.net.httpserver.*;
import io.victoriametrics.client.metrics.Counter;
import io.victoriametrics.client.metrics.MetricRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * Expose metrics with a plain Java HttpServer.
 *
 * @author Valery Kantor
 */
public class HTTPServer implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(HTTPServer.class);

    private final HttpServer server;

    private HTTPServer(HttpServer httpServer, MetricRegistry metricRegistry, Authenticator authenticator, String context) {
        if (metricRegistry == null) {
            throw new IllegalArgumentException("metricRegistry is null");
        }

        server = httpServer;

        if (context == null) {
            context = "/metrics";
        }
        HttpContext httpContext = server.createContext(context, new MetricHttpHandler(metricRegistry, context));

        if (authenticator != null) {
            httpContext.setAuthenticator(authenticator);
        }
    }

    public void start() {
        server.start();
    }

    public void stop() {
        close();
    }

    @Override
    public void close() {
        server.stop(0);
    }

    public static class MetricHttpHandler implements HttpHandler {

        private final MetricRegistry metricRegistry;

        private final Counter requestsCount;

        public MetricHttpHandler(MetricRegistry metricRegistry, String context) {
            this.metricRegistry = metricRegistry;

            this.requestsCount = metricRegistry.createCounter()
                    .name("http_server_requests_total")
                    .addLabel("context", context)
                    .register();
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            requestsCount.inc();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            try {
                OutputStreamWriter osw = new OutputStreamWriter(baos, StandardCharsets.UTF_8);
                metricRegistry.write(osw);
                osw.flush();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                byte[] bytes = e.getMessage().getBytes();
                exchange.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, bytes.length);
                exchange.getResponseBody().write(bytes);
            }

            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, baos.size());
            baos.writeTo(exchange.getResponseBody());
            exchange.close();
        }
    }

    public static class Builder {
        private Authenticator authenticator;
        private int port = 80;
        private String hostname;
        private InetAddress inetAddress;
        private MetricRegistry collection;
        private String context;

        public Builder withPort(int port) {
            this.port = port;
            return this;
        }

        public Builder withHostname(String hostname) {
            this.hostname = hostname;
            return this;
        }

        public Builder withInetAddress(InetAddress inetAddress) {
            this.inetAddress = inetAddress;
            return this;
        }

        public Builder withContext(String context) {
            this.context = context;
            return this;
        }

        public Builder withAuthenticator(Authenticator authenticator) {
            this.authenticator = authenticator;
            return this;
        }

        public Builder withMetricCollection(MetricRegistry collection) {
            this.collection = collection;
            return this;
        }

        public HTTPServer build() throws IOException {
            if (inetAddress != null && hostname != null) {
                throw new IllegalStateException("'inetAddress' and 'hostname' connot be used at the same time");
            }

            if (inetAddress == null && hostname == null) {
                hostname = "localhost";
            }

            if (inetAddress == null) {
                inetAddress = InetAddress.getByName(hostname);
            }

            var inetSocketAddress = new InetSocketAddress(inetAddress, port);
            HttpServer httpsServer = HttpServer.create(inetSocketAddress, 0);

            return new HTTPServer(httpsServer, collection, authenticator, context);
        }
    }

    public static void main(String[] args) throws IOException {
        var builder = new HTTPServer.Builder()
                .withMetricCollection(MetricRegistry.create())
                .withHostname("::1")
                .withPort(8080)
                .withContext("/metrics");
        //noinspection resource
        var server = builder.build();
        server.start();
    }
}
