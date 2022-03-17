package com.thomasjensen.checkstyle.addons.build.tasks;
/*
 * Checkstyle-Addons - Additional Checkstyle checks
 * Copyright (c) 2015-2022, the Checkstyle Addons contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 3, as published by the Free
 * Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.file.FileTree;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskContainer;

import com.thomasjensen.checkstyle.addons.build.BuildUtil;


/**
 * Gradle task to assemble the documentation for publication on the website.
 */
public class SiteTask
    extends DefaultTask
{
    public static final String SITE_GROUP = "site";

    private Provider<FileTree> markdownFiles;

    private Provider<File> siteDir;



    public void configureTask()
    {
        final Project project = getProject();
        final BuildUtil buildUtil = new BuildUtil(project);
        setDescription("Package documentation for publication on the website");
        setGroup(SITE_GROUP);

        final TaskContainer tasks = project.getTasks();
        for (final String predecTaskName : new String[]{
            "processResources", "siteCopyAllChecks", "siteCopyJavadoc", "siteCopyDownloadGuide"})//
        {
            dependsOn(tasks.named(predecTaskName));
        }

        markdownFiles = project.provider(() -> {
            SourceSet mainSourceSet = buildUtil.getSourceSet(SourceSet.MAIN_SOURCE_SET_NAME);
            return project.fileTree(
                mainSourceSet.getResources().getSrcDirs().iterator().next(), ft -> ft.include("**/*.md"));
        });

        siteDir = project.provider(() -> new File(project.getBuildDir(), "site"));
    }



    @TaskAction
    public void runCollect()
    {
        try {
            collect();
        }
        catch (IOException e) {
            throw new GradleException("failed to run task '" + getName() + "'", e);
        }
    }



    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void collect()
        throws IOException
    {
        final Project project = getProject();
        final BuildUtil buildUtil = new BuildUtil(project);
        final FileTree mdFiles = getMarkdownFiles().get();
        final File siteDir = getSiteDir().get();
        File includesVersionDir = new File(siteDir, "_includes/v" + project.getVersion());
        includesVersionDir.mkdirs();

        File versionChecksDir = new File(siteDir, "v" + project.getVersion() + "/checks");
        versionChecksDir.mkdirs();
        File latestChecksDir = new File(siteDir, "latest/checks");
        latestChecksDir.mkdirs();

        SortedMap<String, List<File>> rawDocs = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (final File f : mdFiles.getFiles()) {
            String cat = f.getAbsoluteFile().getParentFile().getName();
            List<File> catDocs = rawDocs.computeIfAbsent(cat, k -> new ArrayList<>());
            catDocs.add(f.getAbsoluteFile());
        }

        final DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ZZZZ");
        for (Map.Entry<String, List<File>> cat : rawDocs.entrySet()) {
            File mdDir = new File(includesVersionDir, cat.getKey());
            mdDir.mkdir();

            SortedSet<String> frontMatterFileSet = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            for (final File f : cat.getValue()) {
                Files.copy(f.toPath(), new File(mdDir, f.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.COPY_ATTRIBUTES);
                frontMatterFileSet.add("v" + project.getVersion() + "/" + cat.getKey() + "/" + f.getName());
            }

            StringBuilder sb = new StringBuilder();
            sb.append("---\n");
            sb.append("layout: checks\n");

            // current version
            sb.append("check_version: v");
            sb.append(project.getVersion());
            sb.append("\n");

            // current check category
            sb.append("check_category: ");
            sb.append(cat.getKey());
            sb.append("\n");

            // list of all check categories, so Jekyll can construct navigation
            sb.append("check_categories:\n");
            for (String s : rawDocs.keySet()) {
                sb.append("  - ");
                sb.append(s);
                sb.append("\n");
            }

            // list of checks in this category
            sb.append("check_list:\n");
            for (String s : frontMatterFileSet) {
                sb.append("  - ");
                sb.append(s);
                sb.append("\n");
            }

            // timestamp of this file's generation, for sitemap.xml
            sb.append("last_modified_at: ");
            sb.append(sdf.format(buildUtil.getBuildConfig().getBuildTimestamp().get()));
            sb.append("\n");
            sb.append("---\n\n");

            final File catPage = new File(versionChecksDir, cat.getKey() + ".html");
            Files.write(catPage.toPath(), sb.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);

            Files.copy(catPage.toPath(), new File(latestChecksDir, catPage.getName()).toPath(),
                StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
        }
    }



    @InputFiles
    public Provider<FileTree> getMarkdownFiles()
    {
        return markdownFiles;
    }



    @OutputDirectory
    public Provider<File> getSiteDir()
    {
        return siteDir;
    }
}
