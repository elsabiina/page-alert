package es.oscasais.pa.scraper.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import es.oscasais.pa.scraper.dto.ApiUrlDTO;
import java.util.UUID;

@Service
public class ScraperApiRepository {
  private final WebClient webClient;

  private final String apiKey;

  @Autowired
  public ScraperApiRepository(@Value("${scraper.url}") String scraperUrl, @Value("${scraper.api.key}") String apiKey,
      WebClient.Builder webClientBuilder) {
    this.webClient = webClientBuilder.baseUrl(scraperUrl).build();
    this.apiKey = apiKey;
  }

  public List<ApiUrlDTO> getUrlsByUserId(UUID userId) {
    String userTag = userId.toString();

    return webClient.get()
        .uri("/watch?tag={userTag}", userTag)
        .header("x-api-key", apiKey)
        .retrieve()
        .bodyToFlux(ApiUrlDTO.class)
        .collectList()
        .block();
  }

  public List<ApiUrlDTO> getAllUrls() {
    return webClient.get()
        .uri("/watch")
        .header("x-api-key", apiKey)
        .retrieve()
        .bodyToFlux(ApiUrlDTO.class)
        .collectList()
        .block();
  }
}
