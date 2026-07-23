package com.jobseekercopilot.jsearchgateway.controller;

import com.jobseekercopilot.jsearchgateway.client.JSearchProviderClient;
import com.jobseekercopilot.jsearchgateway.model.dto.JSearchSearchRequest;
import com.jobseekercopilot.jsearchgateway.model.dto.JSearchSearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/jsearch/jobs")
@Tag(name = "JSearch Jobs")
public class JSearchController {
    private final JSearchProviderClient jSearchApiClient;

    public JSearchController(JSearchProviderClient jSearchApiClient) {
        this.jSearchApiClient = jSearchApiClient;
    }

    @PostMapping("/search")
    @Operation(summary = "Search JSearch jobs for job-service")
    public ResponseEntity<JSearchSearchResponse> search(@RequestBody JSearchSearchRequest request) {
        return ResponseEntity.ok(jSearchApiClient.search(request));
    }
}
