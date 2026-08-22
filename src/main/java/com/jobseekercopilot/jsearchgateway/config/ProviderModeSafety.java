package com.jobseekercopilot.jsearchgateway.config;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ProviderModeSafety implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(ProviderModeSafety.class);
    private final ExternalProviderProperties providerProperties;
    private final JSearchProperties jSearchProperties;
    private final FixtureProperties fixtureProperties;
    private final Environment environment;

    public ProviderModeSafety(
            ExternalProviderProperties providerProperties,
            JSearchProperties jSearchProperties,
            FixtureProperties fixtureProperties,
            Environment environment) {
        this.providerProperties = providerProperties;
        this.jSearchProperties = jSearchProperties;
        this.fixtureProperties = fixtureProperties;
        this.environment = environment;
    }
    @Override
    public void run(ApplicationArguments args) {
        boolean production = Arrays.stream(environment.getActiveProfiles()).anyMatch(profile -> profile.equalsIgnoreCase("prod") || profile.equalsIgnoreCase("production"));
        if (production && providerProperties.getMode() == ExternalProviderMode.FIXTURE) {
            throw new IllegalStateException("jsearch-gateway cannot start in FIXTURE mode with a production profile.");
        }
        boolean credentialConfigured = !blank(jSearchProperties.getApiKey());
        boolean liveEnabled = providerProperties.getMode() == ExternalProviderMode.LIVE
                && jSearchProperties.isEnabled();
        if (liveEnabled && !credentialConfigured) {
            throw new IllegalStateException(
                    "JSearch LIVE mode requires JSEARCH_API_KEY.");
        }
        log.info(
                "provider mode active gateway=jsearch-gateway mode={} datasetId={} datasetVersion={} scenario={} externalCallsEnabled={} credentialConfigured={}",
                providerProperties.getMode(),
                fixtureProperties.getDatasetId(),
                fixtureProperties.getDatasetVersion(),
                fixtureProperties.getScenario(),
                liveEnabled,
                credentialConfigured);
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
