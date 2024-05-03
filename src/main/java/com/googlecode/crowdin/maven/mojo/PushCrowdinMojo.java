package com.googlecode.crowdin.maven.mojo;

import com.googlecode.crowdin.maven.dao.push.PushSourceFileDAO;
import com.googlecode.crowdin.maven.dao.push.PushSourceFileDAOCrowdin;
import com.googlecode.crowdin.maven.dao.CrowdinApiUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Push Maven translations of this project in crowdin
 */
@Mojo(name = "push", threadSafe = true)
public class PushCrowdinMojo extends AbstractCrowdinMojo {

    private PushSourceFileDAO crowdinDAO;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        super.execute();

        if (messagesInputDirectory.exists()) {
            String folderName = getMavenId(project.getArtifact());
            String crowdinFolderId = getCrowdinFileDAO().getFolderIdByName(folderName);

            getLog().debug("Retrieving message files from this project");
            Map<String, File> files = getMessageFiles(folderName);

            if (crowdinFolderId == null)
                crowdinDAO.createFolder(folderName);

            Map<String,String> existingFiles = getCrowdinFileDAO().getAllFileInFolder(crowdinFolderId,folderName);
            Set<Entry<String, File>> entrySet = files.entrySet();

            for (Entry<String, File> entry : entrySet) {
                getLog().debug("File : " + entry.getKey());
                getLog().debug("existingFiles : " + existingFiles.keySet());
                if (existingFiles.containsKey(entry.getKey())) {
                    getLog().info("Update file on crowdin : " + entry.getValue().getName());
                    String read = readFileContent(entry.getValue());

                    crowdinDAO.updateFile(existingFiles.get(entry.getKey()), entry.getValue().getName(), read);
                } else {
                    getLog().info("Create file on crowdin : " + entry.getValue().getName());
                    String read = readFileContent(entry.getValue());

                    crowdinDAO.createFile(crowdinFolderId, entry.getKey().split("/")[1], read);
                }
            }

            for (String file : existingFiles.keySet()) {
                if (!files.containsKey(file)) {
                    String fileName = folderName + "/" + file;
                    getLog().info("Delete file on crowdin : " + fileName);
                    crowdinDAO.deleteFile(existingFiles.get(file));
                }
            }
        } else {
            getLog().info(messagesInputDirectory.getPath() + " not found");
        }
    }

    /**
     * Read the content of a file
     * @param file the file to read
     * @return the content of the file
     * @throws MojoExecutionException if the file can't be read
     */
    private String readFileContent(File file) throws MojoExecutionException{
        try {
            return new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            throw new MojoExecutionException(e);
        }
    }

    /**
     * Get the DAO to push files in crowdin (Factory)
     * @return the DAO to push files in crowdin
     */
    private PushSourceFileDAO getCrowdinFileDAO() {
        if (crowdinDAO == null)
            this.crowdinDAO = new PushSourceFileDAOCrowdin(CrowdinApiUtils.getServerUrl(),
                authenticationInfo.getUserName(),
                authenticationInfo.getPassword());
        return crowdinDAO;
    }

    /**
     * Get the files to push in crowdin
     * @param folderName the folder name in crowdin
     * @return the files to push in crowdin
     */
    private Map<String, File> getMessageFiles(String folderName) {
        Map<String, File> result = new HashMap<>();
        File[] listFiles = messagesInputDirectory.listFiles();
        if (listFiles != null) {
            for (File file : listFiles) {
                if (!file.isDirectory() && !file.getName().startsWith(".") && file.getName().endsWith(".properties")) {
                    String crowdinPath = folderName + "/" + file.getName();
                    getLog().debug("Found " + crowdinPath);
                    result.put(crowdinPath, file);
                }
            }
        }
        return result;
    }
}
