package com.jobseekercopilot.jsearchgateway.client;

import com.jobseekercopilot.jsearchgateway.model.dto.JSearchSearchRequest;
import com.jobseekercopilot.jsearchgateway.model.dto.JSearchSearchResponse;

public interface JSearchProviderClient {
    JSearchSearchResponse search(JSearchSearchRequest request);
}
