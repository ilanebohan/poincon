package io.github.ilanebohan.poincon.rga;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads the semicolon-separated export of the RGA.
 * Fields may be wrapped in double quotes, with {@code ""} standing for a literal quote.
 */
public final class RgaCsvParser {

    private static final char SEPARATOR = ';';
    private static final char QUOTE = '"';

    private RgaCsvParser() {
    }

    public static List<RgaRecord> parse(Reader source) throws IOException {
        BufferedReader reader = new BufferedReader(source);
        String headerLine = reader.readLine();
        if (headerLine == null) {
            return List.of();
        }
        Map<String, Integer> columns = indexColumns(splitLine(headerLine));

        List<RgaRecord> records = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.isBlank()) {
                records.add(toRecord(splitLine(line), columns));
            }
        }
        return records;
    }

    static List<String> splitLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (quoted) {
                if (c != QUOTE) {
                    current.append(c);
                } else if (i + 1 < line.length() && line.charAt(i + 1) == QUOTE) {
                    current.append(QUOTE);
                    i++;
                } else {
                    quoted = false;
                }
            } else if (c == QUOTE) {
                quoted = true;
            } else if (c == SEPARATOR) {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());
        return fields;
    }

    private static Map<String, Integer> indexColumns(List<String> header) {
        Map<String, Integer> columns = new HashMap<>();
        for (int i = 0; i < header.size(); i++) {
            columns.put(header.get(i).strip(), i);
        }
        return columns;
    }

    private static RgaRecord toRecord(List<String> fields, Map<String, Integer> columns) {
        return new RgaRecord(
                text(fields, columns, "referenceRGA"),
                text(fields, columns, "famille"),
                text(fields, columns, "typeArme"),
                text(fields, columns, "marque"),
                text(fields, columns, "modele"),
                text(fields, columns, "fabricant"),
                text(fields, columns, "paysFabricant"),
                text(fields, columns, "modeFonctionnement"),
                text(fields, columns, "systemeAlimentation"),
                number(fields, columns, "longueurArme"),
                number(fields, columns, "capaciteHorsChambre"),
                number(fields, columns, "capaciteChambre"),
                text(fields, columns, "calibreCanonUn"),
                number(fields, columns, "longueurCanonUn"),
                text(fields, columns, "classementFrancais"),
                text(fields, columns, "classementEuropeen"));
    }

    private static String text(List<String> fields, Map<String, Integer> columns, String column) {
        Integer index = columns.get(column);
        if (index == null || index >= fields.size()) {
            return null;
        }
        String value = fields.get(index).strip();
        return value.isEmpty() ? null : value;
    }

    private static Integer number(List<String> fields, Map<String, Integer> columns, String column) {
        String value = text(fields, columns, column);
        if (value == null) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
