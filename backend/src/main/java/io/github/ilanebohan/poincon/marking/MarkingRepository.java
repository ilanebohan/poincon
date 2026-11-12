package io.github.ilanebohan.poincon.marking;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

/**
 * Loads the marking datasets from {@code data/markings/*.yaml}.
 */
@Component
public class MarkingRepository {

    private static final String LOCATION = "classpath*:data/markings/*.yaml";

    private final Map<String, MarkingDataset> datasets = new LinkedHashMap<>();

    public MarkingRepository() {
        try {
            for (Resource resource : new PathMatchingResourcePatternResolver().getResources(LOCATION)) {
                try (InputStream input = resource.getInputStream()) {
                    MarkingDataset dataset = read(input);
                    datasets.put(dataset.model().id(), dataset);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read the marking datasets", e);
        }
    }

    public List<WeaponModel> models() {
        return datasets.values().stream().map(MarkingDataset::model).toList();
    }

    public Optional<MarkingDataset> find(String modelId) {
        return Optional.ofNullable(datasets.get(modelId));
    }

    static MarkingDataset read(InputStream input) {
        Map<String, Object> root = new Yaml(new SafeConstructor(new LoaderOptions())).load(input);
        Map<String, Object> model = map(root.get("model"));
        return new MarkingDataset(
                new WeaponModel(
                        text(model, "id"),
                        text(model, "name"),
                        text(model, "rgaBrand"),
                        text(model, "rgaModel"),
                        text(model, "defaultCaliber"),
                        text(model, "markingLocation")),
                list(root.get("sources")).stream().map(MarkingRepository::map).map(MarkingRepository::toSource).toList(),
                list(root.get("codes")).stream().map(MarkingRepository::map).map(MarkingRepository::toCode).toList());
    }

    private static Source toSource(Map<String, Object> source) {
        return new Source(
                text(source, "id"),
                text(source, "title"),
                text(source, "publisher"),
                text(source, "url"),
                text(source, "license"));
    }

    private static MarkingCode toCode(Map<String, Object> code) {
        return new MarkingCode(
                text(code, "code"),
                text(code, "manufacturer"),
                text(code, "location"),
                text(code, "country"),
                texts(code.get("rgaManufacturers")),
                texts(code.get("relatedRgaManufacturers")),
                text(code, "period"),
                texts(code.get("sources")));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object value) {
        return value == null ? Map.of() : (Map<String, Object>) value;
    }

    private static List<?> list(Object value) {
        return value == null ? List.of() : (List<?>) value;
    }

    private static List<String> texts(Object value) {
        return list(value).stream().map(String::valueOf).toList();
    }

    private static String text(Map<String, Object> values, String key) {
        Object value = values.get(key);
        return value == null ? null : String.valueOf(value);
    }
}
