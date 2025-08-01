package es.oscasais.pa.scraper.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import es.oscasais.pa.scraper.dto.ApiUrlDTO;
import es.oscasais.pa.scraper.dto.UrlDTO;
import es.oscasais.pa.scraper.mappper.ApiUrlMapper;
import es.oscasais.pa.scraper.model.Url;
import es.oscasais.pa.scraper.repository.ScraperApiRepository;
import es.oscasais.pa.scraper.repository.ScraperRepository;
import jakarta.transaction.Transactional;
import java.util.UUID;

@Service
public class ScraperService {
  @Autowired
  private final ScraperRepository repo;
  @Autowired
  private final ScraperApiRepository apiRepo;

  public ScraperService(ScraperRepository repo, ScraperApiRepository apiRepo) {
    this.repo = repo;
    this.apiRepo = apiRepo;
  }

  @Transactional
  public List<UrlDTO> getAllUrls() {
    List<ApiUrlDTO> urls = apiRepo.getAllUrls();
    List<UrlDTO> apiUrls = urls.stream()
      .map(ApiUrlMapper::toUrlDTO)
      .toList();

    List<UrlDTO> paUrls = repo.findAll().stream()
      .map(ApiUrlMapper::toUrlDTO)
      .toList();
    
    return ApiUrlMapper.syncDTOs(apiUrls, paUrls);
  }

  @Transactional
  public List<UrlDTO> getUrlsByUserId(UUID userId) {
    List<ApiUrlDTO> urls = apiRepo.getUrlsByUserId(userId);

    List<UrlDTO> apiUrls = urls.stream()
      .map(ApiUrlMapper::toUrlDTO)
      .toList();

    return ApiUrlMapper.syncDTOs(apiUrls, repo.getUrlsByUserId(userId));
  }
}
