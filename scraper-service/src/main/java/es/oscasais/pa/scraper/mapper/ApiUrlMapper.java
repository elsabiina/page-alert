package es.oscasais.pa.scraper.mapper;

import es.oscasais.pa.scraper.dto.ApiUrlDTO;
import es.oscasais.pa.scraper.dto.UrlDTO;

public class ApiUrlMapper {
  public static UrlDTO toUrlDTO(ApiUrlDTO apiDTO) {
    boolean hasChanged = false;

    if (apiDTO.lastChanged() != null && apiDTO.lastChecked() != null) {
      hasChanged = apiDTO.lastChanged().isAfter(apiDTO.lastChecked());
    }

    UrlDTO urlDTO = new UrlDTO();
    urlDTO.setHasChanged(hasChanged);
    urlDTO.setUrl(apiDTO.url());
    urlDTO.setUrl(apiDTO.url());
    return urlDTO;
  }
}
