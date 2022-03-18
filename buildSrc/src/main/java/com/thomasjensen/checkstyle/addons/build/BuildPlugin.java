package com.thomasjensen.checkstyle.addons.build;
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
import java.util.Arrays;
import javax.annotation.Nonnull;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.InvalidPatternException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.gradle.api.GradleException;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.publish.PublicationContainer;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.tasks.GenerateMavenPom;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.api.tasks.testing.Test;
import org.gradle.language.base.plugins.LifecycleBasePlugin;

import com.thomasjensen.checkstyle.addons.build.tasks.JavadocConfigAction;
import com.thomasjensen.checkstyle.addons.build.tasks.GeneratePomConfigAction;
import com.thomasjensen.checkstyle.addons.build.tasks.PrintDepConfigsTask;
import com.thomasjensen.checkstyle.addons.build.tasks.SiteCopyAllChecksConfigAction;
import com.thomasjensen.checkstyle.addons.build.tasks.SiteCopyDownloadGuideConfigAction;
import com.thomasjensen.checkstyle.addons.build.tasks.SiteCopyJavadocConfigAction;
import com.thomasjensen.checkstyle.addons.build.tasks.SiteTask;
import com.thomasjensen.checkstyle.addons.build.tasks.TestTaskConfigAction;


/**
 * The main class of the build plugin for Checkstyle Addons.
 */
