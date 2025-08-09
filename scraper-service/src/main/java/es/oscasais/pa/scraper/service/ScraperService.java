package es.oscasais.pa.scraper.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import es.oscasais.pa.scraper.dto.ApiUrlDTO;
import es.oscasais.pa.scraper.dto.UrlDTO;
import es.oscasais.pa.scraper.mapper.ApiUrlMapper;
import es.oscasais.pa.scraper.mapper.UrlMapper;
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
  public List<UrlDTO> getAllUrls(String userId) {
    UUID id = UUID.fromString(userId);
    List<ApiUrlDTO> urls = apiRepo.getUrlsByUserId(id);
    List<UrlDTO> apiUrls = urls.stream()
        .map(ApiUrlMapper::toUrlDTO)
        .toList();

    List<UrlDTO> paUrls = repo.getByUserId(id).stream()
        .map(UrlMapper::toDTO)
        .toList();

    return UrlMapper.syncDTOs(apiUrls, paUrls);
  }

  public UrlDTO createUrl(String userId, UrlDTO url) {

    url.setUserId(userId);

    Url urlWatched = repo.save(UrlMapper.toModel(url));

    return UrlMapper.toDTO(urlWatched);
  }

  public UrlDTO updateUrl(String userId, UrlDTO url) {

    url.setUserId(userId);
  
    Url urlWatched = repo.save(UrlMapper.toModel(url));

    return UrlMapper.toDTO(urlWatched);
  }

  public void deleteUrl(String urlId) {
    UUID id = UUID.fromString(urlId);

    repo.deleteById(id);
  }
}
