package com.googlecode.crowdin.maven.mojo;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.wagon.authentication.AuthenticationInfo;

import java.io.File;

public abstract class AbstractCrowdinMojo extends AbstractMojo {

    /**
     * The current Maven project
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    /**
     * The Maven Wagon manager to use when obtaining server authentication details.
     */
    @Component
    protected WagonManager wagonManager;

    /**
     * Server id in settings.xml. username is Crowdin project id, password is personal API key
     */
    @Parameter(property = "crowdinServerId", required = true)
    protected String crowdinServerId;

    /**
     * The directory where the messages can be found.
     */
    @Parameter(defaultValue = "${project.basedir}/src/main/messages", required = true)
    protected File messagesInputDirectory;

    /**
     * The directory where the messages can be found.
     */
    @Parameter(defaultValue = "${project.basedir}/src/main/crowdin", required = true)
    protected File messagesOutputDirectory;

    protected AuthenticationInfo authenticationInfo;

    public void execute() throws MojoExecutionException, MojoFailureException {
        authenticationInfo = wagonManager.getAuthenticationInfo(crowdinServerId);
        if (authenticationInfo == null) {
            throw new MojoExecutionException("Failed to find server with id " + crowdinServerId
                    + " in Maven settings (~/.m2/settings.xml)");
        }
    }

    protected String getMavenId(Artifact artifact) {
        return artifact.getGroupId() + "." + artifact.getArtifactId();
    }

}
