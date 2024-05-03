package com.googlecode.crowdin.maven.dao.pull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.crowdin.maven.dao.CrowdinDAOException;
import com.googlecode.crowdin.maven.dao.CrowdinApiUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static com.googlecode.crowdin.maven.dao.CrowdinApiUtils.executeQuery;

public class PullTranslationDAOCrowdin implements PullTranslationDAO {

    private final Logger log;

    private final ObjectMapper mapper = new ObjectMapper();
    private final String projectId;
    private final String apiKey;
    private final String serverUrl;
    protected CloseableHttpClient client;

    public PullTranslationDAOCrowdin(Logger logger, String serverUrl, String projectId, String apiKey) {
        this.projectId = projectId;
        this.apiKey = apiKey;
        this.serverUrl = serverUrl;
        this.log = logger;
        this.client = HttpClientBuilder.create().useSystemProperties().build();
    }

    @Override
    public String buildTranslations() {
        String uri = serverUrl + "/projects/" + projectId + "/translations/builds";
        HttpPost postMethod = new HttpPost(uri);
        InputStream responseBodyAsStream = CrowdinApiUtils.executeQuery(client, apiKey, postMethod, log);

        try {
            JsonNode node = mapper.readTree(responseBodyAsStream);
            return node.get("data").get("id").asText();
        }catch (Exception e) {
            throw new CrowdinDAOException("Failed to build translations", e);
        }

    }

    @Override
    public InputStream downloadTranslations(String buildId) {
        String uri = serverUrl + "/projects/" + projectId + "/translations/builds/" +buildId+ "/download";
        HttpGet getMethod = new HttpGet(uri);
        HttpResponse response = CrowdinApiUtils.executeQueryWithResponse(client, apiKey, getMethod, log);

        int statusCode = response.getStatusLine().getStatusCode();
        try {
            InputStream responseBodyAsStream = response.getEntity().getContent();
            if (statusCode == 200) {
                JsonNode node = mapper.readTree(responseBodyAsStream);
                String downloadUrl = node.get("data").get("url").asText();

                HttpGet downloadRequest = new HttpGet(downloadUrl);
                return executeQuery(client,null, downloadRequest, log);

            } else if (statusCode == 202) {
                TimeUnit.SECONDS.sleep(2);
                JsonNode node = mapper.readTree(responseBodyAsStream);
                String percent = node.get("data").get("progress").asText();
                log.info("Waiting for build to be ready... (" + percent + "%)");
                return downloadTranslations(buildId);
            } else {
                throw new CrowdinDAOException("Failed to download translations. Status code: " + statusCode);
            }
        }catch (Exception e) {
            throw new CrowdinDAOException("Failed to download translations", e);
        }
    }

    @Override
    public InputStream exportTranslations() {
        String uri = serverUrl + "/projects/" + projectId + "/translations/exports";
        HttpPost postRequest = new HttpPost(uri);
        InputStream responseBodyAsStream = CrowdinApiUtils.executeQuery(client, apiKey, postRequest, log);

        try {
            JsonNode node = mapper.readTree(responseBodyAsStream);
            String downloadUrl = node.get("data").get("url").asText();

            HttpGet downloadRequest = new HttpGet(downloadUrl);
            return CrowdinApiUtils.executeQuery(client, apiKey, downloadRequest, log);
        } catch (Exception e) {
            throw new CrowdinDAOException("Failed to download translations", e);
        }
    }
}
