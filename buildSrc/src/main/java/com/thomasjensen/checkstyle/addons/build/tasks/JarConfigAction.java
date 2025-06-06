package com.thomasjensen.checkstyle.addons.build.tasks;
/*
 * Checkstyle-Addons - Additional Checkstyle checks
 * Copyright (c) 2015-2024, the Checkstyle Addons contributors
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
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.tools.ant.filters.ReplaceTokens;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.java.archives.Attributes;
import org.gradle.api.java.archives.Manifest;
import org.gradle.api.publish.maven.tasks.GenerateMavenPom;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.internal.jvm.Jvm;
import org.gradle.jvm.toolchain.JavaToolchainService;
import org.gradle.util.GradleVersion;

import com.thomasjensen.checkstyle.addons.build.BuildConfigExtension;
import com.thomasjensen.checkstyle.addons.build.BuildUtil;
import com.thomasjensen.checkstyle.addons.build.ClasspathBuilder;
import com.thomasjensen.checkstyle.addons.build.DependencyConfig;
import com.thomasjensen.checkstyle.addons.build.TaskCreator;
import com.thomasjensen.checkstyle.addons.build.TaskNames;


/**
 * Gradle task configuration for the main binary JAR creation task.
 */
public class JarConfigAction
    extends AbstractTaskConfigAction<Jar>
{
    public JarConfigAction(@Nonnull DependencyConfig pDepConfig,
        @Nonnull final JavaToolchainService pJavaToolchainService)
    {
        super(pDepConfig, pJavaToolchainService);
    }



    @Override
    protected void configureTaskFor(@Nonnull Jar pJarTask, @Nullable DependencyConfig pDepConfig)
    {
        Objects.requireNonNull(pDepConfig, "required dependency config not present");
        final BuildConfigExtension buildConfig = buildUtil.getBuildConfig();
        final TaskContainer tasks = project.getTasks();

        pJarTask.setGroup(TaskCreator.ARTIFACTS_GROUP_NAME);
        pJarTask.setDescription("Assembles a jar archive containing the '" + SourceSet.MAIN_SOURCE_SET_NAME
            + "' classes for dependency configuration '" + pDepConfig.getName() + "'");

        // set appendix for archive name
        final String appendix = pDepConfig.getName();
        if (!pDepConfig.isDefaultConfig()) {
            pJarTask.getArchiveAppendix().set(appendix);
        }

        // Dependency on pom.properties generating task
        pJarTask.dependsOn(tasks.named(TaskNames.generatePomProperties.getName(pDepConfig)));

        // Task Input: pom.properties file
        final File pomPropsUsed = ((GeneratePomPropsTask)
            tasks.getByName(TaskNames.generatePomProperties.getName(pDepConfig))).getPluginPomProps();
        pJarTask.getInputs().file(pomPropsUsed);

        // Dependency on pom.xml generating task
        final GenerateMavenPom generatePomTask = buildUtil.getTask(
            TaskNames.generatePomFileForCheckstyleAddonsPublication, GenerateMavenPom.class, pDepConfig);
        pJarTask.dependsOn(generatePomTask);

        // Dependency on 'classes' task (compile and resources)
        pJarTask.dependsOn(buildUtil.getTaskProvider(TaskNames.mainClasses, Task.class, pDepConfig));

        // Configuration of JAR file contents
        pJarTask.from(generatePomTask.getDestination(), copySpec ->
            copySpec.rename(filename -> filename.replace("pom-default.xml", "pom.xml")));
        final SourceSet mainSourceSet = buildUtil.getSourceSet(SourceSet.MAIN_SOURCE_SET_NAME);
        pJarTask.from(new ClasspathBuilder(project).getClassesDirs(mainSourceSet, pDepConfig));
        pJarTask.from(mainSourceSet.getOutput().getResourcesDir());

        pJarTask.exclude("download-guide.html",
            "**/*.md",
            "**/checks/all_checks.html",
            "eclipsecs-plugin.xml",
            "**/checkstyle-metadata.*");

        pJarTask.into("META-INF", copySpec -> copySpec.from("LICENSE"));

        // add generated pom.xml and pom.properties to archive, setting build timestamp in the process
        pJarTask.into("META-INF/maven/" + project.getGroup() + "/" + project.getName(), copySpec -> {
            copySpec.from(generatePomTask.getDestination());
            copySpec.rename(filename -> filename.replace("pom-default.xml", "pom.xml"));
            copySpec.from(pomPropsUsed);
            Map<String, String> placeHolders = new HashMap<>();
            placeHolders.put("buildTimestamp", buildConfig.getBuildTimestamp().get().toString());
            Map<String, Object> propsMap = new HashMap<>();
            propsMap.put("tokens", placeHolders);
            copySpec.filter(propsMap, ReplaceTokens.class);
        });

        // Manifest
        configureManifest(pJarTask, pDepConfig, appendix);
    }



    private void configureManifest(@Nonnull Jar pJarTask, @Nonnull DependencyConfig pDepConfig,
        @Nonnull final String pAppendix)
    {
        final BuildConfigExtension buildConfig = buildUtil.getBuildConfig();
        String effectiveName = project.getName();
        if (!pDepConfig.isDefaultConfig()) {
            effectiveName += '-' + pAppendix;
        }
        Manifest manifest = pJarTask.getManifest();
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
        buildUtil.addBuildTimestampDeferred(pJarTask);
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
