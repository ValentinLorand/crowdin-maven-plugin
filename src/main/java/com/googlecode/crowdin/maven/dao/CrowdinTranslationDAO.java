package com.googlecode.crowdin.maven.dao;

import java.io.InputStream;

public interface CrowdinTranslationDAO {

    /**
     * Build translations on crowdin
     * @return the build id
     */
    String buildTranslations();

    /**
     * Download translations from crowdin
     * @param buildId the build id
     * @return the translations
     */
    InputStream downloadTranslations(String buildId);

    /**
     * Export translations from crowdin
     * @return the translations
     */
    InputStream exportTranslations();

}
