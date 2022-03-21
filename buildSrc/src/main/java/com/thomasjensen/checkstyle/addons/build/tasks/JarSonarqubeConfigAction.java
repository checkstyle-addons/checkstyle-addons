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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.java.archives.Attributes;
import org.gradle.api.tasks.TaskInputs;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.compile.JavaCompile;

import com.thomasjensen.checkstyle.addons.build.BuildConfigExtension;
import com.thomasjensen.checkstyle.addons.build.BuildUtil;
import com.thomasjensen.checkstyle.addons.build.DependencyConfig;
import com.thomasjensen.checkstyle.addons.build.TaskCreator;
import com.thomasjensen.checkstyle.addons.build.TaskNames;


/**
 * Configure a Gradle task to produce the SonarQube plugin.
 */
public class JarSonarqubeConfigAction
    extends AbstractTaskConfigAction<Jar>
{
    public JarSonarqubeConfigAction(@Nonnull DependencyConfig pDepConfig)
    {
        super(pDepConfig);
    }



    @Override
    protected void configureTaskFor(@Nonnull Jar pJarTask, @Nullable DependencyConfig pDepConfig)
    {
        Objects.requireNonNull(pDepConfig, "required dependency config not present");
        pJarTask.setGroup(TaskCreator.ARTIFACTS_GROUP_NAME);
        pJarTask.setDescription("Assembles the SonarQube plugin for dependency configuration '"
            + pDepConfig.getName() + "'");
        final BuildConfigExtension buildConfig = buildUtil.getBuildConfig();

        // Inputs for up-to-date checking
        final TaskInputs inputs = pJarTask.getInputs();
        inputs.property(BuildUtil.GROUP_ID, project.getGroup());
        inputs.property(BuildUtil.VERSION, project.getVersion());
        inputs.property("name", buildConfig.getLongName());
        inputs.property("description", project.getDescription());
        inputs.property("authorName", buildConfig.getAuthorName());
        inputs.property("sqPluginKey", buildConfig.getSqPluginKey());
        inputs.property("sqPackage", buildConfig.getSqPackage());
        inputs.property("gitHash", buildConfig.getGitHash());
        inputs.property("orgUrl", buildConfig.getOrgUrl());
        inputs.property("issueTrackerUrl", buildConfig.getIssueTrackerUrl());
        inputs.property("website", buildConfig.getWebsite());

        // archive name
        pJarTask.getArchiveFileName().set("sonar-" + inputs.getProperties().get("sqPluginKey") + "-"
            + inputs.getProperties().get(BuildUtil.VERSION)
            + (pDepConfig.isDefaultConfig() ? "" : ("-csp" + pDepConfig.getSonarQubeMinCsPluginVersion()))
            + ".jar");

        // Task Dependencies
        final Jar defaultJarTask = buildUtil.getTask(TaskNames.jar, Jar.class, pDepConfig);
        pJarTask.dependsOn(defaultJarTask);
        final TaskProvider<Task> sqClassesTaskProvider =
            buildUtil.getTaskProvider(TaskNames.sonarqubeClasses, Task.class, pDepConfig);
        pJarTask.dependsOn(sqClassesTaskProvider);
        final VersionFileTask versionTask = (VersionFileTask) project.getTasks().getByName(VersionFileTask.TASK_NAME);
        pJarTask.dependsOn(versionTask);

        // Configuration of JAR file contents
        pJarTask.into("META-INF", copySpec -> copySpec.from("LICENSE"));

        final JavaCompile compileTask =
            buildUtil.getTask(TaskNames.compileSonarqubeJava, JavaCompile.class, pDepConfig);
        pJarTask.from(compileTask.getDestinationDirectory());

        pJarTask.into(inputs.getProperties().get("sqPackage"), copySpec -> copySpec.from(versionTask.getVersionFile()));

        final Set<File> pubLibs = JarEclipseConfigAction.getPublishedDependencyLibs(pJarTask, pDepConfig);
        pJarTask.into("META-INF/lib", copySpec -> {
            copySpec.from(defaultJarTask.getArchiveFile());
            copySpec.from(pubLibs);
        });

        // Manifest
        setManifestAttributes(pJarTask, pDepConfig, pubLibs);
    }



    private void setManifestAttributes(@Nonnull Jar pJarTask, @Nonnull final DependencyConfig pDepConfig,
        @Nonnull final Set<File> pPubLibs)
    {
        final String baseCsVersion = pDepConfig.getCheckstyleBaseVersion();
        final Map<String, Object> inputProps = pJarTask.getInputs().getProperties();
        final Jar thinJarTask = buildUtil.getTask(TaskNames.jar, Jar.class, pDepConfig);
        final Attributes attributes = pJarTask.getManifest().getAttributes();

        attributes.clear();
        attributes.put("Plugin-Name", inputProps.get("name"));
        attributes.put("Plugin-Base", "checkstyle");
        attributes.put("Plugin-Key", inputProps.get("sqPluginKey"));
        attributes.put("Implementation-Build", inputProps.get("gitHash"));
        attributes.put("Plugin-Description", inputProps.get("description")
            + " (based on Checkstyle " + baseCsVersion + ")");
        attributes.put("Plugin-Version", inputProps.get(BuildUtil.VERSION));
        attributes.put("Plugin-Organization", inputProps.get("authorName"));
        attributes.put("Plugin-OrganizationUrl", inputProps.get("orgUrl"));
        attributes.put("Plugin-SourcesUrl", "https://github.com/checkstyle-addons/checkstyle-addons");
        attributes.put("Plugin-IssueTrackerUrl", inputProps.get("issueTrackerUrl"));
        attributes.put("Plugin-Class", "com.thomasjensen.checkstyle.addons.sonarqube.CheckstyleExtensionPlugin");
        attributes.put("Plugin-RequirePlugins", "checkstyle:" + pDepConfig.getSonarQubeMinCsPluginVersion());
        // TODO Use of 'Plugin-Dependencies' mechanism is planned for removal.
        //      Update the plugin Checkstyle Addons [checkstyleaddons] to shade its dependencies instead.
        attributes.put("Plugin-Dependencies", "META-INF/lib/" + thinJarTask.getArchiveFileName().get()
            + (pPubLibs.size() > 0 ? " " : "")
            + JarEclipseConfigAction.flattenPrefixLibs("META-INF/lib/", pPubLibs, ' '));
        attributes.put("Plugin-License", "GPLv3");
        attributes.put("Plugin-Homepage", inputProps.get("website"));
        //attrs.put("Plugin-TermsConditionsUrl", "");
        attributes.put("Sonar-Version", pDepConfig.getSonarQubeMinPlatformVersion());
        attributes.putAll(JarConfigAction.mfAttrStd(project));
        attributes.remove("Website");

        //noinspection Convert2Lambda
        pJarTask.doFirst(new Action<>()
        {
            @Override
            public void execute(@Nonnull final Task pTask)
            {
                DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");   // required by SonarQube
                attributes.put("Plugin-BuildDate", sdf.format(buildUtil.getBuildConfig().getBuildTimestamp().get()));
            }
        });
    }
}
