package com.banking.dashboard.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthContributor;
import org.springframework.boot.health.registry.HealthContributorRegistry;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class HealthEndpointTest {

    @LocalServerPort
    private int port;

    private final HttpClient client = HttpClient.newHttpClient();

    @Autowired
    private ConfigurableApplicationContext context;

    @Autowired
    private HealthContributorRegistry healthContributors;

    @Test
    void healthIsUpWhenReadyAndDatabaseIsAvailable() throws Exception {
        var response = health();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"status\":\"UP\"");
    }

    @Test
    void healthIsUnavailableWhenDatabaseIsDown() throws Exception {
        HealthContributor database = healthContributors.unregisterContributor("db");
        try {
            healthContributors.registerContributor("db", (HealthIndicator) () -> Health.down().build());

            var response = health();

            assertThat(response.statusCode()).isEqualTo(503);
            assertThat(response.body()).contains("\"status\":\"DOWN\"");
        } finally {
            healthContributors.unregisterContributor("db");
            healthContributors.registerContributor("db", database);
        }
    }

    @Test
    void healthIsUnavailableWhenNotAcceptingTraffic() throws Exception {
        AvailabilityChangeEvent.publish(context, ReadinessState.REFUSING_TRAFFIC);
        try {
            var response = health();

            assertThat(response.statusCode()).isEqualTo(503);
            assertThat(response.body()).contains("\"status\":\"OUT_OF_SERVICE\"");
        } finally {
            AvailabilityChangeEvent.publish(context, ReadinessState.ACCEPTING_TRAFFIC);
        }
    }

    private HttpResponse<String> health() throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/health"))
                .timeout(Duration.ofSeconds(10)).build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
