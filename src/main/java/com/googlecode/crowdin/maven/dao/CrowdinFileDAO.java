package com.googlecode.crowdin.maven.dao;

import java.util.Map;

public interface CrowdinFileDAO {

    /**
     * Get the folder id by its name
     * @param folderName the folder name
     * @return the folder id
     */
    String getFolderIdByName(String folderName);

    /**
     * Get all files in a folder
     * @param folderId the folder id
     * @return a map of file id and file name
     */
    Map<String,String> getAllFileInFolder(String folderId, String folderName);

    /**
     * Create a folder
     * @param folderName the folder name
     * @return the folder id
     */
    String createFolder(String folderName);

    /**
     * Create a file
     * @param folderId the folder id
     * @param fileName the file name
     * @param content the file content
     */
    void createFile(String folderId, String fileName, String content);

    /**
     * Update a file
     * @param fileId the file id
     * @param fileName the file name
     * @param content the file content
     */
    void updateFile(String fileId, String fileName, String content);

    /**
     * Delete a file
     * @param fileId the file id
     */
    void deleteFile(String fileId);

}
