package com.googlecode.crowdin.maven.dao.push;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.googlecode.crowdin.maven.dao.CrowdinDAOException;
import com.googlecode.crowdin.maven.tool.CrowdinApiUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

public class CrowdinPushFileDAOImpl implements CrowdinPushFileDAO {

    private final Logger log = Logger.getLogger(CrowdinPushFileDAOImpl.class.getName());

    private final ObjectMapper mapper = new ObjectMapper();
    private final String projectId;
    private final String apiKey;
    private final String serverUrl;
    protected CloseableHttpClient client;

    public CrowdinPushFileDAOImpl(String serverUrl, String projectId, String apiKey) {
        this.projectId = projectId;
        this.apiKey = apiKey;
        this.serverUrl = serverUrl;
        this.client = HttpClientBuilder.create().useSystemProperties().build();
    }

    @Override
    public String getFolderIdByName(String folderName) {
        String uri = serverUrl + "/projects/" + projectId + "/directories";
        uri += "?filter=" + folderName;
        HttpGet getRequest = new HttpGet(uri);
        InputStream responseBodyAsStream = CrowdinApiUtils.executeQuery(client, apiKey, getRequest, log);

        try {
            JsonNode node = mapper.readTree(responseBodyAsStream);
            if (node.withArray("data").isEmpty())
                return null;
            return node.get("data").get(0).get("data").get("id").asText();
        } catch (Exception e) {
            throw new CrowdinDAOException("Failed to retrieve folder id by its name " + folderName, e);
        }
    }

    @Override
    public Map<String,String> getAllFileInFolder(String folderId, String folderName) {
        String uri = serverUrl + "/projects/" + projectId + "/files";
        uri += "?directoryId=" + folderId;
        HttpGet getRequest = new HttpGet(uri);
        InputStream responseBodyAsStream = CrowdinApiUtils.executeQuery(client, apiKey, getRequest, log);

        try {
            JsonNode node = mapper.readTree(responseBodyAsStream);
            ArrayNode nodes = node.withArrayProperty("data");
            Map<String,String> result = new HashMap<>();
            for (Iterator<JsonNode> it = nodes.elements(); it.hasNext(); ) {
                JsonNode element = it.next();
                String name = folderName + "/" + element.get("data").get("name").asText();
                String id = element.get("data").get("id").asText();
                log.fine("File " + name + " has id " + id);
                result.put(name,id);
            }
            return result;
        } catch (Exception e) {
            throw new CrowdinDAOException("Failed to call API", e);
        }
    }

    @Override
    public String createFolder(String folderName) {
        String uri = serverUrl + "/projects/" + projectId + "/directories";
        HttpPost postRequest = new HttpPost(uri);
        StringEntity requestEntity = new StringEntity("{\"name\":\"" + folderName + "\"}", ContentType.APPLICATION_JSON);
        postRequest.setEntity(requestEntity);
        InputStream responseBodyAsStream = CrowdinApiUtils.executeQuery(client, apiKey, postRequest, log);

        try {
            JsonNode node = mapper.readTree(responseBodyAsStream);
            return node.get("data").get("id").asText();
        } catch (Exception e) {
            throw new CrowdinDAOException("Failed to create a folder in Crowdin" + folderName, e);
        }
    }

    @Override
    public void createFile(String directoryId, String fileName, String content) {
        String storageId = uploadStorage(fileName, content);

        String uri2 = serverUrl + "/projects/" + projectId + "/files";
        HttpPost postRequest = new HttpPost(uri2);
        StringEntity requestEntity = new StringEntity("{\"storageId\":"+storageId+",\"name\":\""+fileName+"\",\"directoryId\":"+directoryId+"}", ContentType.APPLICATION_JSON);
        postRequest.setEntity(requestEntity);

        CrowdinApiUtils.executeQuery(client, apiKey, postRequest, log);
    }

    @Override
    public void updateFile(String fileId, String fileName, String content) {
        String storageId = uploadStorage(fileName, content);

        String uri = serverUrl + "/projects/" + projectId + "/files/" + fileId;
        HttpPut putRequest = new HttpPut(uri);
        StringEntity requestEntity = new StringEntity("{\"storageId\":"+storageId+",\"name\":\""+fileName+"\"}", ContentType.APPLICATION_JSON);
        putRequest.setEntity(requestEntity);

        CrowdinApiUtils.executeQuery(client, apiKey, putRequest, log);
    }

    @Override
    public void deleteFile(String fileId) {
        String uri = serverUrl + "/projects/" + this.projectId + "/files/" + fileId;
        HttpDelete deleteRequest = new HttpDelete(uri);
        CrowdinApiUtils.executeQuery(client, apiKey, deleteRequest, log);
    }

    private String uploadStorage(String fileName, String content) {
        String uri = serverUrl + "/storages";
        HttpPost storageRequest = new HttpPost(uri);
        storageRequest.setHeader("Crowdin-API-FileName", fileName);
        storageRequest.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_OCTET_STREAM.toString());
        storageRequest.setEntity(new StringEntity(content, ContentType.APPLICATION_OCTET_STREAM));

        InputStream responseBodyAsStream = CrowdinApiUtils.executeQuery(client, apiKey, storageRequest, log);
        try {
            JsonNode node = mapper.readTree(responseBodyAsStream);
            return node.get("data").get("id").asText();
        } catch (Exception e) {
            throw new CrowdinDAOException("Failed to upload storage to Crowdin", e);
        }
    }
}
