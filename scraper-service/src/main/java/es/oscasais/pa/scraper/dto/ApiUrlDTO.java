package es.oscasais.pa.scraper.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonFormat;

public record ApiUrlDTO(
    String title,
    String url,
    boolean lastError,
    boolean viewed,
    @JsonFormat(shape = JsonFormat.Shape.NUMBER) Instant lastChecked,
    @JsonFormat(shape = JsonFormat.Shape.NUMBER) Instant lastChanged) {
}
