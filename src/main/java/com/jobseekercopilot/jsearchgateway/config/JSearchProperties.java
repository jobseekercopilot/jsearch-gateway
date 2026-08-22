package com.jobseekercopilot.jsearchgateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jsearch")
public class JSearchProperties {
    public static final int DEFAULT_PAGES_PER_SEARCH = 1;
    public static final int MAX_PAGES_PER_SEARCH = 2;

    private String baseUrl;
    private String apiKey;
    private String country;
    private String language;
    private int pagesPerSearch = DEFAULT_PAGES_PER_SEARCH;
    private boolean enabled;
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public int getPagesPerSearch() { return pagesPerSearch; }
    public void setPagesPerSearch(int pagesPerSearch) {
        this.pagesPerSearch = Math.max(
                DEFAULT_PAGES_PER_SEARCH,
                Math.min(pagesPerSearch, MAX_PAGES_PER_SEARCH));
    }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
