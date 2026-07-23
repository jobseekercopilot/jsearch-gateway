package com.jobseekercopilot.jsearchgateway.model.dto;

import java.math.BigDecimal;
import java.util.List;

public class JSearchJob {
    private String externalJobId, title, companyName, publisher, description, employmentType, locationDisplayName, city, state, country, salaryCurrency, salaryPeriod, postedAt, expiresAt, primaryApplyUrl;
    private BigDecimal latitude, longitude;
    private Integer salaryMinimum, salaryMaximum;
    private Boolean remote, directApply;
    private List<JSearchApplyOption> applyOptions;
    public String getExternalJobId() { return externalJobId; }
    public void setExternalJobId(String externalJobId) { this.externalJobId = externalJobId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getEmploymentType() { return employmentType; }
    public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }
    public String getLocationDisplayName() { return locationDisplayName; }
    public void setLocationDisplayName(String locationDisplayName) { this.locationDisplayName = locationDisplayName; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getSalaryCurrency() { return salaryCurrency; }
    public void setSalaryCurrency(String salaryCurrency) { this.salaryCurrency = salaryCurrency; }
    public String getSalaryPeriod() { return salaryPeriod; }
    public void setSalaryPeriod(String salaryPeriod) { this.salaryPeriod = salaryPeriod; }
    public String getPostedAt() { return postedAt; }
    public void setPostedAt(String postedAt) { this.postedAt = postedAt; }
    public String getExpiresAt() { return expiresAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }
    public String getPrimaryApplyUrl() { return primaryApplyUrl; }
    public void setPrimaryApplyUrl(String primaryApplyUrl) { this.primaryApplyUrl = primaryApplyUrl; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public Integer getSalaryMinimum() { return salaryMinimum; }
    public void setSalaryMinimum(Integer salaryMinimum) { this.salaryMinimum = salaryMinimum; }
    public Integer getSalaryMaximum() { return salaryMaximum; }
    public void setSalaryMaximum(Integer salaryMaximum) { this.salaryMaximum = salaryMaximum; }
    public Boolean getRemote() { return remote; }
    public void setRemote(Boolean remote) { this.remote = remote; }
    public Boolean getDirectApply() { return directApply; }
    public void setDirectApply(Boolean directApply) { this.directApply = directApply; }
    public List<JSearchApplyOption> getApplyOptions() { return applyOptions; }
    public void setApplyOptions(List<JSearchApplyOption> applyOptions) { this.applyOptions = applyOptions; }
}
