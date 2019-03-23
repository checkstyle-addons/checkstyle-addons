package com.thomasjensen.checkstyle.addons.build;
/*
 * Checkstyle-Addons - Additional Checkstyle checks
 * Copyright (c) 2015-2018, Thomas Jensen and the Checkstyle Addons contributors
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
import java.util.Arrays;
import java.util.Map;
import javax.annotation.Nonnull;

import com.github.spotbugs.SpotBugsTask;
import groovy.lang.Closure;
import org.gradle.api.JavaVersion;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.plugins.quality.Checkstyle;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.tasks.GenerateMavenPom;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.language.base.plugins.LifecycleBasePlugin;

import com.thomasjensen.checkstyle.addons.build.tasks.CompileTask;
import com.thomasjensen.checkstyle.addons.build.tasks.CreateFatJarTask;
import com.thomasjensen.checkstyle.addons.build.tasks.CreateJarEclipseTask;
import com.thomasjensen.checkstyle.addons.build.tasks.CreateJarJavadocTask;
import com.thomasjensen.checkstyle.addons.build.tasks.CreateJarSonarqubeTask;
import com.thomasjensen.checkstyle.addons.build.tasks.CreateJarSourcesTask;
import com.thomasjensen.checkstyle.addons.build.tasks.CreateJarTask;
import com.thomasjensen.checkstyle.addons.build.tasks.GeneratePomFileTask;
import com.thomasjensen.checkstyle.addons.build.tasks.GeneratePomPropsTask;
import com.thomasjensen.checkstyle.addons.build.tasks.JavadocTask;
import com.thomasjensen.checkstyle.addons.build.tasks.TestTask;


/**
 * Creates and wires a whole bunch of Gradle tasks required by this complex build.
 */
public class TaskCreator
{
    /** name of the task group of the artifact creation / assembly tasks */
    public static final String ARTIFACTS_GROUP_NAME = "artifacts";

    /** name of the task group of the cross-check tasks */
    public static final String XTEST_GROUP_NAME = "xtest";

    /** name of the task bundling all the xtest tasks */
    public static final String XTEST_TASK_NAME = "xtest";

    /** name of the configuration for compileOnly dependencies which get added to all source sets */
    public static final String GENERAL_COMPILE_ONLY_CONFIG_NAME = "generalCompileOnly";

    private final Project project;

    private final BuildUtil buildUtil;



    /**
     * Constructor.
     *
     * @param pProject the Gradle root project
     */
    public TaskCreator(@Nonnull final Project pProject)
    {
        project = pProject;
        buildUtil = new BuildUtil(pProject);
    }



    public void establishSonarQubeSourceSet()
    {
        final JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
        final SourceSetContainer sourceSets = javaConvention.getSourceSets();
        final ConfigurationContainer configs = project.getConfigurations();

        final SourceSet testSourceSet = sourceSets.getByName(SourceSet.TEST_SOURCE_SET_NAME);
        final SourceSet sqSourceSet = sourceSets.create(BuildUtil.SONARQUBE_SOURCE_SET_NAME);

        configs.getByName(testSourceSet.getImplementationConfigurationName()).extendsFrom(
            configs.getByName(sqSourceSet.getImplementationConfigurationName()));
        configs.getByName(testSourceSet.getRuntimeOnlyConfigurationName()).extendsFrom(
            configs.getByName(sqSourceSet.getRuntimeOnlyConfigurationName()));

        final TaskContainer tasks = project.getTasks();
        tasks.getByName(JavaPlugin.COMPILE_TEST_JAVA_TASK_NAME).dependsOn(
            tasks.getByName(sqSourceSet.getClassesTaskName()));

        final FileCollection sqOutputs = sqSourceSet.getOutput().getClassesDirs().plus(
            project.files(sqSourceSet.getOutput().getResourcesDir()));
        testSourceSet.setCompileClasspath(testSourceSet.getCompileClasspath().plus(sqOutputs));
        testSourceSet.setRuntimeClasspath(testSourceSet.getRuntimeClasspath().plus(sqOutputs));
    }



