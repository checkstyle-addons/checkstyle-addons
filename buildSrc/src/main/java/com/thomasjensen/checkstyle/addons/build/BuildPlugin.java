package com.thomasjensen.checkstyle.addons.build;

import java.util.Arrays;
import javax.annotation.Nonnull;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.api.tasks.testing.Test;

import com.thomasjensen.checkstyle.addons.build.tasks.JavadocTaskConfigurer;
import com.thomasjensen.checkstyle.addons.build.tasks.PrintDepConfigsTask;
import com.thomasjensen.checkstyle.addons.build.tasks.TestTaskConfigurer;


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
        final DependencyConfigs depConfigs = provideDepConfigs(project);
        new JavaLevelUtil(project).analyzeJavaLevels();
        provideGitHash();
        establishSonarQubeSourceSet(project);
        establishGeneralCompileOnlyCfg(project);

        project.getTasks().named(JavaPlugin.TEST_TASK_NAME, Test.class).configure(testTask -> {
            new TestConfigAction().execute(testTask);
            // Since the default test task (and not our own TestTask class) will be used to run the tests in the
            // default dependency configuration, we need to provide it with the same system property.
            testTask.systemProperty(TestTaskConfigurer.CSVERSION_SYSPROP_NAME,
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
        taskCreator.rewirePublishingTasks(depConfigs);

        new SetupSiteTasks(project).registerTasks();
    }



    private void setVersionFromFile(final Project pRootProject)
    {
        String version = new VersionWrapper(pRootProject).toString();
        pRootProject.setVersion(version);
    }



    private DependencyConfigs provideDepConfigs(final Project pRootProject)
    {
        final DependencyConfigs depConfigs = new DependencyConfigs(pRootProject);
        final TaskProvider<PrintDepConfigsTask> printTaskProvider =
            pRootProject.getTasks().register("printDepConfigs", PrintDepConfigsTask.class, depConfigs);
        pRootProject.getTasks().named(JavaBasePlugin.BUILD_TASK_NAME).configure(buildTask ->
            buildTask.dependsOn(printTaskProvider));
        return depConfigs;
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
            new JavadocTaskConfigurer(javadocTask).configureJavadocTask(pDepConfigs.getDefault()));
    }
}
