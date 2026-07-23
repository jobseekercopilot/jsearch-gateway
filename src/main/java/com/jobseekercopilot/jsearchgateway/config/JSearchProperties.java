package com.jobseekercopilot.jsearchgateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jsearch")
public class JSearchProperties {
    private String baseUrl;
    private String apiKey;
    private String country;
    private String language;
    private int pagesPerSearch;
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
    public void setPagesPerSearch(int pagesPerSearch) { this.pagesPerSearch = pagesPerSearch; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