    public void establishGeneralCompileOnlyCfg()
    {
        final JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
        final SourceSetContainer sourceSets = javaConvention.getSourceSets();
        final ConfigurationContainer configs = project.getConfigurations();

        final Configuration generalCompileOnly = configs.create(GENERAL_COMPILE_ONLY_CONFIG_NAME);

        Arrays.asList(SourceSet.MAIN_SOURCE_SET_NAME, BuildUtil.SONARQUBE_SOURCE_SET_NAME,
            SourceSet.TEST_SOURCE_SET_NAME).forEach((@Nonnull final String pSourceSetName) -> {
            final SourceSet sourceSet = sourceSets.getByName(pSourceSetName);
            configs.getByName(sourceSet.getCompileOnlyConfigurationName()).extendsFrom(generalCompileOnly);
        });
    }



    public void setupBuildTasks(@Nonnull final DependencyConfig pDepConfig)
    {
        final TaskContainer tasks = project.getTasks();

        // compile, classes
        setupCompileTaskForSourceSet(pDepConfig, SourceSet.MAIN_SOURCE_SET_NAME, TaskNames.compileJava,
            TaskNames.mainClasses);
        setupCompileTaskForSourceSet(pDepConfig, BuildUtil.SONARQUBE_SOURCE_SET_NAME, TaskNames.compileSonarqubeJava,
            TaskNames.sonarqubeClasses);
        setupCompileTaskForSourceSet(pDepConfig, SourceSet.TEST_SOURCE_SET_NAME, TaskNames.compileTestJava,
            TaskNames.testClasses);

        // test
        final TestTask testTask = tasks.create(TaskNames.test.getName(pDepConfig), TestTask.class);
        testTask.configureFor(pDepConfig, pDepConfig.getCheckstyleBaseVersion());

        // javadoc
        final JavadocTask javadocTask = tasks.create(TaskNames.javadoc.getName(pDepConfig), JavadocTask.class);
        javadocTask.configureFor(pDepConfig);
    }



    private void setupCompileTaskForSourceSet(@Nonnull final DependencyConfig pDepConfig,
        @Nonnull final String pSourceSetName, @Nonnull final TaskNames pCompileTaskName,
        @Nonnull final TaskNames pClassesTaskName)
    {
        final TaskContainer tasks = project.getTasks();
        final boolean isTest = SourceSet.TEST_SOURCE_SET_NAME.equals(pSourceSetName);

        final SourceSet sourceSet = buildUtil.getSourceSet(pSourceSetName);
        final CompileTask compileTask = tasks.create(pCompileTaskName.getName(pDepConfig), CompileTask.class);
        compileTask.configureFor(pDepConfig, sourceSet, isTest);

        final Task classesTask = tasks.create(pClassesTaskName.getName(pDepConfig));
        classesTask.setDescription("Assembles '" + pSourceSetName + "' classes for dependency configuration '"
                + pDepConfig.getName() + "'");
        classesTask.setGroup(BasePlugin.BUILD_GROUP);
        classesTask.dependsOn(compileTask, tasks.getByName(sourceSet.getProcessResourcesTaskName()));
    }



    /**
     * Set up cross-check feature. We provide an 'xcheck' task which depends on a number of Test tasks that run the
     * unit tests compiled against every Checkstyle version against all the other Checkstyle libraries. In this way, we
     * find out which versions are compatible.
     */
    public void setupCrossCheckTasks()
    {
        final TaskContainer tasks = project.getTasks();

        final Task xtest = tasks.create(XTEST_TASK_NAME);
        xtest.setGroup(XTEST_GROUP_NAME);
        xtest.setDescription("Run the unit tests against all supported Checkstyle runtimes");
        tasks.getByName(JavaBasePlugin.BUILD_TASK_NAME).dependsOn(xtest);

        for (final DependencyConfig depConfig : buildUtil.getDepConfigs().getAll().values()) {
            final JavaVersion javaLevel = depConfig.getJavaLevel();
            final String csBaseVersion = depConfig.getCheckstyleBaseVersion();
            for (final String csRuntimeVersion : depConfig.getCompatibleCheckstyleVersions()) {
                if (csBaseVersion.equals(csRuntimeVersion)) {
                    continue;
                }

                final TestTask testTask = tasks.create(TaskNames.xtest.getName(depConfig, csRuntimeVersion),
                    TestTask.class);
                testTask.configureFor(depConfig, csRuntimeVersion);
                testTask.setGroup(XTEST_GROUP_NAME);
                testTask.setDescription("Run the unit tests compiled for Checkstyle " + csBaseVersion
                        + " against a Checkstyle " + csRuntimeVersion + " runtime (Java level: " + javaLevel + ")");
                testTask.getReports().getHtml().setEnabled(false);
                xtest.dependsOn(testTask);
            }
        }
    }



