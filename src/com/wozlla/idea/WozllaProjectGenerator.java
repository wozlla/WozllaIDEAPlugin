package com.wozlla.idea;

import com.intellij.lang.javascript.boilerplate.AbstractGithubTagDownloadedProjectGenerator;
import com.intellij.platform.templates.github.GithubTagInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WozllaProjectGenerator extends AbstractGithubTagDownloadedProjectGenerator {

    @NotNull
    @Override
    protected String getDisplayName() {
        return "Wozlla Project";
    }

    @NotNull
    @Override
    protected String getGithubUserName() {
        return "wozlla";
    }

    @NotNull
    @Override
    protected String getGithubRepositoryName() {
        return "WozllaIDEAPlugin-ProjectTemplate";
    }

    @Nullable
    @Override
    public String getDescription() {
        return "Wozlla Project";
    }

    @Nullable
    @Override
    public String getPrimaryZipArchiveUrlForDownload(@NotNull GithubTagInfo githubTagInfo) {
        return null;
    }
}
