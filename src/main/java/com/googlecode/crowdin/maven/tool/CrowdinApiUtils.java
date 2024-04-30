package com.googlecode.crowdin.maven.tool;

import com.googlecode.crowdin.maven.dao.CrowdinDAOException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.InputStream;
import java.util.logging.Logger;

public class CrowdinApiUtils {

    private CrowdinApiUtils() {
    }

    static final String CROWDIN_SERVER_URL = "https://crowdin.com/api/v2";

    public static String getServerUrl() {
        return CROWDIN_SERVER_URL;
    }

    public static void addAuthorizationHeader(HttpRequestBase httpRequestBase, String apiKey) {
        httpRequestBase.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);
    }

    public static HttpResponse executeQueryWithResponse(CloseableHttpClient httpClient, String apiKey, HttpRequestBase httpRequestBase, Logger log) {
        if (apiKey != null)
            addAuthorizationHeader(httpRequestBase, apiKey);
        try {
            log.fine("Calling " + httpRequestBase.getURI().getHost());
            HttpResponse response = httpClient.execute(httpRequestBase);
            log.fine("Status code : " + response.getStatusLine().getStatusCode());
            checkStatusCode(response);
            return response;
        } catch (Exception e) {
            throw new CrowdinDAOException("Failed to call API", e);
        }
    }

    public static InputStream executeQuery(CloseableHttpClient httpClient, String apiKey, HttpRequestBase httpRequestBase, Logger log) {
        HttpResponse response = executeQueryWithResponse(httpClient, apiKey, httpRequestBase, log);
        try {
            return response.getEntity() != null ? response.getEntity().getContent() : null;
        } catch (Exception e) {
            throw new CrowdinDAOException("Failed to call API", e);
        }
    }

    private static void checkStatusCode(HttpResponse response) {
        int returnCode = response.getStatusLine().getStatusCode();
        if (returnCode >= 400) {
            throw new CrowdinDAOException("Status code is " + returnCode + " - " + response.getStatusLine().getReasonPhrase());
        }
    }
}
