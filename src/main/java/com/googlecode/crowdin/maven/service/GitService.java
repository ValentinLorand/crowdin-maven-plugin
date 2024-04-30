package com.googlecode.crowdin.maven.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class GitService {

    public GitService() {
    }

    public String getCurrentGitBranch(){
        try {
            Process process = Runtime.getRuntime().exec("git rev-parse --abbrev-ref HEAD");
            process.waitFor();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            return reader.readLine();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to get current git branch", e);
        }
    }
}
