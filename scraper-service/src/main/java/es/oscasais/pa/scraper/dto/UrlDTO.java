package es.oscasais.pa.scraper.dto;

import java.math.BigDecimal;

import es.oscasais.pa.scraper.model.Url.TypeCheck;

public class UrlDTO {

  private String id;

  private String userId;

  private String url;

  private String selector;

  private TypeCheck typecheck = TypeCheck.ANY;

  private BigDecimal priceMin;

  private BigDecimal priceMax;

  private Boolean hasChanged = false;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getSelector() {
    return selector;
  }

  public void setSelector(String selector) {
    this.selector = selector;
  }

  public TypeCheck getTypecheck() {
    return typecheck;
  }

  public void setTypecheck(TypeCheck typecheck) {
    this.typecheck = typecheck;
  }

  public BigDecimal getPriceMin() {
    return priceMin;
  }

  public void setPriceMin(BigDecimal priceMin) {
    this.priceMin = priceMin;
  }

  public BigDecimal getPriceMax() {
    return priceMax;
  }

  public void setPriceMax(BigDecimal priceMax) {
    this.priceMax = priceMax;
  }

  public Boolean getHasChanged() {
    return hasChanged;
  }

  public void setHasChanged(Boolean hasChanged) {
    this.hasChanged = hasChanged;
  }
}
