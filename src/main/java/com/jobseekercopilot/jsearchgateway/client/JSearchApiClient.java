package com.jobseekercopilot.jsearchgateway.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.jobseekercopilot.jsearchgateway.config.JSearchProperties;
import com.jobseekercopilot.jsearchgateway.logging.CorrelationIdFilter;
import com.jobseekercopilot.jsearchgateway.model.dto.JSearchApplyOption;
import com.jobseekercopilot.jsearchgateway.model.dto.JSearchJob;
import com.jobseekercopilot.jsearchgateway.model.dto.JSearchSearchRequest;
import com.jobseekercopilot.jsearchgateway.model.dto.JSearchSearchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.util.ArrayList;

@Component
@ConditionalOnProperty(prefix = "external-provider", name = "mode", havingValue = "LIVE", matchIfMissing = true)
public class JSearchApiClient implements JSearchProviderClient {
    private static final Logger log = LoggerFactory.getLogger(JSearchApiClient.class);

    private final JSearchProperties properties;
    private final WebClient webClient;

    public JSearchApiClient(JSearchProperties properties) {
        this.properties = properties;
        this.webClient = WebClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .filter((request, next) -> {
                    String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
                    if (StringUtils.hasText(correlationId)) {
                        return next.exchange(ClientRequest.from(request)
                                .header(CorrelationIdFilter.HEADER_NAME, correlationId)
                                .build());
                    }
                    return next.exchange(request);
                })
                .build();
    }

    @Override
    public JSearchSearchResponse search(JSearchSearchRequest request) {
        if (!properties.isEnabled() || blank(properties.getApiKey())) {
            log.warn("JSearch provider disabled or credentials missing enabled={} hasApiKey={}",
                    properties.isEnabled(),
                    !blank(properties.getApiKey()));
            return empty();
        }
        long startedAt = System.nanoTime();
        log.info("JSearch provider request started targetRole={} location={} remoteOnly={} pages={}",
                request.getTargetRole(),
                request.getLocation(),
                request.getRemoteOnly(),
                properties.getPagesPerSearch());
        try {
            JSearchSearchResponse response = empty();
            String cursor = request.getCursor();
            for (int page = 0; page < Math.max(1, properties.getPagesPerSearch()); page++) {
                String currentCursor = cursor;
                JsonNode body = webClient.get()
                        .uri(uriBuilder -> {
                            var builder = uriBuilder.path("/search-v2")
                                    .queryParam("query", query(request))
                                    .queryParam("country", properties.getCountry())
                                    .queryParam("language", properties.getLanguage());
                            if (Boolean.TRUE.equals(request.getRemoteOnly())) {
                                builder.queryParam("work_from_home", "true");
                            }
                            if (!blank(currentCursor)) {
                                builder.queryParam("cursor", currentCursor);
                            }
                            return builder.build();
                        })
                        .header("X-API-KEY", properties.getApiKey())
                        .retrieve()
                        .bodyToMono(JsonNode.class)
                        .block();
                if (body == null) {
                    continue;
                }
                cursor = firstText(body, "cursor", "next_cursor");
                if (blank(cursor)) {
                    cursor = firstText(body.path("data"), "cursor", "next_cursor");
                }
                response.setCursor(cursor);
                jobsNode(body).forEach(node -> {
                    JSearchJob job = toJob(node);
                    if (hasSearchableContent(job)) {
                        response.getJobs().add(job);
                    }
                });
                if (blank(cursor)) {
                    break;
                }
            }
            log.info("JSearch provider returned status=200 resultCount={} hasCursor={} durationMs={}",
                    response.getJobs().size(),
                    !blank(response.getCursor()),
                    (System.nanoTime() - startedAt) / 1_000_000);
            return response;
        } catch (WebClientResponseException.TooManyRequests ex) {
            log.warn("JSearch provider rate limited status={} durationMs={}",
                    ex.getStatusCode().value(),
                    (System.nanoTime() - startedAt) / 1_000_000);
            throw new ProviderUnavailableException("JSearch rate limit exceeded", ex);
        } catch (WebClientResponseException ex) {
            log.warn("JSearch provider failed status={} durationMs={} error={}",
                    ex.getStatusCode().value(),
                    (System.nanoTime() - startedAt) / 1_000_000,
                    ex.getClass().getSimpleName(),
                    ex);
            throw new ProviderUnavailableException("JSearch API request failed", ex);
        } catch (RuntimeException ex) {
            log.warn("JSearch provider failed durationMs={} error={}",
                    (System.nanoTime() - startedAt) / 1_000_000,
                    ex.getClass().getSimpleName(),
                    ex);
            throw new ProviderUnavailableException("JSearch API request failed", ex);
        }
    }

