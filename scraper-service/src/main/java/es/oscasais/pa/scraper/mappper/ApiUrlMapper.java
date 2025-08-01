package es.oscasais.pa.scraper.mappper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.function.Function;

import es.oscasais.pa.scraper.dto.ApiUrlDTO;
import es.oscasais.pa.scraper.dto.UrlDTO;
import es.oscasais.pa.scraper.model.Url;

public class ApiUrlMapper {
  public static UrlDTO toUrlDTO(ApiUrlDTO apiDTO) {
    boolean hasChanged = apiDTO.lastChanged().isAfter(apiDTO.lastChecked());

    UrlDTO urlDTO = new UrlDTO();
    urlDTO.setHasChanged(hasChanged);
    urlDTO.setUrl(apiDTO.url());
    urlDTO.setUrl(apiDTO.url());
    return urlDTO;
  }
  
  public static UrlDTO toUrlDTO(Url url) {
      UrlDTO urlDTO = new UrlDTO();
      
      urlDTO.setId(url.getId().toString());
      urlDTO.setPriceMax(url.getPriceMax());
      urlDTO.setPriceMin(url.getPriceMin());
      urlDTO.setSelector(url.getSelector());
      urlDTO.setTypecheck(url.getTypeCheck());
      urlDTO.setUserId(url.getUserId().toString());
      
      return urlDTO;
  }

  public static List<UrlDTO> syncDTOs(List<UrlDTO> apiUrls, List<UrlDTO> urls) {
    Map<String, UrlDTO> targetByUrl = apiUrls.stream().collect(Collectors.toMap(UrlDTO::getUrl, Function.identity())); 

    for (UrlDTO src : urls) { UrlDTO trg = targetByUrl.get(src.getUrl());
      if (trg != null) {
        src.setHasChanged(trg.getHasChanged());
      }
    }

    return urls;
  }
}
