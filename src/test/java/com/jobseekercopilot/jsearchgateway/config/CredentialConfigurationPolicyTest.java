package com.jobseekercopilot.jsearchgateway.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class CredentialConfigurationPolicyTest {

    private static final Pattern NON_EMPTY_DEFAULT = Pattern.compile(
            "\\$\\{JSEARCH_API_KEY:[^}\\s]+}");

    @Test
    void sourceConfigurationHasOnlyAnEmptyCredentialFallback() throws Exception {
        String configuration = Files.readString(
                Path.of("src/main/resources/application.yml"));

        assertThat(configuration)
                .contains(
                        "api-key: ${JSEARCH_API_KEY:}",
                        "mode: ${EXTERNAL_PROVIDER_MODE:FIXTURE}");
        assertThat(NON_EMPTY_DEFAULT.matcher(configuration).find()).isFalse();
    }

    @Test
    void credentialRunbookDefinesRotationIncidentAndEvidenceBoundaries()
            throws Exception {
        String runbook = Files.readString(
                Path.of("docs/CREDENTIAL_OPERATIONS.md"));

        assertThat(runbook)
                .contains(
                        "Revoke the previously exposed",
                        "approved secret manager",
                        "restricted security record",
                        "Routine renewal",
                        "Logging and support",
                        "complete authenticated",
                        "clone:",
                        "scans all reachable",
                        "Remaining external closure evidence",
                        "JSEARCH-01 cannot be closed");
        assertThat(runbook)
                .doesNotContain(
                        "sk_live_",
                        "Bearer ey",
                        "password=secret");
    }
}
