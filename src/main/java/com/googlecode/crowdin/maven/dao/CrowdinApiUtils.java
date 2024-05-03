package com.googlecode.crowdin.maven.dao;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.InputStream;
import java.util.logging.Logger;

public class CrowdinApiUtils {

    private CrowdinApiUtils() {
    }

    private static final String CROWDIN_SERVER_URL = "https://crowdin.com/api/v2";

    public static String getServerUrl() {
        return CROWDIN_SERVER_URL;
    }

    public static InputStream executeQuery(CloseableHttpClient httpClient, String apiKey, HttpRequestBase httpRequestBase, Logger log) {
        HttpResponse response = executeQueryWithResponse(httpClient, apiKey, httpRequestBase, log);
        try {
            return response.getEntity() != null ? response.getEntity().getContent() : null;
        } catch (Exception e) {
            throw new CrowdinDAOException("Failed to call API", e);
        }
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

    /**
     * Add the Authorization header to the request
     * @param httpRequestBase the request
     * @param apiKey the API key
     */
    private static void addAuthorizationHeader(HttpRequestBase httpRequestBase, String apiKey) {
        httpRequestBase.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);
    }

    /**
     * Check the status code of the response, throw an exception if code is 4xx or 5xx
     * @param response the response
     */
    private static void checkStatusCode(HttpResponse response) {
        int returnCode = response.getStatusLine().getStatusCode();
        if (returnCode >= 400) {
            throw new CrowdinDAOException("Status code is " + returnCode + " - " + response.getStatusLine().getReasonPhrase());
        }
    }
}
