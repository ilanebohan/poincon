package io.github.ilanebohan.poincon.identify;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import io.github.ilanebohan.poincon.marking.MarkingRepository;
import io.github.ilanebohan.poincon.rga.RgaCatalog;
import io.github.ilanebohan.poincon.rga.RgaRecord;

/**
 * Runs against the real datasets of the repository, so a broken data file fails the build.
 */
class IdentificationServiceTest {

    private static final String K98K = "mauser-k98k";

    private final IdentificationService service = new IdentificationService(new MarkingRepository(), new RgaCatalog());

    @Test
    void findsTheRgaRecordOfACodeInTheDefaultCaliber() {
        Identification result = service.identify(K98K, "bnz", null);

        assertThat(result.code().manufacturer()).isEqualTo("Steyr-Daimler-Puch");
        assertThat(result.caliber()).isEqualTo("8 X 57 IS");
        assertThat(result.matches()).extracting(RgaRecord::reference).containsExactly("BK887");
        assertThat(result.matches().getFirst().frenchClass()).isEqualTo("C 1° b)");
    }

    @Test
    void warnsAboutTheRecordOfTheSameMakerListedWithoutTheCode() {
        Identification result = service.identify(K98K, "bnz", null);

        assertThat(result.similar()).extracting(RgaRecord::reference).contains("AJ639");
    }

    @Test
    void ignoresCaseAndSurroundingSpaces() {
        assertThat(service.identify(K98K, "  BNZ ", null).matches())
                .extracting(RgaRecord::reference)
                .containsExactly("BK887");
    }

    @Test
    void readsNumericCodes() {
        assertThat(service.identify(K98K, "337", null).matches())
                .extracting(RgaRecord::reference)
                .containsExactly("BO118");
    }

    @Test
    void groupsTheRgaLabelsThatSpellTheSameCodeDifferently() {
        assertThat(service.identify(K98K, "dot", "7 X 64").matches())
                .extracting(RgaRecord::reference)
                .containsExactly("CH676");
    }

    @Test
    void listsEveryCaliberTheCodeExistsIn() {
        assertThat(service.identify(K98K, "bnz", null).availableCalibers())
                .contains("8 X 57 IS", "8 X 60 S", "308 WIN (7,62 X 51MM)");
    }

    @Test
    void returnsNoMatchWhenTheRgaHasNoRecordInThatCaliber() {
        Identification result = service.identify(K98K, "svw", "7 X 64");

        assertThat(result.matches()).isEmpty();
        assertThat(result.availableCalibers()).containsExactly("8 X 57 IS");
    }

    @Test
    void citesTheSourcesOfTheCode() {
        assertThat(service.identify(K98K, "bnz", null).sources())
                .singleElement()
                .satisfies(source -> assertThat(source.license()).isEqualTo("Licence Ouverte 2.0"));
    }

    @Test
    void rejectsAnUnknownCode() {
        assertThatThrownBy(() -> service.identify(K98K, "zzz", null))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("zzz");
    }

    @Test
    void rejectsAnUnknownModel() {
        assertThatThrownBy(() -> service.identify("no-such-model", "bnz", null))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("no-such-model");
    }
}
