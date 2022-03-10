package com.thomasjensen.checkstyle.addons.build.tasks;
/*
 * Checkstyle-Addons - Additional Checkstyle checks
 * Copyright (c) 2015-2020, the Checkstyle Addons contributors
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
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

import org.apache.tools.ant.filters.ReplaceTokens;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.java.archives.Attributes;
import org.gradle.api.java.archives.Manifest;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.internal.jvm.Jvm;
import org.gradle.util.GradleVersion;

import com.thomasjensen.checkstyle.addons.build.BuildConfigExtension;
import com.thomasjensen.checkstyle.addons.build.BuildUtil;
import com.thomasjensen.checkstyle.addons.build.ClasspathBuilder;
import com.thomasjensen.checkstyle.addons.build.DependencyConfig;
import com.thomasjensen.checkstyle.addons.build.TaskCreator;
import com.thomasjensen.checkstyle.addons.build.TaskNames;


/**
 * Gradle task to create the main binary JAR.
 */
public class JarTaskConfigurer
    implements ConfigurableAddonsTask
{
    private final Jar jarTask;



    public JarTaskConfigurer(@Nonnull final Jar pJarTask)
    {
        super();
        jarTask = pJarTask;
    }



    @Override
    public void configureFor(@Nonnull final DependencyConfig pDepConfig)
    {
        final Project project = jarTask.getProject();
        final BuildUtil buildUtil = new BuildUtil(project);
        final BuildConfigExtension buildConfig = buildUtil.getBuildConfig();
        final TaskContainer tasks = project.getTasks();

        jarTask.setGroup(TaskCreator.ARTIFACTS_GROUP_NAME);
        jarTask.setDescription("Assembles a jar archive containing the '" + SourceSet.MAIN_SOURCE_SET_NAME
            + "' classes for dependency configuration '" + pDepConfig.getName() + "'");

        // set appendix for archive name
        final String appendix = pDepConfig.getName();
        if (!pDepConfig.isDefaultConfig()) {
            jarTask.getArchiveAppendix().set(appendix);
        }

        // Dependency on pom.properties generating task
        jarTask.dependsOn(tasks.named(TaskNames.generatePomProperties.getName(pDepConfig)));

        // Task Input: pom.properties file
        final File pomPropsUsed = ((GeneratePomPropsTask)
            tasks.getByName(TaskNames.generatePomProperties.getName(pDepConfig))).getPluginPomProps();
        jarTask.getInputs().file(pomPropsUsed);

        // Dependency on pom.xml generating task
        jarTask.dependsOn(tasks.named(TaskNames.generatePom.getName(pDepConfig)));

        // Task Input: pom.xml
        final Provider<File> pomUsed = ((GeneratePomFileTask)
            tasks.getByName(TaskNames.generatePom.getName(pDepConfig))).getPomFile();
        jarTask.getInputs().file(pomUsed);

        // Dependency on 'classes' task (compile and resources)
        jarTask.dependsOn(buildUtil.getTaskProvider(TaskNames.mainClasses, Task.class, pDepConfig));

        // Configuration of JAR file contents
        jarTask.from(pomUsed);
        final SourceSet mainSourceSet = buildUtil.getSourceSet(SourceSet.MAIN_SOURCE_SET_NAME);
        jarTask.from(new ClasspathBuilder(project).getClassesDirs(mainSourceSet, pDepConfig));
        jarTask.from(mainSourceSet.getOutput().getResourcesDir());

        jarTask.exclude("download-guide.html",
            "**/*.md",
            "**/checks/all_checks.html",
            "eclipsecs-plugin.xml",
            "**/checkstyle-metadata.*");

        jarTask.into("META-INF", copySpec -> copySpec.from("LICENSE"));

        // add generated pom.xml and pom.properties to archive, setting build timestamp in the process
        jarTask.into("META-INF/maven/" + project.getGroup() + "/" + project.getName(), copySpec -> {
            copySpec.from(pomUsed);
            copySpec.from(pomPropsUsed);
            Map<String, String> placeHolders = new HashMap<>();
            placeHolders.put("buildTimestamp", buildConfig.getBuildTimestamp().get().toString());
            Map<String, Object> propsMap = new HashMap<>();
            propsMap.put("tokens", placeHolders);
            copySpec.filter(propsMap, ReplaceTokens.class);
        });

        // Manifest
        String effectiveName = project.getName();
        if (!pDepConfig.isDefaultConfig()) {
            effectiveName += '-' + appendix;
        }
        Manifest manifest = jarTask.getManifest();
        final Attributes attrs = manifest.getAttributes();
        attrs.clear();
        attrs.put("Specification-Title", effectiveName);
        attrs.put("Specification-Vendor", buildConfig.getAuthorName().get());
        attrs.put("Specification-Vendor-Id", "com.thomasjensen");
        attrs.put("Specification-Version", project.getVersion());
        attrs.put("Implementation-Title", effectiveName);
        attrs.put("Implementation-Vendor", buildConfig.getAuthorName().get());
        attrs.put("Implementation-Vendor-Id", "com.thomasjensen");
        attrs.put("Implementation-Version", project.getVersion());
        attrs.put("Implementation-Build", buildConfig.getGitHash().get());
        attrs.put("Checkstyle-Version", pDepConfig.getCheckstyleBaseVersion());
        attrs.putAll(mfAttrStd(project));
        buildUtil.addBuildTimestampDeferred(jarTask);
    }



    /**
     * Build a little map with standard manifest attributes.
     *
     * @param pProject Gradle project
     * @return the map
     */
    public static Map<String, String> mfAttrStd(final Project pProject)
    {
        final BuildConfigExtension buildConfig = new BuildUtil(pProject).getBuildConfig();
        Map<String, String> result = new HashMap<>();
        result.put("Manifest-Version", "1.0");
        result.put("Website", buildConfig.getWebsite().get());
        result.put("Created-By", GradleVersion.current().toString());
        result.put("Built-By", System.getProperty("user.name"));
        result.put("Build-Jdk", Jvm.current().toString());
        return result;
    }
}
