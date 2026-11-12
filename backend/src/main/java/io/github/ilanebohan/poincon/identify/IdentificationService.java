package io.github.ilanebohan.poincon.identify;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;

import io.github.ilanebohan.poincon.marking.MarkingCode;
import io.github.ilanebohan.poincon.marking.MarkingDataset;
import io.github.ilanebohan.poincon.marking.MarkingRepository;
import io.github.ilanebohan.poincon.rga.RgaCatalog;
import io.github.ilanebohan.poincon.rga.RgaRecord;

@Service
public class IdentificationService {

    private final MarkingRepository markings;
    private final RgaCatalog rga;

    public IdentificationService(MarkingRepository markings, RgaCatalog rga) {
        this.markings = markings;
        this.rga = rga;
    }

    public MarkingDataset dataset(String modelId) {
        return markings.find(modelId)
                .orElseThrow(() -> new NotFoundException("No weapon model with id '%s'.".formatted(modelId)));
    }

    /**
     * @param caliber RGA caliber label; the model's default caliber when blank
     */
    public Identification identify(String modelId, String rawCode, String caliber) {
        MarkingDataset dataset = dataset(modelId);
        MarkingCode code = dataset.findCode(rawCode).orElseThrow(() -> new NotFoundException(
                "No code '%s' recorded for the %s.".formatted(rawCode.strip(), dataset.model().name())));
        String wantedCaliber = caliber == null || caliber.isBlank()
                ? dataset.model().defaultCaliber()
                : caliber.strip();

        List<RgaRecord> records = rga.forModel(modelId);
        return new Identification(
                dataset.model(),
                code,
                wantedCaliber,
                select(records, code.rgaManufacturers(), wantedCaliber),
                select(records, code.relatedRgaManufacturers(), wantedCaliber),
                records.stream()
                        .filter(record -> code.rgaManufacturers().contains(record.manufacturer()))
                        .map(RgaRecord::caliber)
                        .distinct()
                        .sorted()
                        .toList(),
                dataset.sourcesOf(code));
    }

    private static List<RgaRecord> select(List<RgaRecord> records, Collection<String> manufacturers, String caliber) {
        return records.stream()
                .filter(record -> manufacturers.contains(record.manufacturer()))
                .filter(record -> caliber.equalsIgnoreCase(record.caliber()))
                .toList();
    }
}
