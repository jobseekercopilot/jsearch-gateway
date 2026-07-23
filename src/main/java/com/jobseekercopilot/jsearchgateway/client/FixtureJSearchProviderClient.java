package com.jobseekercopilot.jsearchgateway.client;

import com.jobseekercopilot.jsearchgateway.config.FixtureProperties;
import com.jobseekercopilot.jsearchgateway.model.dto.JSearchApplyOption;
import com.jobseekercopilot.jsearchgateway.model.dto.JSearchJob;
import com.jobseekercopilot.jsearchgateway.model.dto.JSearchSearchRequest;
import com.jobseekercopilot.jsearchgateway.model.dto.JSearchSearchResponse;
import com.jobseekercopilot.generated.systemdataservice.api.FixtureControllerApi;
import com.jobseekercopilot.generated.systemdataservice.model.DemoJob;
import com.jobseekercopilot.generated.systemdataservice.model.FixtureJobSearchResponse;
import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "external-provider", name = "mode", havingValue = "FIXTURE")
public class FixtureJSearchProviderClient implements JSearchProviderClient {
    private static final Logger log = LoggerFactory.getLogger(FixtureJSearchProviderClient.class);
    private final FixtureProperties fixtureProperties;
    private final FixtureControllerApi fixtureControllerApi;

    public FixtureJSearchProviderClient(FixtureProperties fixtureProperties, FixtureControllerApi fixtureControllerApi) {
        this.fixtureProperties = fixtureProperties;
        this.fixtureControllerApi = fixtureControllerApi;
    }

    @Override
    public JSearchSearchResponse search(JSearchSearchRequest request) {
        FixtureJobSearchResponse body = fixtureControllerApi.searchJobs(
                fixtureProperties.getDatasetId(),
                fixtureProperties.getDatasetVersion(),
                fixtureProperties.getScenario(),
                "JSEARCH",
                request.getTargetRole(),
                Boolean.TRUE.equals(request.getRemoteOnly()) ? "Remote" : request.getLocation(),
                cursorPage(request.getCursor()),
                10,
                null,
                null,
                null,
                Boolean.TRUE.equals(request.getRemoteOnly()) ? "REMOTE" : null);
        JSearchSearchResponse response = new JSearchSearchResponse();
        response.setProvider("JSEARCH");
        response.setCursor(nextCursor(body));
        response.setJobs(body == null || body.getJobs() == null ? List.of() : body.getJobs().stream().map(this::toJob).toList());
        log.info("JSearch fixture search returned resultCount={} datasetId={} scenario={}",
                response.getJobs().size(), fixtureProperties.getDatasetId(), fixtureProperties.getScenario());
        return response;
    }

    private JSearchJob toJob(DemoJob source) {
        JSearchJob job = new JSearchJob();
        job.setExternalJobId(text(source.getExternalReference(), source.getId()));
        job.setTitle(source.getTitle());
        job.setCompanyName(text(source.getCompanyDisplayName(), source.getCompanyName()));
        job.setPublisher("JSearch Fixture");
        job.setDescription(source.getDescription());
        job.setEmploymentType(source.getEmploymentType());
        job.setPrimaryApplyUrl(text(source.getSourceUrl(), "https://fixtures.jobseekercopilot.local/jsearch/" + job.getExternalJobId()));
        job.setDirectApply(false);
        job.setLocationDisplayName(source.getLocationName());
        job.setCity(source.getLocationName());
        job.setState(source.getRegion());
        job.setCountry("GB");
        job.setLatitude(decimal(source.getLatitude()));
        job.setLongitude(decimal(source.getLongitude()));
        job.setSalaryMinimum(source.getSalaryMinimum());
        job.setSalaryMaximum(source.getSalaryMaximum());
        job.setSalaryCurrency(text(source.getSalaryCurrency(), "GBP"));
        job.setSalaryPeriod(text(source.getSalaryPeriod(), "YEAR"));
        job.setPostedAt(source.getDatePosted());
        job.setExpiresAt(source.getClosingDate());
        job.setRemote("REMOTE".equalsIgnoreCase(text(source.getRemoteType(), "")));
        JSearchApplyOption option = new JSearchApplyOption();
        option.setPublisher("JSearch Fixture");
        option.setApplyUrl(job.getPrimaryApplyUrl());
        option.setDirect(false);
        job.setApplyOptions(List.of(option));
        return job;
    }

    private int cursorPage(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(cursor.replace("fixture-page-", ""));
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private String nextCursor(FixtureJobSearchResponse body) {
        if (body == null) {
            return null;
        }
        int page = number(body.getPage());
        int pageSize = Math.max(1, number(body.getPageSize()));
        int total = number(body.getTotalResults());
        return ((page + 1) * pageSize) < total ? "fixture-page-" + (page + 1) : null;
    }

    private String text(String value, String fallback) {
        return value == null ? fallback : value;
    }

    private int number(Integer value) {
        return value == null ? 0 : value;
    }

    private BigDecimal decimal(Double value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }
}
