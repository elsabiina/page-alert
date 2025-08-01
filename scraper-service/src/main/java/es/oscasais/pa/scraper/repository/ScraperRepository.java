package es.oscasais.pa.scraper.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.oscasais.pa.scraper.dto.UrlDTO;
import es.oscasais.pa.scraper.model.Url;

@Repository
public interface ScraperRepository extends JpaRepository<Url, UUID> {

  List<UrlDTO> getUrlsByUserId(UUID id);
}
