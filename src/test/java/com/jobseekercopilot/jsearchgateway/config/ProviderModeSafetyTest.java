package com.jobseekercopilot.jsearchgateway.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.mock.env.MockEnvironment;

@ExtendWith(OutputCaptureExtension.class)
class ProviderModeSafetyTest {

    @Test
    void fixtureIsTheSafeDefaultAndNeedsNoLiveCredential() {
        ExternalProviderProperties provider =
                new ExternalProviderProperties();
        JSearchProperties jSearch = jSearch(true, "");

        assertThat(provider.getMode()).isEqualTo(ExternalProviderMode.FIXTURE);
        assertThatCode(() -> safety(provider, jSearch, new MockEnvironment())
                        .run(null))
                .doesNotThrowAnyException();
    }

    @Test
    void enabledLiveModeFailsClosedWhenCredentialIsMissing() {
        ExternalProviderProperties provider =
                provider(ExternalProviderMode.LIVE);

        assertThatThrownBy(() -> safety(
                                provider,
                                jSearch(true, ""),
                                new MockEnvironment())
                        .run(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("JSearch LIVE mode requires JSEARCH_API_KEY.");
    }

    @Test
    void configuredLiveModeLogsOnlyCredentialState(
            CapturedOutput output) {
        ExternalProviderProperties provider =
                provider(ExternalProviderMode.LIVE);
        String privateApiKey = "private-live-api-key";

        safety(
                        provider,
                        jSearch(true, privateApiKey),
                        new MockEnvironment())
                .run(null);

        assertThat(output)
                .contains(
                        "mode=LIVE",
                        "externalCallsEnabled=true",
                        "credentialConfigured=true")
                .doesNotContain(privateApiKey);
    }

    @Test
    void disabledLiveProviderIsAnExplicitSafeKillSwitch() {
        assertThatCode(() -> safety(
                                provider(ExternalProviderMode.LIVE),
                                jSearch(false, ""),
                                new MockEnvironment())
                        .run(null))
                .doesNotThrowAnyException();
    }

    @Test
    void productionStillRejectsFixtureMode() {
        MockEnvironment environment =
                new MockEnvironment().withProperty(
                        "spring.profiles.active", "production");
        environment.setActiveProfiles("production");

        assertThatThrownBy(() -> safety(
                                provider(ExternalProviderMode.FIXTURE),
                                jSearch(true, ""),
                                environment)
                        .run(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(
                        "cannot start in FIXTURE mode with a production profile");
    }

    private ProviderModeSafety safety(
            ExternalProviderProperties provider,
            JSearchProperties jSearch,
            MockEnvironment environment) {
        FixtureProperties fixture = new FixtureProperties();
        fixture.setDatasetId("synthetic-dataset");
        fixture.setDatasetVersion("1.0");
        fixture.setScenario("DEMO_READY");
        return new ProviderModeSafety(
                provider, jSearch, fixture, environment);
    }

    private ExternalProviderProperties provider(ExternalProviderMode mode) {
        ExternalProviderProperties provider =
                new ExternalProviderProperties();
        provider.setMode(mode);
        return provider;
    }

    private JSearchProperties jSearch(
            boolean enabled, String apiKey) {
        JSearchProperties jSearch = new JSearchProperties();
        jSearch.setEnabled(enabled);
        jSearch.setApiKey(apiKey);
        return jSearch;
    }
}
