package com.googlecode.crowdin.maven.dao;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

class CrowdinTranslationDAOTest {

    Logger logger = Logger.getLogger(CrowdinTranslationDAOTest.class.getName());

    private static CrowdinTranslationDAO crowdinDAO;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(7777));

    @BeforeEach
    public void setUp() {
        crowdinDAO = new CrowdinPullTranslationDAOImpl(logger, "http://localhost:7777","111", "secretApiKey");
        wireMockRule.start();
    }

    @AfterEach
    public void tearDown() {
        wireMockRule.stop();
    }

    @Test
    void testBuildTranslations() {
        wireMockRule.stubFor(get("/projects/111/translations/builds")
                .withHeader("Authorization", equalTo("Bearer secretApiKey"))
                .willReturn(ok()
                        .withBody("{\"data\":[{\"data\":{\"id\":\"2\"}}]}"))
        );
        String folderId = crowdinDAO.buildTranslations();
        Assertions.assertFalse(folderId.isEmpty());
        Assertions.assertEquals("2", folderId);
    }

    @Test
    void testDownloadTranslations() throws IOException {
        wireMockRule.stubFor(get("/projects/111/translations/builds/2/download")
                .withHeader("Authorization", equalTo("Bearer secretApiKey"))
                .willReturn(ok()
                        .withHeader("Authorization", "Bearer secretApiKey")
                        .withBody("{\"data\":{\"url\": \"http://localhost:7777/downloadLink\"}}"))
        );
        wireMockRule.stubFor(get("/downloadLink")
                .withHeader("Authorization", equalTo("Bearer secretApiKey"))
                .willReturn(ok()
                        .withBody("traductionKey=traductionValue"))
        );
        InputStream inputStream = crowdinDAO.downloadTranslations("2");
        Assertions.assertNotNull(inputStream);
        String result = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        Assertions.assertEquals("traductionKey=traductionValue", result);
    }

    @Test
    void testExportTranslations() throws IOException {
        wireMockRule.stubFor(post("/projects/111/translations/exports")
                .withHeader("Authorization", equalTo("Bearer secretApiKey"))
                .willReturn(ok()
                        .withBody("{\"data\":{\"url\": \"http://localhost:7777/downloadLink\"}}"))
        );
        wireMockRule.stubFor(get("/downloadLink")
                .withHeader("Authorization", equalTo("Bearer secretApiKey"))
                .willReturn(ok()
                        .withBody("exportedTranslations"))
        );
        InputStream inputStream = crowdinDAO.exportTranslations();
        Assertions.assertNotNull(inputStream);
        String result = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        Assertions.assertEquals("exportedTranslations", result);
    }
}
