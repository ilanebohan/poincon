package io.github.ilanebohan.poincon.marking;

import java.util.List;
import java.util.Optional;

/**
 * Everything the project knows about the markings of one weapon model.
 */
public record MarkingDataset(WeaponModel model, List<Source> sources, List<MarkingCode> codes) {

    public Optional<MarkingCode> findCode(String rawCode) {
        String wanted = normalize(rawCode);
        return codes.stream().filter(code -> normalize(code.code()).equals(wanted)).findFirst();
    }

    public List<Source> sourcesOf(MarkingCode code) {
        return sources.stream().filter(source -> code.sources().contains(source.id())).toList();
    }

    private static String normalize(String code) {
        return code == null ? "" : code.strip().toLowerCase().replace(" ", "");
    }
}
