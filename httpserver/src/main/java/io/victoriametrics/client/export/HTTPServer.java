/*
 * Copyright (c) 2022 Victoria Metrics Inc.
 */

package io.victoriametrics.client.export;

import com.sun.net.httpserver.*;
import io.victoriametrics.client.metrics.MetricRegistry;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * Expose metrics with a plain Java HttpServer.
 * @author Valery Kantor
 */
public class HTTPServer implements Closeable {

    private HttpServer server;

    private HTTPServer(HttpServer httpServer, MetricRegistry metricRegistry, Authenticator authenticator, String context) {
        this.server = httpServer;
        HttpContext httpContext = this.server.createContext(context != null ? context : "/metrics", new MeticHttpHandler(metricRegistry));

        if (authenticator != null) {
            httpContext.setAuthenticator(authenticator);
        }
    }

    public void start() {
        this.server.start();
    }

    public void stop() {
        close();
    }

    @Override
    public void close() {
        this.server.stop(0);
    }

    public static class MeticHttpHandler implements HttpHandler {

        private final MetricRegistry metricRegistry;

        public MeticHttpHandler(MetricRegistry metricRegistry) {
            this.metricRegistry = metricRegistry;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(baos, StandardCharsets.UTF_8);

            this.metricRegistry.write(osw);
            osw.flush();

            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, baos.size());
            baos.writeTo(exchange.getResponseBody());
            osw.close();
            exchange.close();
        }
    }

    public static class Builder {
        private Authenticator authenticator;
        private int port = 0;
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
            InetSocketAddress inetSocketAddress;
            if (inetAddress != null) {
                assertNull(hostname, "'inetAddress' and 'hostname' connot be used at the same time");
                inetSocketAddress = new InetSocketAddress(inetAddress, port);
            } else if (hostname != null) {
                inetSocketAddress = new InetSocketAddress(hostname, port);
            } else {
                inetSocketAddress = new InetSocketAddress(port);
            }
            HttpServer httpsServer = HttpServer
                    .create(inetSocketAddress, 0);

            return new HTTPServer(httpsServer, collection, authenticator, context);
        }
    }

    private static void assertNull(Object obj, String message) {
        if (obj != null) throw new IllegalStateException(message);
    }

}
