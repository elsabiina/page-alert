package es.oscasais.pa.scraper.mapper;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import es.oscasais.pa.scraper.dto.UrlDTO;
import es.oscasais.pa.scraper.model.Url;

public class UrlMapper {
  public static UrlDTO toDTO(Url url) {
    UrlDTO urlDTO = new UrlDTO();

    urlDTO.setId(url.getId().toString());
    urlDTO.setUserId(url.getUserId().toString());
    urlDTO.setUrl(url.getUrl());
    urlDTO.setPriceMax(url.getPriceMax());
    urlDTO.setPriceMin(url.getPriceMin());
    urlDTO.setSelector(url.getSelector());
    urlDTO.setTypecheck(url.getTypeCheck());

    return urlDTO;
  }

  public static List<UrlDTO> syncDTOs(List<UrlDTO> apiUrls, List<UrlDTO> urls) {
    Map<String, UrlDTO> targetByUrl = apiUrls.stream().collect(Collectors.toMap(UrlDTO::getUrl, Function.identity()));

    for (UrlDTO src : urls) {
      UrlDTO trg = targetByUrl.get(src.getUrl());
      if (trg != null) {
        src.setHasChanged(trg.getHasChanged());
      }
    }

    return urls;
  }

  public static Url toModel(UrlDTO urlDTO) {
    Url url = new Url();

    if(urlDTO.getId() != null) {
      url.setId(UUID.fromString(urlDTO.getId()));
    }
    if(urlDTO.getUserId() != null) {
      url.setUserId(UUID.fromString(urlDTO.getUserId()));
    }

    url.setUrl(urlDTO.getUrl());
    url.setPriceMax(urlDTO.getPriceMax());
    url.setPriceMin(urlDTO.getPriceMin());
    url.setTypeCheck(urlDTO.getTypecheck());
    url.setSelector(urlDTO.getSelector());

    return url;
  }
}
