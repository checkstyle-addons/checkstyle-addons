package com.thomasjensen.checkstyle.addons.build;
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

import java.util.Arrays;
import javax.annotation.Nonnull;

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
import org.gradle.api.publish.plugins.PublishingPlugin;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.api.tasks.testing.Test;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.plugins.signing.Sign;
import org.gradle.plugins.signing.SigningExtension;

import com.thomasjensen.checkstyle.addons.build.tasks.JavadocConfigAction;
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

        setVersionFromFile(project);
        final DependencyConfigs depConfigs = new DependencyConfigs(pRootProject);
        new JavaLevelUtil(project).analyzeJavaLevels();
        provideGitHash();
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
        taskCreator.rewirePublishingTasks(depConfigs);

        configurePrintDepConfigsTask(project, depConfigs);
        configureSiteTasks(project);
    }



    private void setVersionFromFile(final Project pRootProject)
    {
        String version = new VersionWrapper(pRootProject).toString();
        pRootProject.setVersion(version);
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



    private void provideGitHash()
    {
        buildUtil.getBuildConfig().getGitHash().set(buildUtil.currentGitCommitHash());
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
        final SigningExtension signExt = pProject.getExtensions().getByType(SigningExtension.class);
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
        }

        // CHECK The use of afterEvaluate() is a workaround here, because the signing plugin does not make use of
        //       configuration avoidance yet, so it causes 20+ tasks to be created that would otherwise not even be
        //       configured. This sucks so much! Ah well, but one day, they'll fix it and then ...
        pProject.afterEvaluate(p -> signExt.sign(publications));

        if (pProject.hasProperty("signing.gnupg.keyName.thomasjensen.com")) {
            pProject.setProperty("signing.gnupg.keyName",
                pProject.getProperties().get("signing.gnupg.keyName.thomasjensen.com"));
        }
        pProject.getTasks().withType(Sign.class).configureEach(signTask ->
            signTask.setGroup(PublishingPlugin.PUBLISH_TASK_GROUP));
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
