package io.github.ilanebohan.poincon.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.github.ilanebohan.poincon.identify.Identification;
import io.github.ilanebohan.poincon.identify.IdentificationService;
import io.github.ilanebohan.poincon.marking.MarkingCode;
import io.github.ilanebohan.poincon.marking.MarkingRepository;
import io.github.ilanebohan.poincon.marking.WeaponModel;

@RestController
@RequestMapping("/api/models")
public class ModelController {

    private final MarkingRepository markings;
    private final IdentificationService identification;

    public ModelController(MarkingRepository markings, IdentificationService identification) {
        this.markings = markings;
        this.identification = identification;
    }

    @GetMapping
    public List<WeaponModel> models() {
        return markings.models();
    }

    @GetMapping("/{modelId}/codes")
    public List<MarkingCode> codes(@PathVariable String modelId) {
        return identification.dataset(modelId).codes();
    }

    @GetMapping("/{modelId}/identify")
    public Identification identify(
            @PathVariable String modelId,
            @RequestParam String code,
            @RequestParam(required = false) String caliber) {
        return identification.identify(modelId, code, caliber);
    }
}
