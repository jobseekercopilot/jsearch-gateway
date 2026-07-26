package com.jobseekercopilot.jsearchgateway.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jobseekercopilot.jsearchgateway.config.JSearchProperties;
import com.jobseekercopilot.jsearchgateway.model.dto.JSearchSearchRequest;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@ExtendWith(OutputCaptureExtension.class)
class JSearchApiClientTest {

    private final AtomicInteger responseStatus = new AtomicInteger(200);
    private final Queue<String> responseBodies =
            new ConcurrentLinkedQueue<>();
    private final List<URI> requestedUris = new CopyOnWriteArrayList<>();
    private final AtomicReference<String> requestedApiKey =
            new AtomicReference<>();
    private HttpServer server;
    private JSearchProperties properties;

    @BeforeEach
    void startProviderStub() throws IOException {
        server = HttpServer.create(
                new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", this::respond);
        server.start();

        properties = new JSearchProperties();
        properties.setBaseUrl(
                "http://127.0.0.1:" + server.getAddress().getPort());
        properties.setApiKey("synthetic-key");
        properties.setCountry("gb");
        properties.setLanguage("en");
        properties.setPagesPerSearch(1);
        properties.setEnabled(true);
    }

    @AfterEach
    void stopProviderStub() {
        server.stop(0);
    }

    @Test
    void mapsRepresentativeResponseApplyOptionsAndProviderCursors() {
        properties.setPagesPerSearch(2);
        responseBodies.add("""
                {
                  "cursor": "cursor-2",
                  "data": [
                    {
                      "job_id": "js-1",
                      "job_title": "Platform Engineer",
                      "employer_name": "Example Ltd",
                      "job_publisher": "Example Jobs",
                      "job_description": "Build reliable services",
                      "job_employment_types": ["FULLTIME", "CONTRACTOR"],
                      "job_apply_link": "https://jobs.example.test/js-1",
                      "job_apply_is_direct": true,
                      "job_location": "London, UK",
                      "job_city": "London",
                      "job_state": "England",
                      "job_country": "GB",
                      "job_latitude": 51.507351,
                      "job_longitude": -0.127758,
                      "job_min_salary": 65000,
                      "job_max_salary": 85000,
                      "job_salary_currency": "GBP",
                      "job_salary_period": "YEAR",
                      "job_posted_at_datetime_utc": "2026-07-24T09:00:00Z",
                      "job_offer_expiration_datetime_utc": "2026-08-24T09:00:00Z",
                      "job_is_remote": true,
                      "apply_options": [
                        {
                          "publisher": "Example Employer",
                          "apply_link": "https://employer.example.test/apply/js-1",
                          "is_direct": true
                        }
                      ]
                    }
                  ]
                }
                """);
        responseBodies.add("""
                {
                  "data": {
                    "jobs": [
                      {
                        "job_id": "js-2",
                        "job_title": "Backend Engineer",
                        "employer_name": "Second Ltd",
                        "job_apply_link": "https://jobs.example.test/js-2"
                      }
                    ]
                  }
                }
                """);

        var response = new JSearchApiClient(properties)
                .search(request(true, null));

        assertThat(response.getProvider()).isEqualTo("JSEARCH");
        assertThat(response.getCursor()).isNull();
        assertThat(response.getJobs()).hasSize(2);
        assertThat(response.getJobs().get(0)).satisfies(job -> {
            assertThat(job.getExternalJobId()).isEqualTo("js-1");
            assertThat(job.getTitle()).isEqualTo("Platform Engineer");
            assertThat(job.getCompanyName()).isEqualTo("Example Ltd");
            assertThat(job.getPublisher()).isEqualTo("Example Jobs");
            assertThat(job.getEmploymentType()).isEqualTo("FULLTIME");
            assertThat(job.getPrimaryApplyUrl())
                    .isEqualTo("https://jobs.example.test/js-1");
            assertThat(job.getDirectApply()).isTrue();
            assertThat(job.getLocationDisplayName())
                    .isEqualTo("London, UK");
            assertThat(job.getLatitude())
                    .isEqualByComparingTo("51.507351");
            assertThat(job.getSalaryMinimum()).isEqualTo(65000);
            assertThat(job.getSalaryCurrency()).isEqualTo("GBP");
            assertThat(job.getPostedAt())
                    .isEqualTo("2026-07-24T09:00:00Z");
            assertThat(job.getExpiresAt())
                    .isEqualTo("2026-08-24T09:00:00Z");
            assertThat(job.getRemote()).isTrue();
            assertThat(job.getApplyOptions()).singleElement()
                    .satisfies(option -> {
                        assertThat(option.getPublisher())
                                .isEqualTo("Example Employer");
                        assertThat(option.getApplyUrl()).isEqualTo(
                                "https://employer.example.test/apply/js-1");
                        assertThat(option.getDirect()).isTrue();
                    });
        });
        assertThat(response.getJobs().get(1).getExternalJobId())
                .isEqualTo("js-2");
        assertThat(requestedUris).hasSize(2);
        assertThat(requestedUris.get(0).getPath())
                .isEqualTo("/search-v2");
        assertThat(requestedUris.get(0).getRawQuery())
                .contains("query=Platform%20Engineer%20in%20London")
                .contains("country=gb")
                .contains("language=en")
                .contains("work_from_home=true")
                .doesNotContain("cursor=");
        assertThat(requestedUris.get(1).getRawQuery())
                .contains("cursor=cursor-2");
        assertThat(requestedApiKey).hasValue("synthetic-key");
    }

    @Test
    void ignoresUnsearchableRowsAndKeepsMalformedValuesAbsent() {
        responseBodies.add("""
                {
                  "data": [
                    {},
                    {
                      "job_title": "Sparse role",
                      "job_min_salary": "not-a-number",
                      "job_latitude": "not-a-number",
                      "job_apply_is_direct": "not-a-boolean",
                      "job_is_remote": "not-a-boolean",
                      "apply_options": [
                        {
                          "publisher": "Sparse publisher",
                          "is_direct": "not-a-boolean"
                        }
                      ]
                    }
                  ]
                }
                """);

        var response = new JSearchApiClient(properties)
                .search(request(false, null));

        assertThat(response.getJobs()).singleElement().satisfies(job -> {
            assertThat(job.getTitle()).isEqualTo("Sparse role");
            assertThat(job.getExternalJobId()).isNull();
            assertThat(job.getSalaryMinimum()).isNull();
            assertThat(job.getLatitude()).isNull();
            assertThat(job.getDirectApply()).isNull();
            assertThat(job.getRemote()).isNull();
            assertThat(job.getApplyOptions()).singleElement()
                    .satisfies(option ->
                            assertThat(option.getDirect()).isNull());
        });
    }

    @Test
    void returnsContractValidEmptyResponseForZeroMatches() {
        responseBodies.add("""
                {"data":{"jobs":[]}}
                """);

        var response = new JSearchApiClient(properties)
                .search(request(false, null));

        assertThat(response.getProvider()).isEqualTo("JSEARCH");
        assertThat(response.getCursor()).isNull();
        assertThat(response.getJobs()).isEmpty();
    }

    @Test
    void translatesRateLimitAndUpstreamErrors() {
        responseStatus.set(429);
        assertThatThrownBy(() -> new JSearchApiClient(properties)
                .search(request(false, null)))
                .isInstanceOf(
                        JSearchApiClient.ProviderUnavailableException.class)
                .hasMessage("JSearch rate limit exceeded");

        responseStatus.set(503);
        assertThatThrownBy(() -> new JSearchApiClient(properties)
                .search(request(false, null)))
                .isInstanceOf(
                        JSearchApiClient.ProviderUnavailableException.class)
                .hasMessage("JSearch API request failed");
    }

    @Test
    void rejectsMissingLiveCredentialInsteadOfReturningNoMatches() {
        properties.setApiKey("");

        assertThatThrownBy(() -> new JSearchApiClient(properties)
                        .search(request(false, null)))
                .isInstanceOf(
                        JSearchApiClient.ProviderUnavailableException.class)
                .hasMessage(
                        "JSearch live provider credential is not configured");
        assertThat(requestedUris).isEmpty();
    }

    @Test
    void explicitKillSwitchReturnsNoMatchesWithoutCredential() {
        properties.setEnabled(false);
        properties.setApiKey("");

        var response = new JSearchApiClient(properties)
                .search(request(false, null));

        assertThat(response.getProvider()).isEqualTo("JSEARCH");
        assertThat(response.getJobs()).isEmpty();
        assertThat(requestedUris).isEmpty();
    }

    @Test
    void upstreamFailureLogsNeverContainCredential(
            CapturedOutput output) {
        String privateApiKey = "private-api-key-for-redaction";
        properties.setApiKey(privateApiKey);
        responseStatus.set(503);

        assertThatThrownBy(() -> new JSearchApiClient(properties)
                        .search(request(false, null)))
                .isInstanceOf(
                        JSearchApiClient.ProviderUnavailableException.class)
                .hasMessage("JSearch API request failed");

        assertThat(output)
                .contains("status=503")
                .doesNotContain(privateApiKey, "X-API-KEY");
    }

    private JSearchSearchRequest request(
            boolean remoteOnly,
            String cursor) {
        JSearchSearchRequest request = new JSearchSearchRequest();
        request.setTargetRole("Platform Engineer");
        request.setLocation("London");
        request.setRemoteOnly(remoteOnly);
        request.setCursor(cursor);
        return request;
    }

    private void respond(HttpExchange exchange) throws IOException {
        requestedUris.add(exchange.getRequestURI());
        requestedApiKey.set(
                exchange.getRequestHeaders().getFirst("X-API-KEY"));
        String response = responseBodies.poll();
        if (response == null) {
            response = "{\"data\":[]}";
        }
        byte[] body = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set(
                "Content-Type", "application/json");
        exchange.sendResponseHeaders(responseStatus.get(), body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }
}
