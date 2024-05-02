package com.googlecode.crowdin.maven.mojo;

import com.googlecode.crowdin.maven.dao.push.CrowdinPushFileDAO;
import com.googlecode.crowdin.maven.dao.push.CrowdinPushFileDAOImpl;
import com.googlecode.crowdin.maven.tool.CrowdinApiUtils;
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

    private CrowdinPushFileDAO crowdinDAO;

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
                    getLog().info("Delete file on crowdin : " + file);
                    crowdinDAO.deleteFile(existingFiles.get(file));
                }
            }
        } else {
            getLog().info(messagesInputDirectory.getPath() + " not found");
        }
    }

    private String readFileContent(File file) {
        try {
            return new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private CrowdinPushFileDAO getCrowdinFileDAO() {
        if (crowdinDAO == null)
            this.crowdinDAO = new CrowdinPushFileDAOImpl(CrowdinApiUtils.getServerUrl(),
                authenticationInfo.getUserName(),
                authenticationInfo.getPassword());
        return crowdinDAO;
    }

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
