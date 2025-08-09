package es.oscasais.pa.scraper.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.oscasais.pa.scraper.dto.UrlDTO;
import es.oscasais.pa.scraper.exception.GlobalExceptionHandler;
import es.oscasais.pa.scraper.service.ScraperService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/users/{userId}/urls")
@Tag(name = "Scraper", description = "API for managing watched urls")
public class UrlController {
  private static final Logger log = LoggerFactory.getLogger(
      GlobalExceptionHandler.class);
  private final ScraperService scraperService;

  public UrlController(ScraperService scraperService) {
    this.scraperService = scraperService;
  }

  @GetMapping
  @Operation(summary = "Get all watched urls")
  public ResponseEntity<List<UrlDTO>> getUrlsByEmail(@PathVariable String userId) {

    List<UrlDTO> urls = scraperService.getAllUrls(userId);
    return ResponseEntity.ok().body(urls);
  }

  @PostMapping
  @Operation(summary = "Create new user's watched urls")
  public ResponseEntity<UrlDTO> createUrlByUserId(@PathVariable String userId, @Valid @RequestBody UrlDTO urlDTO) {
    log.info(">>> userId={} | urlDTO={}", userId, urlDTO);
    UrlDTO urlWatched = scraperService.createUrl(userId, urlDTO);

    return ResponseEntity.ok().body(urlWatched);
  }

  @PutMapping("/{urlId}")
  @Operation(summary = "Update a watched url")
  public ResponseEntity<UrlDTO> updateUrl(@PathVariable String userId, @PathVariable String urlId,
      @Valid @RequestBody UrlDTO urlDTO) {
    UrlDTO urlWatched = scraperService.updateUrl(userId, urlDTO);
    return ResponseEntity.ok().body(urlWatched);
  }

  @DeleteMapping("/{urlId}")
  @Operation(summary = "Update a watched url")
  public ResponseEntity<Void> deleteUrl(@PathVariable String urlId) {
    scraperService.deleteUrl(urlId);

    return ResponseEntity.noContent().build();
  }
}