    public void setupArtifactTasks()
    {
        final TaskContainer tasks = project.getTasks();

        for (final DependencyConfig depConfig : buildUtil.getDepConfigs().getAll().values()) {
            final String pubSuffix = depConfig.getName();

            // 'generatePomProperties' task
            final String pomPropsTaskName = TaskNames.generatePomProperties.getName(depConfig);
            final GeneratePomPropsTask pomPropsTask = tasks.create(pomPropsTaskName, GeneratePomPropsTask.class);
            pomPropsTask.setAppendix(depConfig.isDefaultConfig() ? null : pubSuffix);

            // 'generatePom' task
            final GeneratePomFileTask generatePomTask = tasks.create(TaskNames.generatePom.getName(depConfig),
                GeneratePomFileTask.class);
            generatePomTask.configureFor(depConfig);

            // 'jar' task
            final String jarTaskName = TaskNames.jar.getName(depConfig);
            final CreateJarTask jarTask = tasks.create(jarTaskName, CreateJarTask.class);
            jarTask.configureFor(depConfig);

            // 'fatjar' task
            final String fatjarTaskName = TaskNames.fatJar.getName(depConfig);
            final CreateFatJarTask fatjarTask = tasks.create(fatjarTaskName, CreateFatJarTask.class);
            fatjarTask.configureFor(depConfig);

            // 'jarSources' task
            final String jarSourcesTaskName = TaskNames.jarSources.getName(depConfig);
            final CreateJarSourcesTask jarSourcesTask = tasks.create(jarSourcesTaskName, CreateJarSourcesTask.class);
            jarSourcesTask.configureFor(depConfig);

            // 'jarJavadoc' task
            final String jarJavadocTaskName = TaskNames.jarJavadoc.getName(depConfig);
            final CreateJarJavadocTask jarJavadocTask = tasks.create(jarJavadocTaskName, CreateJarJavadocTask.class);
            jarJavadocTask.configureFor(depConfig);

            // Add JARs to list of artifacts to publish
            String pubName = "checkstyleAddons";
            if (!depConfig.isDefaultConfig()) {
                pubName += '-' + pubSuffix;
            }
            final PublishingExtension publishing = (PublishingExtension) project.getExtensions().getByName(
                PublishingExtension.NAME);
            final MavenPublication pub = publishing.getPublications().create(pubName, MavenPublication.class);
            final String pubArtifactId = project.getName() + (depConfig.isDefaultConfig() ? "" : ("-" + pubSuffix));
            pub.setArtifactId(pubArtifactId);
            pub.artifact(jarTask);
            pub.artifact(jarSourcesTask);
            pub.artifact(jarJavadocTask);

            // 'jarEclipse' task
            final String eclipseTaskName = TaskNames.jarEclipse.getName(depConfig);
            final CreateJarEclipseTask jarEclipseTask = tasks.create(eclipseTaskName, CreateJarEclipseTask.class);
            jarEclipseTask.configureFor(depConfig);

            // 'jarSonarqube' task
            CreateJarSonarqubeTask jarSqTask = null;
            if (depConfig.isSonarQubeSupported()) {
                final String sqTaskName = TaskNames.jarSonarqube.getName(depConfig);
                jarSqTask = tasks.create(sqTaskName, CreateJarSonarqubeTask.class);
                jarSqTask.configureFor(depConfig);
            }

            // 'assemble' task for the dependency configuration
            Task assembleTask = tasks.getByName(BasePlugin.ASSEMBLE_TASK_NAME);
            if (!depConfig.isDefaultConfig()) {
                assembleTask = tasks.create(TaskNames.assemble.getName(depConfig));
                assembleTask.setDescription("Assembles the artifacts belonging to dependency configuration '"
                        + depConfig.getName() + "'");
                final Task buildTask = tasks.getByName(LifecycleBasePlugin.BUILD_TASK_NAME);
                buildTask.dependsOn(assembleTask);
            }
            assembleTask.setGroup(ARTIFACTS_GROUP_NAME);
            assembleTask.dependsOn(jarTask);
            assembleTask.dependsOn(fatjarTask);
            assembleTask.dependsOn(jarSourcesTask);
            assembleTask.dependsOn(jarJavadocTask);
            assembleTask.dependsOn(jarEclipseTask);
            if (jarSqTask != null) {
                assembleTask.dependsOn(jarSqTask);
            }
        }

        // disable standard 'jar' task
        tasks.getByName(JavaPlugin.JAR_TASK_NAME).setEnabled(false);
    }



