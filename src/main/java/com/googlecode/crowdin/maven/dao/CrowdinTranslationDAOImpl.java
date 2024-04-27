package com.googlecode.crowdin.maven.dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.InputStream;
import java.util.logging.Logger;

import static com.googlecode.crowdin.maven.tool.CrowdinApiUtils.executeQuery;

public class CrowdinTranslationDAOImpl implements CrowdinTranslationDAO {

    private final Logger log = Logger.getLogger(CrowdinTranslationDAOImpl.class.getName());

    private final ObjectMapper mapper = new ObjectMapper();
    private final String projectId;
    private final String apiKey;
    private final String serverUrl;
    protected CloseableHttpClient client;

    public CrowdinTranslationDAOImpl(String serverUrl, String projectId, String apiKey) {
        this.projectId = projectId;
        this.apiKey = apiKey;
        this.serverUrl = serverUrl;
        this.client = HttpClientBuilder.create().useSystemProperties().build();
    }

    @Override
    public String buildTranslations() {
        String uri = serverUrl + "/projects/" + projectId + "/translations/builds";
        HttpGet getMethod = new HttpGet(uri);
        InputStream responseBodyAsStream = executeQuery(client, apiKey, getMethod, log);

        try {
            JsonNode node = mapper.readTree(responseBodyAsStream);
            return node.get("data").get(0).get("data").get("id").asText();
        }catch (Exception e) {
            throw new CrowdinDAOException("Failed to build translations", e);
        }

    }

    @Override
    public InputStream downloadTranslations(String buildId) {
        String uri = serverUrl + "/projects/" + projectId + "/translations/builds/" +buildId+ "/download";
        HttpGet getMethod = new HttpGet(uri);
        InputStream responseBodyAsStream = executeQuery(client, apiKey, getMethod, log);

        try {
            JsonNode node = mapper.readTree(responseBodyAsStream);
            String downloadUrl = node.get("data").get("url").asText();

            HttpGet downloadRequest = new HttpGet(downloadUrl);
            return executeQuery(client, apiKey, downloadRequest, log);
        } catch (Exception e) {
            throw new CrowdinDAOException("Failed to download translations", e);
        }
    }

    @Override
    public InputStream exportTranslations() {
        String uri = serverUrl + "/projects/" + projectId + "/translations/exports";
        HttpPost postRequest = new HttpPost(uri);
        InputStream responseBodyAsStream = executeQuery(client, apiKey, postRequest, log);

        try {
            JsonNode node = mapper.readTree(responseBodyAsStream);
            String downloadUrl = node.get("data").get("url").asText();

            HttpGet downloadRequest = new HttpGet(downloadUrl);
            return executeQuery(client, apiKey, downloadRequest, log);
        } catch (Exception e) {
            throw new CrowdinDAOException("Failed to download translations", e);
        }
    }
}
