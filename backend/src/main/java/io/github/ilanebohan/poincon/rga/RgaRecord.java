package io.github.ilanebohan.poincon.rga;

/**
 * One record of the French RGA (Référentiel Général des Armes).
 * Text values are kept exactly as the ministry publishes them.
 */
public record RgaRecord(
        String reference,
        String family,
        String type,
        String brand,
        String model,
        String manufacturer,
        String country,
        String action,
        String feeding,
        Integer overallLengthMm,
        Integer magazineCapacity,
        Integer chamberCapacity,
        String caliber,
        Integer barrelLengthMm,
        String frenchClass,
        String europeanClass) {
}
