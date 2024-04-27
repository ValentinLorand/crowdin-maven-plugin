package com.googlecode.crowdin.maven.mojo;

import com.googlecode.crowdin.maven.dao.CrowdinTranslationDAO;
import com.googlecode.crowdin.maven.dao.CrowdinTranslationDAOImpl;
import com.googlecode.crowdin.maven.tool.CrowdinApiUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Export crowdin translations in this project, for a fresh translation file
 */
@Mojo(name = "export", aggregator = true, threadSafe = true)
public class ExportCrowdinMojo extends AbstractCrowdinMojo {

	private final CrowdinTranslationDAO crowdinDAO;

	public ExportCrowdinMojo() {
		super();
		this.crowdinDAO = new CrowdinTranslationDAOImpl(CrowdinApiUtils.getServerUrl(),
				authenticationInfo.getUserName(),
				authenticationInfo.getPassword());
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("Asking crowdin to export translations");
		try {
			crowdinDAO.exportTranslations();
		} catch (Exception e) {
			getLog().info("Export failed, trying to build translations");
			throw new MojoExecutionException("Failed to export translations", e);
		}
	}
}
