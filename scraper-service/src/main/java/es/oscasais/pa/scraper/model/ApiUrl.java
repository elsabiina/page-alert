package es.oscasais.pa.scraper.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ApiUrl {
  @JsonProperty("last_changed")
  private Long lastChanged;

  @JsonProperty("last_checked")
  private Long lastChecked;

  @JsonProperty("last_error")
  private Boolean lastError;

  private String title;

  private String url;

  private Boolean viewed;

  public Long getLastChanged() {
    return lastChanged;
  }

  public void setLastChanged(Long lastChanged) {
    this.lastChanged = lastChanged;
  }

  public Long getLastChecked() {
    return lastChecked;
  }

  public void setLastChecked(Long lastChecked) {
    this.lastChecked = lastChecked;
  }

  public Boolean getLastError() {
    return lastError;
  }

  public void setLastError(Boolean lastError) {
    this.lastError = lastError;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public Boolean getViewed() {
    return viewed;
  }

  public void setViewed(Boolean viewed) {
    this.viewed = viewed;
  }
}
