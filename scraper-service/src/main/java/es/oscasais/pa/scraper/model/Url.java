package es.oscasais.pa.scraper.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "urls")
public class Url {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "url", nullable = false, unique = true)
  private String url;

  @Column(name = "body_hash")
  private String bodyHash;

  @Column(name = "selector")
  private String selector;

  @Enumerated(EnumType.STRING)
  @Column(name = "type_check", nullable = false, length = 20)
  private TypeCheck typeCheck = TypeCheck.ANY;

  @Column(name = "price_min", precision = 12, scale = 2)
  private BigDecimal priceMin;

  @Column(name = "price_max", precision = 12, scale = 2)
  private BigDecimal priceMax;

  @Column(name = "has_changed", nullable = false)
  private Boolean hasChanged = false;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false, updatable = false, insertable = false)
  private OffsetDateTime updatedAt;

  // Getters y Setters
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getUserId() {
    return userId;
  }

  public void setUserId(UUID userId) {
    this.userId = userId;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getBodyHash() {
    return bodyHash;
  }

  public void setBodyHash(String bodyHash) {
    this.bodyHash = bodyHash;
  }

  public String getSelector() {
    return selector;
  }

  public void setSelector(String selector) {
    this.selector = selector;
  }

  public TypeCheck getTypeCheck() {
    return typeCheck;
  }

  public void setTypeCheck(TypeCheck typeCheck) {
    this.typeCheck = typeCheck;
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

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  // Enum para type_check
  public enum TypeCheck {
    ANY,
    PAGE_SECTION,
    PRICE
  }
}
