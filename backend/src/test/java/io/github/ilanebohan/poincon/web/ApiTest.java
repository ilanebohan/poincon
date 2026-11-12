package io.github.ilanebohan.poincon.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class ApiTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void listsTheSupportedModels() throws Exception {
        mvc.perform(get("/api/models"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem("mauser-k98k")));
    }

    @Test
    void listsTheCodesOfAModel() throws Exception {
        mvc.perform(get("/api/models/mauser-k98k/codes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].code", hasItem("bnz")));
    }

    @Test
    void identifiesACode() throws Exception {
        mvc.perform(get("/api/models/mauser-k98k/identify").param("code", "bnz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code.manufacturer").value("Steyr-Daimler-Puch"))
                .andExpect(jsonPath("$.matches[0].reference").value("BK887"))
                .andExpect(jsonPath("$.similar[*].reference", hasItem("AJ639")));
    }

    @Test
    void answersNotFoundForAnUnknownCode() throws Exception {
        mvc.perform(get("/api/models/mauser-k98k/identify").param("code", "zzz"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail", containsString("zzz")));
    }

    @Test
    void answersBadRequestWhenTheCodeIsMissing() throws Exception {
        mvc.perform(get("/api/models/mauser-k98k/identify")).andExpect(status().isBadRequest());
    }

    @Test
    void returnsAnRgaRecordWhateverTheCaseOfTheReference() throws Exception {
        mvc.perform(get("/api/rga/bk887"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.manufacturer").value("WAFFENFABRIK STEYR DAIMLER PUCH (BNZ)"));
    }

    @Test
    void answersNotFoundForAnRgaRecordOutsideTheDataset() throws Exception {
        mvc.perform(get("/api/rga/ZZ999")).andExpect(status().isNotFound());
    }
}
