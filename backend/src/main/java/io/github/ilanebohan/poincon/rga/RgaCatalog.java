package io.github.ilanebohan.poincon.rga;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

/**
 * The RGA records kept in the repository, grouped by the weapon model they belong to.
 * Each file {@code data/rga/<model-id>.csv} holds the records of one model.
 */
@Component
public class RgaCatalog {

    private static final String LOCATION = "classpath*:data/rga/*.csv";

    private final Map<String, List<RgaRecord>> recordsByModel = new HashMap<>();
    private final Map<String, RgaRecord> recordsByReference = new HashMap<>();

    public RgaCatalog() {
        try {
            for (Resource resource : new PathMatchingResourcePatternResolver().getResources(LOCATION)) {
                String modelId = resource.getFilename().replaceFirst("\\.csv$", "");
                try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                    List<RgaRecord> records = List.copyOf(RgaCsvParser.parse(reader));
                    recordsByModel.put(modelId, records);
                    records.forEach(record -> recordsByReference.put(record.reference(), record));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read the RGA extracts", e);
        }
    }

    public List<RgaRecord> forModel(String modelId) {
        return recordsByModel.getOrDefault(modelId, List.of());
    }

    public Optional<RgaRecord> findByReference(String reference) {
        return Optional.ofNullable(recordsByReference.get(reference.strip().toUpperCase()));
    }
}
