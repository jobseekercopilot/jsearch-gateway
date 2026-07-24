package com.jobseekercopilot.jsearchgateway.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jobseekercopilot.generated.systemdataservice.api.FixtureControllerApi;
import com.jobseekercopilot.generated.systemdataservice.model.DemoJob;
import com.jobseekercopilot.generated.systemdataservice.model.FixtureJobSearchResponse;
import com.jobseekercopilot.jsearchgateway.config.FixtureProperties;
import com.jobseekercopilot.jsearchgateway.model.dto.JSearchSearchRequest;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;

class FixtureJSearchProviderClientTest {

    private final FixtureProperties properties = properties();
    private final FixtureControllerApi fixtureApi =
            mock(FixtureControllerApi.class);
    private final FixtureJSearchProviderClient client =
            new FixtureJSearchProviderClient(properties, fixtureApi);

    @Test
    void mapsPopulatedFixtureRemoteRequestAndNextCursor() {
        DemoJob source = new DemoJob()
                .id("fixture-id")
                .externalReference("js-fixture-1")
                .title("Fixture Engineer")
                .companyName("Fallback Company")
                .companyDisplayName("Fixture Company")
                .description("Synthetic fixture")
                .locationName("Remote UK")
                .region("UK")
                .latitude(53.0)
                .longitude(-1.0)
                .salaryMinimum(50000)
                .salaryMaximum(70000)
                .salaryCurrency("GBP")
                .salaryPeriod("YEAR")
                .employmentType("FULL_TIME")
                .remoteType("REMOTE")
                .datePosted("2026-07-20T10:00:00Z")
                .closingDate("2026-08-20T10:00:00Z")
                .sourceUrl(URI.create(
                        "https://fixtures.example.test/js-fixture-1"));
        FixtureJobSearchResponse fixture = new FixtureJobSearchResponse()
                .page(2)
                .pageSize(10)
                .totalResults(40)
                .jobs(List.of(source));
        when(fixtureApi.searchJobs(
                "dataset", "2.0", "DEMO_READY", "JSEARCH",
                "Engineer", "Remote", 2, 10,
                null, null, null, "REMOTE"))
                .thenReturn(fixture);

        JSearchSearchRequest request = new JSearchSearchRequest();
        request.setTargetRole("Engineer");
        request.setLocation("London");
        request.setRemoteOnly(true);
        request.setCursor("fixture-page-2");

        var response = client.search(request);

        verify(fixtureApi).searchJobs(
                "dataset", "2.0", "DEMO_READY", "JSEARCH",
                "Engineer", "Remote", 2, 10,
                null, null, null, "REMOTE");
        assertThat(response.getProvider()).isEqualTo("JSEARCH");
        assertThat(response.getCursor()).isEqualTo("fixture-page-3");
        assertThat(response.getJobs()).singleElement().satisfies(job -> {
            assertThat(job.getExternalJobId())
                    .isEqualTo("js-fixture-1");
            assertThat(job.getTitle()).isEqualTo("Fixture Engineer");
            assertThat(job.getCompanyName()).isEqualTo("Fixture Company");
            assertThat(job.getPublisher()).isEqualTo("JSearch Fixture");
            assertThat(job.getLocationDisplayName())
                    .isEqualTo("Remote UK");
            assertThat(job.getCountry()).isEqualTo("GB");
            assertThat(job.getSalaryMinimum()).isEqualTo(50000);
            assertThat(job.getSalaryCurrency()).isEqualTo("GBP");
            assertThat(job.getPostedAt())
                    .isEqualTo("2026-07-20T10:00:00Z");
            assertThat(job.getExpiresAt())
                    .isEqualTo("2026-08-20T10:00:00Z");
            assertThat(job.getRemote()).isTrue();
            assertThat(job.getPrimaryApplyUrl()).isEqualTo(
                    "https://fixtures.example.test/js-fixture-1");
            assertThat(job.getApplyOptions()).singleElement()
                    .satisfies(option ->
                            assertThat(option.getApplyUrl())
                                    .isEqualTo(job.getPrimaryApplyUrl()));
        });
    }

    @Test
    void returnsDeterministicEmptyResultForNullFixtureBody() {
        JSearchSearchRequest request = new JSearchSearchRequest();
        request.setTargetRole("No Matches");
        when(fixtureApi.searchJobs(
                "dataset", "2.0", "DEMO_READY", "JSEARCH",
                "No Matches", null, 0, 10,
                null, null, null, null))
                .thenReturn(null);

        var response = client.search(request);

        assertThat(response.getProvider()).isEqualTo("JSEARCH");
        assertThat(response.getCursor()).isNull();
        assertThat(response.getJobs()).isEmpty();
    }

    private FixtureProperties properties() {
        FixtureProperties result = new FixtureProperties();
        result.setDatasetId("dataset");
        result.setDatasetVersion("2.0");
        result.setScenario("DEMO_READY");
        return result;
    }
}
