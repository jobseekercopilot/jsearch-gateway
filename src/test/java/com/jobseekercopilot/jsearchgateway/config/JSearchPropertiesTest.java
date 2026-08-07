package com.jobseekercopilot.jsearchgateway.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class JSearchPropertiesTest {

    @Test
    void defaultsToOneProviderPagePerGatewaySearch() {
        assertThat(new JSearchProperties().getPagesPerSearch())
                .isEqualTo(JSearchProperties.DEFAULT_PAGES_PER_SEARCH);
    }

    @ParameterizedTest
    @CsvSource({
        "-100, 1",
        "0, 1",
        "1, 1",
        "2, 2",
        "3, 2",
        "2147483647, 2"
    })
    void clampsConfiguredProviderPageBudget(
            int configured,
            int expected) {
        JSearchProperties properties = new JSearchProperties();

        properties.setPagesPerSearch(configured);

        assertThat(properties.getPagesPerSearch()).isEqualTo(expected);
    }
}
