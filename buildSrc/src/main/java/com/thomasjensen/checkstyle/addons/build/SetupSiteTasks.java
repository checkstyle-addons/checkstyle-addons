package com.thomasjensen.checkstyle.addons.build;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.tools.ant.filters.ReplaceTokens;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.javadoc.Javadoc;

import com.thomasjensen.checkstyle.addons.build.tasks.SiteTask;


/**
 * Packaging of documentation for publication on the website.
 */
public class SetupSiteTasks
{
    private final Project project;



    public SetupSiteTasks(final Project project)
    {
        this.project = project;
    }



    public void registerTasks()
    {
        configureCopyJavadoc();
        configureSiteCopyAllChecks();
        configureSiteCopyDownloadGuide();
        project.getTasks().register("site", SiteTask.class).configure(SiteTask::configureTask);
    }



    private void configureCopyJavadoc()
    {
        final BuildUtil buildUtil = new BuildUtil(project);
        TaskProvider<Copy> taskProvider = project.getTasks().register("siteCopyJavadoc", Copy.class);
        taskProvider.configure(copyTask -> {
            Javadoc javadocTask = (Javadoc) project.getTasks().getByName("javadoc");
            copyTask.dependsOn(javadocTask);
            copyTask.setDescription("Copy Javadoc to site directory");
            copyTask.setGroup(SiteTask.SITE_GROUP);
            copyTask.setDestinationDir(new File(project.getBuildDir(), "site"));
            copyTask.setDuplicatesStrategy(DuplicatesStrategy.EXCLUDE);

            final String website = buildUtil.getBuildConfig().getWebsite().get();
            copyTask.into("v" + project.getVersion() + "/apidocs", (CopySpec copySpec) -> {
                copySpec.from(project.fileTree(Objects.requireNonNull(javadocTask.getDestinationDir())));
                copySpec.filter((String line) -> {
                    if (!line.contains(website + "latest/checks/")) {
                        return line;
                    }
                    return line.replace(website + "latest/checks/", website + "v" + project.getVersion() + "/checks/");
                });
            });
            copyTask.into("v" + project.getVersion() + "/apidocs/resources", copySpec ->
                copySpec.from(project.fileTree(new File(javadocTask.getDestinationDir(), "resources"))));
            copyTask.into("latest/apidocs", copySpec ->
                copySpec.from(project.fileTree(Objects.requireNonNull(javadocTask.getDestinationDir()))));
        });
    }



    private Map<String, Object> buildReplacements()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ZZZZ");
        Date buildTimestamp = new BuildUtil(project).getBuildConfig().getBuildTimestamp().get();
        Map<String, String> replacements = new HashMap<>();
        replacements.put("buildTimestamp", sdf.format(buildTimestamp));
        replacements.put("version", "v" + project.getVersion());
        Map<String, Object> result = new HashMap<>();
        result.put("tokens", replacements);
        return result;
    }



    private void configureSiteCopyAllChecks()
    {
        TaskProvider<Copy> taskProvider = project.getTasks().register("siteCopyAllChecks", Copy.class);
        taskProvider.configure(copyTask -> {
            final BuildUtil buildUtil = new BuildUtil(project);
            copyTask.setDescription("Copy list of all checks to site directory");
            copyTask.setGroup(SiteTask.SITE_GROUP);
            copyTask.setDestinationDir(new File(project.getBuildDir(), "site"));

            final SourceSet mainSourceSet = buildUtil.getSourceSet(SourceSet.MAIN_SOURCE_SET_NAME);
            final String checksPackage = buildUtil.getBuildConfig().getChecksPackage().get();
            final File originalFile = new File(mainSourceSet.getResources().getSrcDirs().iterator().next(),
                "/" + checksPackage + "/all_checks.html");
            final Action<CopySpec> action = copySpec -> {
                copySpec.from(originalFile);
                copySpec.rename((String fileName) -> fileName.replace(originalFile.getName(), "index.html"));
                copySpec.filter(buildReplacements(), ReplaceTokens.class);
            };
            copyTask.into("v" + project.getVersion() + "/checks", action);
            copyTask.into("latest/checks", action);
        });
    }



    private void configureSiteCopyDownloadGuide()
    {
        TaskProvider<Copy> taskProvider = project.getTasks().register("siteCopyDownloadGuide", Copy.class);
        taskProvider.configure(copyTask -> {
            copyTask.setDescription("Copy download guide frontmatter stub to site directory");
            copyTask.setGroup(SiteTask.SITE_GROUP);
            copyTask.setDestinationDir(new File(project.getBuildDir(), "site"));

            final SourceSet mainSourceSet = new BuildUtil(project).getSourceSet(SourceSet.MAIN_SOURCE_SET_NAME);
            final File originalFile = new File(mainSourceSet.getResources().getSrcDirs().iterator().next(),
                "/download-guide.html");
            final Action<CopySpec> action = copySpec -> {
                copySpec.from(originalFile);
                copySpec.filter(buildReplacements(), ReplaceTokens.class);
            };
            copyTask.into("v" + project.getVersion(), action);
            copyTask.into("latest", action);
        });
    }
}