    private JSearchSearchResponse empty() {
        JSearchSearchResponse response = new JSearchSearchResponse();
        response.setJobs(new ArrayList<>());
        return response;
    }

    private JsonNode jobsNode(JsonNode body) {
        JsonNode data = body.path("data");
        if (data.isArray()) {
            return data;
        }
        if (data.path("jobs").isArray()) {
            return data.path("jobs");
        }
        if (body.path("jobs").isArray()) {
            return body.path("jobs");
        }
        return JsonNodeFactory.instance.arrayNode();
    }

    private boolean hasSearchableContent(JSearchJob job) {
        return !blank(job.getExternalJobId())
                || !blank(job.getTitle())
                || !blank(job.getCompanyName())
                || !blank(job.getPrimaryApplyUrl());
    }

    private String query(JSearchSearchRequest request) {
        return request.getTargetRole() + (blank(request.getLocation()) ? "" : " in " + request.getLocation());
    }

    private JSearchJob toJob(JsonNode node) {
        JSearchJob job = new JSearchJob();
        job.setExternalJobId(text(node, "job_id"));
        job.setTitle(text(node, "job_title"));
        job.setCompanyName(text(node, "employer_name"));
        job.setPublisher(text(node, "job_publisher"));
        job.setDescription(text(node, "job_description"));
        job.setEmploymentType(firstText(node, "job_employment_type", "job_employment_types"));
        job.setPrimaryApplyUrl(text(node, "job_apply_link"));
        job.setDirectApply(booleanValue(node, "job_apply_is_direct"));
        job.setLocationDisplayName(firstText(node, "job_location", "job_city"));
        job.setCity(text(node, "job_city"));
        job.setState(text(node, "job_state"));
        job.setCountry(text(node, "job_country"));
        job.setLatitude(decimal(node, "job_latitude"));
        job.setLongitude(decimal(node, "job_longitude"));
        job.setSalaryMinimum(integer(node, "job_min_salary"));
        job.setSalaryMaximum(integer(node, "job_max_salary"));
        job.setSalaryCurrency(text(node, "job_salary_currency"));
        job.setSalaryPeriod(text(node, "job_salary_period"));
        job.setPostedAt(text(node, "job_posted_at_datetime_utc"));
        job.setExpiresAt(text(node, "job_offer_expiration_datetime_utc"));
        job.setRemote(booleanValue(node, "job_is_remote"));
        var options = new ArrayList<JSearchApplyOption>();
        node.path("apply_options").forEach(optionNode -> {
            JSearchApplyOption option = new JSearchApplyOption();
            option.setPublisher(text(optionNode, "publisher"));
            option.setApplyUrl(text(optionNode, "apply_link"));
            option.setDirect(booleanValue(optionNode, "is_direct"));
            options.add(option);
        });
        job.setApplyOptions(options);
        return job;
    }

    private String firstText(JsonNode node, String first, String second) {
        String value = text(node, first);
        if (!blank(value)) {
            return value;
        }
        JsonNode secondNode = node.path(second);
        if (secondNode.isArray() && secondNode.size() > 0) {
            return secondNode.get(0).asText();
        }
        return text(node, second);
    }

    private String text(JsonNode node, String field) {
        return node == null || node.path(field).isMissingNode() || node.path(field).isNull() ? null : node.path(field).asText();
    }

    private Integer integer(JsonNode node, String field) {
        return node.path(field).isNumber() ? node.path(field).asInt() : null;
    }

    private BigDecimal decimal(JsonNode node, String field) {
        return node.path(field).isNumber() ? node.path(field).decimalValue() : null;
    }

    private Boolean booleanValue(JsonNode node, String field) {
        return node.path(field).isBoolean()
                ? node.path(field).booleanValue()
                : null;
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    public static class ProviderUnavailableException extends RuntimeException {
        public ProviderUnavailableException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
