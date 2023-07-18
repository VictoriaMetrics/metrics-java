/*
 * Copyright (c) 2023 Victoria Metrics Inc.
 */

package io.victoriametrics.client.springboot;

import io.victoriametrics.client.metrics.Counter;
import io.victoriametrics.client.metrics.MetricRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalManagementPort;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Valery Kantor
 */
@EnableVictoriaMetricsEndpoint
@EnableAutoConfiguration
@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestPropertySource(properties = {
        "management.endpoint.victoriametrics.enabled=true",
        "management.endpoints.web.exposure.include=victoriametrics",
        "management.port=0"
})
@AutoConfigureMockMvc
class VictoriaMetricsEndpointTest {

    @Autowired
    TestRestTemplate template;

    @LocalServerPort
    int localServerPort;

    @Autowired
    MetricRegistry metricRegistry;

    @LocalManagementPort
    int menagementPort;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void test_metrics_export() throws Exception {
        Counter counter = metricRegistry.createCounter()
                                        .name("foo")
                                        .addLabel("label1", "bar")
                                        .register();

        counter.inc();

        mockMvc.perform(MockMvcRequestBuilders.get("/actuator/victoriametrics"))
               .andExpect(status().isOk())
               .andExpect(content().string("foo{label1=\"bar\"} 1\n"));
    }

}
