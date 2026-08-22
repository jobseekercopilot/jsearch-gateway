package com.jobseekercopilot.jsearchgateway.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jobseekercopilot.jsearchgateway.client.JSearchProviderClient;
import com.jobseekercopilot.jsearchgateway.model.dto.JSearchJob;
import com.jobseekercopilot.jsearchgateway.model.dto.JSearchSearchRequest;
import com.jobseekercopilot.jsearchgateway.model.dto.JSearchSearchResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class JSearchControllerTest {

    @Test
    void exposesStableGatewaySearchContract() throws Exception {
        JSearchProviderClient provider = mock(JSearchProviderClient.class);
        JSearchJob job = new JSearchJob();
        job.setExternalJobId("js-1");
        job.setTitle("Engineer");
        JSearchSearchResponse response = new JSearchSearchResponse();
        response.setCursor("next-cursor");
        response.setJobs(List.of(job));
        when(provider.search(any())).thenReturn(response);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(
                new JSearchController(provider)).build();

        mvc.perform(post("/api/v1/jsearch/jobs/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "targetRole": "Engineer",
                                  "location": "London",
                                  "remoteOnly": true,
                                  "cursor": "current-cursor"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.provider").value("JSEARCH"))
                .andExpect(jsonPath("$.cursor").value("next-cursor"))
                .andExpect(jsonPath("$.jobs[0].externalJobId")
                        .value("js-1"))
                .andExpect(jsonPath("$.jobs[0].title")
                        .value("Engineer"));

        ArgumentCaptor<JSearchSearchRequest> request =
                ArgumentCaptor.forClass(JSearchSearchRequest.class);
        verify(provider).search(request.capture());
        org.assertj.core.api.Assertions.assertThat(
                request.getValue().getTargetRole()).isEqualTo("Engineer");
        org.assertj.core.api.Assertions.assertThat(
                request.getValue().getRemoteOnly()).isTrue();
        org.assertj.core.api.Assertions.assertThat(
                request.getValue().getCursor())
                .isEqualTo("current-cursor");
    }
}
