package io.github.ilanebohan.poincon.identify;

import java.util.List;

import io.github.ilanebohan.poincon.marking.MarkingCode;
import io.github.ilanebohan.poincon.marking.Source;
import io.github.ilanebohan.poincon.marking.WeaponModel;
import io.github.ilanebohan.poincon.rga.RgaRecord;

/**
 * What a manufacturer code tells about a weapon.
 *
 * @param matches           RGA records of this maker, under this code, in the requested caliber
 * @param similar           RGA records of the same maker listed without the code, same caliber
 * @param availableCalibers every caliber in which the RGA lists this code
 */
public record Identification(
        WeaponModel model,
        MarkingCode code,
        String caliber,
        List<RgaRecord> matches,
        List<RgaRecord> similar,
        List<String> availableCalibers,
        List<Source> sources) {
}
