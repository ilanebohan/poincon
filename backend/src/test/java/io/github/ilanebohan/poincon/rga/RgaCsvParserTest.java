package io.github.ilanebohan.poincon.rga;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.junit.jupiter.api.Test;

class RgaCsvParserTest {

    @Test
    void splitsPlainFields() {
        assertThat(RgaCsvParser.splitLine("BK887;EPAULE;CARABINE")).containsExactly("BK887", "EPAULE", "CARABINE");
    }

    @Test
    void keepsSeparatorsInsideQuotedFields() {
        assertThat(RgaCsvParser.splitLine("A;\"B;C\";D")).containsExactly("A", "B;C", "D");
    }

    @Test
    void readsDoubledQuotesAsOneQuote() {
        assertThat(RgaCsvParser.splitLine("\"CALIBRE 12/76 (3\"\")\";X")).containsExactly("CALIBRE 12/76 (3\")", "X");
    }

    @Test
    void keepsTrailingEmptyFields() {
        assertThat(RgaCsvParser.splitLine("A;;")).containsExactly("A", "", "");
    }

    @Test
    void mapsColumnsByHeaderName() throws IOException {
        String csv = """
                modele;referenceRGA;longueurArme;calibreCanonUn;classementFrancais;fabricant
                "KARABINER 98 K";BK887;1110;"8 X 57 IS";"C 1° b)";"WAFFENFABRIK STEYR DAIMLER PUCH (BNZ)"
                """;

        List<RgaRecord> records = RgaCsvParser.parse(new StringReader(csv));

        assertThat(records).hasSize(1);
        RgaRecord record = records.getFirst();
        assertThat(record.reference()).isEqualTo("BK887");
        assertThat(record.model()).isEqualTo("KARABINER 98 K");
        assertThat(record.overallLengthMm()).isEqualTo(1110);
        assertThat(record.caliber()).isEqualTo("8 X 57 IS");
        assertThat(record.frenchClass()).isEqualTo("C 1° b)");
        assertThat(record.manufacturer()).isEqualTo("WAFFENFABRIK STEYR DAIMLER PUCH (BNZ)");
    }

    @Test
    void leavesMissingOrEmptyValuesNull() throws IOException {
        List<RgaRecord> records = RgaCsvParser.parse(new StringReader("referenceRGA;longueurArme\nBK887;\n"));

        assertThat(records.getFirst().overallLengthMm()).isNull();
        assertThat(records.getFirst().caliber()).isNull();
    }

    @Test
    void returnsNothingForAnEmptyFile() throws IOException {
        assertThat(RgaCsvParser.parse(new StringReader(""))).isEmpty();
    }
}