    /**
     * Assign some standard tasks and tasks created by third-party plugins to their task groups according to the
     * order of things for Checkstyle Addons.
     */
    public void adjustTaskGroupAssignments()
    {
        final TaskContainer tasks = project.getTasks();
        tasks.getByName(BasePlugin.ASSEMBLE_TASK_NAME).setGroup(ARTIFACTS_GROUP_NAME);
        tasks.getByName(JavaPlugin.JAR_TASK_NAME).setGroup(ARTIFACTS_GROUP_NAME);

        final SourceSet sqSourceSet = buildUtil.getSourceSet(BuildUtil.SONARQUBE_SOURCE_SET_NAME);
        tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).setGroup(BasePlugin.BUILD_GROUP);
        tasks.getByName(sqSourceSet.getCompileJavaTaskName()).setGroup(BasePlugin.BUILD_GROUP);
        tasks.getByName(JavaPlugin.COMPILE_TEST_JAVA_TASK_NAME).setGroup(BasePlugin.BUILD_GROUP);

        for (final SpotBugsTask sbTask : tasks.withType(SpotBugsTask.class)) {
            sbTask.setGroup(LifecycleBasePlugin.VERIFICATION_GROUP);
        }
        for (final Checkstyle csTask : tasks.withType(Checkstyle.class)) {
            csTask.setGroup(LifecycleBasePlugin.VERIFICATION_GROUP);
        }
        for (final Copy task : tasks.withType(Copy.class)) {
            if (task.getName().startsWith("process") && task.getName().endsWith("Resources")) {
                task.setGroup(LifecycleBasePlugin.BUILD_GROUP);
            }
        }
    }



    public void rewirePublishingTasks(@Nonnull final Object pScript)
    {
        final TaskContainer tasks = project.getTasks();
        final DependencyConfigs depConfigs = buildUtil.getDepConfigs();

        tasks.all(new Closure<Void>(pScript)
        {
            @Override
            @SuppressWarnings("MethodDoesntCallSuperMethod")
            public Void call(final Object... pArgs)
            {
                final Task task = (Task) getDelegate();
                for (final Map.Entry<String, DependencyConfig> entry : depConfigs.getPublications().entrySet()) {
                    final String pubNameCap = Character.toUpperCase(entry.getKey().charAt(0)) + entry.getKey()
                        .substring(1);
                    final DependencyConfig depConfig = entry.getValue();

                    // local publication depends on the pom.xml
                    if (task.getName().endsWith("PublicationToMavenLocal")) {
                        final String taskName = "publish" + pubNameCap + "PublicationToMavenLocal";
                        if (taskName.equals(task.getName())) {
                            task.dependsOn(buildUtil.getTask(TaskNames.generatePom, depConfig));
                        }
                    }

                    // the default task for POM creation is replaced by our own
                    else if (task.getName().startsWith("generatePomFileFor")) {
                        final String taskName = "generatePomFileFor" + pubNameCap + "Publication";
                        if (taskName.equals(task.getName())) {
                            task.setEnabled(false);  // we do this manually
                            ((GenerateMavenPom) task).setDestination(new File(project.getBuildDir(),
                                "tmp/" + TaskNames.generatePom.getName(depConfig) + "/pom.xml"));
                        }
                    }
                }
                return null;
            }
        });
    }
}
