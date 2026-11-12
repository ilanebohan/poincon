package io.github.ilanebohan.poincon.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.ilanebohan.poincon.identify.NotFoundException;
import io.github.ilanebohan.poincon.rga.RgaCatalog;
import io.github.ilanebohan.poincon.rga.RgaRecord;

@RestController
@RequestMapping("/api/rga")
public class RgaController {

    private final RgaCatalog rga;

    public RgaController(RgaCatalog rga) {
        this.rga = rga;
    }

    @GetMapping("/{reference}")
    public RgaRecord record(@PathVariable String reference) {
        return rga.findByReference(reference)
                .orElseThrow(() -> new NotFoundException("No RGA record '%s' in this dataset.".formatted(reference)));
    }
}