public class BuildPlugin
    implements Plugin<Project>
{
    /** name of the configuration for compileOnly dependencies which get added to all source sets */
    public static final String GENERAL_COMPILE_ONLY_CONFIG_NAME = "generalCompileOnly";

    public BuildUtil buildUtil = null;



    @Override
    public void apply(final Project pRootProject)
    {
        final Project project = pRootProject.getRootProject();
        buildUtil = new BuildUtil(project);

        project.getExtensions().create("checkstyleAddons", BuildConfigExtension.class, project);

        provideGitInfos(project);
        final DependencyConfigs depConfigs = new DependencyConfigs(pRootProject);
        new JavaLevelUtil(project).analyzeJavaLevels();
        establishSonarQubeSourceSet(project);
        establishGeneralCompileOnlyCfg(project);

        project.getTasks().named(JavaPlugin.TEST_TASK_NAME, Test.class).configure(testTask -> {
            new TestConfigAction().execute(testTask);
            // Since the default test task (and not our own TestTask class) will be used to run the tests in the
            // default dependency configuration, we need to provide it with the same system property.
            testTask.systemProperty(TestTaskConfigAction.CSVERSION_SYSPROP_NAME,
                depConfigs.getDefault().getCheckstyleBaseVersion());
        });

        final TaskCreator taskCreator = new TaskCreator(project);
        for (DependencyConfig depConfig : depConfigs.getAll().values()) {
            if (!depConfig.isDefaultConfig()) {
                taskCreator.setupBuildTasks(depConfig);
            }
        }
        taskCreator.setupCrossCheckTasks(depConfigs);
        taskCreator.adjustTaskGroupAssignments();

        configureDefaultJavadocTask(project, depConfigs);
        taskCreator.setupArtifactTasks(depConfigs);
        registerPublications(project, depConfigs);
        configureAssembleAllTask(project, depConfigs);

        configurePrintDepConfigsTask(project, depConfigs);
        configureSiteTasks(project);
    }



    private void configurePrintDepConfigsTask(@Nonnull final Project pRootProject,
        @Nonnull final DependencyConfigs pDepConfigs)
    {
        final TaskProvider<PrintDepConfigsTask> printTaskProvider =
            pRootProject.getTasks().register("printDepConfigs", PrintDepConfigsTask.class, pDepConfigs);
        pRootProject.getTasks().named(JavaBasePlugin.BUILD_TASK_NAME).configure(buildTask ->
            buildTask.dependsOn(printTaskProvider));

        // The 'printDepConfigs' task should run first.
        for (DependencyConfig depConfig : pDepConfigs.getAll().values()) {
            if (!depConfig.isDefaultConfig()) {
                buildUtil.getTaskProvider(TaskNames.assemble, Task.class, depConfig).configure(assembleTask ->
                    assembleTask.shouldRunAfter(printTaskProvider));
            }
        }
        pRootProject.getTasks().named(LifecycleBasePlugin.ASSEMBLE_TASK_NAME).configure(assembleTask ->
            assembleTask.shouldRunAfter(printTaskProvider));
        pRootProject.getTasks().named(LifecycleBasePlugin.CHECK_TASK_NAME).configure(checkTask ->
            checkTask.shouldRunAfter(printTaskProvider));
    }



    private void provideGitInfos(final Project pProject)
    {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        try (
            Repository gitRepo = builder.setGitDir(new File(pProject.getRootDir(), ".git")).build()
        ) {
            String hash = gitRepo.resolve("HEAD").getName();
            pProject.getLogger().info("Detected current Git commit hash as: " + hash);
            buildUtil.getBuildConfig().getGitHash().set(hash);

            final Git git = new Git(gitRepo);
            String version = getGitVersion(pProject, git);
            pProject.setVersion(version);
        }
        catch (GitAPIException | InvalidPatternException | IOException | RuntimeException e) {
            throw new GradleException("Failed to interact with local Git repository: " + e.getMessage(), e);
        }
    }



    private String getGitVersion(@Nonnull final Project pProject, @Nonnull final Git pGit)
        throws GitAPIException, InvalidPatternException
    {
        // We basically perform: git describe --tags "--match=v*" --always --dirty | cut -c 2-
        final boolean clean = pGit.status().call().isClean();
        String version = pGit.describe().setTags(true).setMatch("v*").setAlways(true).call()
            + (clean ? "" : "-dirty");
        if (version.charAt(0) == 'v') {
            version = version.substring(1);
        }
        pProject.getLogger().info("Determined Git version as: " + version);
        return version;
    }



    private void establishSonarQubeSourceSet(final Project pRootProject)
    {
        final JavaPluginExtension javaExt = pRootProject.getExtensions().getByType(JavaPluginExtension.class);
        final SourceSetContainer sourceSets = javaExt.getSourceSets();
        final ConfigurationContainer configs = pRootProject.getConfigurations();

        final SourceSet testSourceSet = sourceSets.getByName(SourceSet.TEST_SOURCE_SET_NAME);
        final SourceSet sqSourceSet = sourceSets.create(BuildUtil.SONARQUBE_SOURCE_SET_NAME);

        configs.named(testSourceSet.getImplementationConfigurationName()).configure(testImplementation ->
            testImplementation.extendsFrom(configs.getByName(sqSourceSet.getImplementationConfigurationName())));
        configs.named(testSourceSet.getRuntimeOnlyConfigurationName()).configure(testRuntimeOnly ->
            testRuntimeOnly.extendsFrom(configs.getByName(sqSourceSet.getRuntimeOnlyConfigurationName())));

        final TaskContainer tasks = pRootProject.getTasks();
        tasks.named(JavaPlugin.COMPILE_TEST_JAVA_TASK_NAME).configure(compileTestJavaTask ->
            compileTestJavaTask.dependsOn(tasks.named(sqSourceSet.getClassesTaskName())));

        final FileCollection sqOutputs = sqSourceSet.getOutput().getClassesDirs().plus(
            pRootProject.files(sqSourceSet.getOutput().getResourcesDir()));
        testSourceSet.setCompileClasspath(testSourceSet.getCompileClasspath().plus(sqOutputs));
        testSourceSet.setRuntimeClasspath(testSourceSet.getRuntimeClasspath().plus(sqOutputs));
    }



    private void establishGeneralCompileOnlyCfg(final Project pRootProject)
    {
        final ConfigurationContainer configs = pRootProject.getConfigurations();
        final Configuration generalCompileOnly = configs.create(GENERAL_COMPILE_ONLY_CONFIG_NAME);

        Arrays.asList(SourceSet.MAIN_SOURCE_SET_NAME, BuildUtil.SONARQUBE_SOURCE_SET_NAME,
            SourceSet.TEST_SOURCE_SET_NAME).forEach((@Nonnull final String pSourceSetName) -> {
            final SourceSet sourceSet = buildUtil.getSourceSet(pSourceSetName);
            configs.named(sourceSet.getCompileOnlyConfigurationName()).configure(compileOnly ->
                compileOnly.extendsFrom(generalCompileOnly));
        });
    }



    private void configureDefaultJavadocTask(final Project pRootProject, final DependencyConfigs pDepConfigs)
    {
        pRootProject.getTasks().named("javadoc", Javadoc.class).configure(javadocTask ->
            new JavadocConfigAction(pDepConfigs.getDefault())
                .configureJavadocTask(javadocTask, pDepConfigs.getDefault()));
    }



    private void registerPublications(@Nonnull final Project pProject, @Nonnull final DependencyConfigs pDepConfigs)
    {
        final PublishingExtension pubExt = pProject.getExtensions().getByType(PublishingExtension.class);
        final PublicationContainer publications = pubExt.getPublications();

        for (DependencyConfig depConfig : pDepConfigs.getAll().values()) {
            final String pubSuffix = depConfig.getName();
            final NamedDomainObjectProvider<MavenPublication> pubProvider =
                publications.register(depConfig.getPublicationName(), MavenPublication.class);
            pubProvider.configure((MavenPublication pub) ->
            {
                final String artifactId = pProject.getName() + (depConfig.isDefaultConfig() ? "" : ("-" + pubSuffix));
                pub.setArtifactId(artifactId);

                final TaskProvider<Jar> jarTaskProvider =
                    buildUtil.getTaskProvider(TaskNames.jar, Jar.class, depConfig);
                pub.artifact(jarTaskProvider);

                final TaskProvider<Jar> jarSrcTaskProvider =
                    buildUtil.getTaskProvider(TaskNames.jarSources, Jar.class, depConfig);
                pub.artifact(jarSrcTaskProvider, a -> a.setClassifier("sources"));

                final TaskProvider<Jar> jarJavadocTaskProvider =
                    buildUtil.getTaskProvider(TaskNames.jarJavadoc, Jar.class, depConfig);
                pub.artifact(jarJavadocTaskProvider, a -> a.setClassifier("javadoc"));
            });

            buildUtil.getTaskProvider(TaskNames.generatePomFileForCheckstyleAddonsPublication, GenerateMavenPom.class,
                depConfig).configure(new GeneratePomConfigAction(depConfig));
        }

        if (pProject.hasProperty("signing.gnupg.keyName.thomasjensen.com")) {
            pProject.setProperty("signing.gnupg.keyName",
                pProject.getProperties().get("signing.gnupg.keyName.thomasjensen.com"));
        }
    }



    private void configureAssembleAllTask(@Nonnull final Project pProject, @Nonnull final DependencyConfigs pDepConfigs)
    {
        pProject.getTasks().register("assembleAll").configure(task -> {
            String longName = buildUtil.getBuildConfig().getLongName().get();
            task.setGroup(TaskCreator.ARTIFACTS_GROUP_NAME);
            task.setDescription("Assembles all " + longName + " artifacts from all dependency configurations");
            for (DependencyConfig depConfig : pDepConfigs.getAll().values()) {
                task.dependsOn(buildUtil.getTaskProvider(TaskNames.assemble, Task.class, depConfig));
            }
        });
    }



    private void configureSiteTasks(@Nonnull final Project pRootProject)
    {
        pRootProject.getTasks().register("siteCopyJavadoc", Copy.class)
            .configure(new SiteCopyJavadocConfigAction());
        pRootProject.getTasks().register("siteCopyAllChecks", Copy.class)
            .configure(new SiteCopyAllChecksConfigAction());
        pRootProject.getTasks().register("siteCopyDownloadGuide", Copy.class)
            .configure(new SiteCopyDownloadGuideConfigAction());
        pRootProject.getTasks().register("site", SiteTask.class)
            .configure(SiteTask::configureTask);
    }
}
