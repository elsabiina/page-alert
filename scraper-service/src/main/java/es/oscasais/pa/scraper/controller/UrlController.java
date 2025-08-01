package es.oscasais.pa.scraper.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.oscasais.pa.scraper.dto.UrlDTO;

import es.oscasais.pa.scraper.service.ScraperService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;

@RestController
@RequestMapping("/urls")
@Tag(name = "Scraper", description = "API for managing watched urls")
public class UrlController {
  private final ScraperService scraperService;

  public UrlController(ScraperService scraperService) {
    this.scraperService = scraperService;
  }

  @GetMapping
  @Operation(summary = "Get all watched urls from all users")
  public ResponseEntity<List<UrlDTO>> getUrlsByEmail() {
    List<UrlDTO> urls = scraperService.getAllUrls();

    return ResponseEntity.ok().body(urls);
  }

  @GetMapping("/{email}")
  @Operation(summary = "Get user watched urls")
  public ResponseEntity<List<UrlDTO>> getUrlsByUserId(String userId) {
    UUID userUUID = UUID.fromString(userId);
    
    List<UrlDTO> urls = scraperService.getUrlsByUserId(userUUID);

    return ResponseEntity.ok().body(urls);
  }
}
