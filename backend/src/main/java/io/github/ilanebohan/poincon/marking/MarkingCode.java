package io.github.ilanebohan.poincon.marking;

import java.util.List;

/**
 * A manufacturer code as stamped on a weapon, with the RGA manufacturer labels it maps to.
 */
public record MarkingCode(
        String code,
        String manufacturer,
        String location,
        String country,
        List<String> rgaManufacturers,
        List<String> relatedRgaManufacturers,
        String period,
        List<String> sources) {
}
