package com.thomasjensen.checkstyle.addons.build;
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

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar;
import org.gradle.api.JavaVersion;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.quality.Checkstyle;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.api.tasks.testing.Test;
import org.gradle.language.base.plugins.LifecycleBasePlugin;

import com.thomasjensen.checkstyle.addons.build.tasks.CompileConfigAction;
import com.thomasjensen.checkstyle.addons.build.tasks.FatJarConfigAction;
import com.thomasjensen.checkstyle.addons.build.tasks.GeneratePomPropsTask;
import com.thomasjensen.checkstyle.addons.build.tasks.JarConfigAction;
import com.thomasjensen.checkstyle.addons.build.tasks.JarEclipseConfigAction;
import com.thomasjensen.checkstyle.addons.build.tasks.JarJavadocConfigAction;
import com.thomasjensen.checkstyle.addons.build.tasks.JarSonarqubeConfigAction;
import com.thomasjensen.checkstyle.addons.build.tasks.JarSourcesConfigAction;
import com.thomasjensen.checkstyle.addons.build.tasks.JavadocConfigAction;
import com.thomasjensen.checkstyle.addons.build.tasks.TestTaskConfigAction;


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
        final TaskProvider<Test> testTaskProvider = tasks.register(TaskNames.test.getName(pDepConfig), Test.class);
        testTaskProvider.configure(new TestTaskConfigAction(pDepConfig, pDepConfig.getCheckstyleBaseVersion()));
        final TaskProvider<Task> checkTaskProvider = tasks.named(JavaBasePlugin.CHECK_TASK_NAME);
        checkTaskProvider.configure(checkTask -> checkTask.dependsOn(testTaskProvider));

        // javadoc
        final TaskProvider<Javadoc> javadocTaskProvider =
            tasks.register(TaskNames.javadoc.getName(pDepConfig), Javadoc.class);
        javadocTaskProvider.configure(new JavadocConfigAction(pDepConfig));
    }



    private void setupCompileTaskForSourceSet(@Nonnull final DependencyConfig pDepConfig,
        @Nonnull final String pSourceSetName, @Nonnull final TaskNames pCompileTaskName,
        @Nonnull final TaskNames pClassesTaskName)
    {
        final TaskContainer tasks = project.getTasks();

        final SourceSet sourceSet = buildUtil.getSourceSet(pSourceSetName);
        final TaskProvider<JavaCompile> compileTaskProvider =
            tasks.register(pCompileTaskName.getName(pDepConfig), JavaCompile.class);
        compileTaskProvider.configure(new CompileConfigAction(pDepConfig, sourceSet));

        final TaskProvider<Task> classesTaskProvider = tasks.register(pClassesTaskName.getName(pDepConfig));
        classesTaskProvider.configure(classesTask -> {
            classesTask.setDescription("Assembles '" + pSourceSetName + "' classes for dependency configuration '"
                + pDepConfig.getName() + "'");
            classesTask.setGroup(BasePlugin.BUILD_GROUP);
            classesTask.dependsOn(compileTaskProvider, tasks.named(sourceSet.getProcessResourcesTaskName()));
        });
    }



    /**
     * Set up cross-check feature. We provide an 'xcheck' task which depends on a number of Test tasks that run the
     * unit tests compiled against every Checkstyle version against all the other Checkstyle libraries. In this way, we
     * make sure that Checkstyle Addons is compatible with all versions of Checkstyle.
     *
     * @param pDepConfigs the dependency configs
     */
    public void setupCrossCheckTasks(@Nonnull final DependencyConfigs pDepConfigs)
    {
        final TaskContainer tasks = project.getTasks();

        final List<TaskProvider<Test>> crossCheckTasks = new ArrayList<>();
        for (final DependencyConfig depConfig : pDepConfigs.getAll().values()) {
            final JavaVersion javaLevel = depConfig.getJavaLevel();
            final String csBaseVersion = depConfig.getCheckstyleBaseVersion();
            for (final String csRuntimeVersion : depConfig.getCompatibleCheckstyleVersions()) {
                if (csBaseVersion.equals(csRuntimeVersion)) {
                    continue;
                }

                final TaskProvider<Test> testTaskProvider =
                    tasks.register(TaskNames.xtest.getName(depConfig, csRuntimeVersion), Test.class);
                testTaskProvider.configure(new TestTaskConfigAction(depConfig, csRuntimeVersion));
                testTaskProvider.configure(testTask -> {
                    testTask.setGroup(XTEST_GROUP_NAME);
                    testTask.setDescription("Run the unit tests compiled for Checkstyle " + csBaseVersion
                        + " against a Checkstyle " + csRuntimeVersion + " runtime (Java level: " + javaLevel + ")");
                    // No test report needed in build/reports/tests:
                    testTask.getReports().getHtml().getRequired().set(Boolean.FALSE);
                });
                crossCheckTasks.add(testTaskProvider);
            }
        }

        final TaskProvider<Task> xtestProvider = tasks.register(XTEST_TASK_NAME);
        xtestProvider.configure(xtest -> {
            xtest.setGroup(XTEST_GROUP_NAME);
            xtest.setDescription("Run the unit tests against all supported Checkstyle runtimes");
            xtest.setDependsOn(crossCheckTasks);
        });
        tasks.named(JavaBasePlugin.BUILD_TASK_NAME).configure(buildTask -> buildTask.dependsOn(xtestProvider));
    }



    public void setupArtifactTasks(@Nonnull final DependencyConfigs pDepConfigs)
    {
        final TaskContainer tasks = project.getTasks();

        for (final DependencyConfig depConfig : pDepConfigs.getAll().values()) {
            final String pubSuffix = depConfig.getName();

            // 'generatePomProperties' task
            final String pomPropsTaskName = TaskNames.generatePomProperties.getName(depConfig);
            final TaskProvider<GeneratePomPropsTask> pomPropsTaskTaskProvider =
                tasks.register(pomPropsTaskName, GeneratePomPropsTask.class);
            pomPropsTaskTaskProvider.configure(pomPropsTask -> {
                pomPropsTask.getAppendix().set(depConfig.isDefaultConfig() ? null : pubSuffix);
                pomPropsTask.updateDescription();
            });

            // 'jar' task
            final String jarTaskName = TaskNames.jar.getName(depConfig);
            final TaskProvider<Jar> jarTaskProvider = tasks.register(jarTaskName, Jar.class);
            jarTaskProvider.configure(new JarConfigAction(depConfig));

            // 'fatjar' task
            final String fatjarTaskName = TaskNames.fatJar.getName(depConfig);
            final TaskProvider<ShadowJar> shadowJarTaskProvider = tasks.register(fatjarTaskName, ShadowJar.class);
            shadowJarTaskProvider.configure(new FatJarConfigAction(depConfig));

            // 'jarSources' task
            final String jarSourcesTaskName = TaskNames.jarSources.getName(depConfig);
            final TaskProvider<Jar> jarSourcesTaskProvider = tasks.register(jarSourcesTaskName, Jar.class);
            jarSourcesTaskProvider.configure(new JarSourcesConfigAction(depConfig));

            // 'jarJavadoc' task
            final String jarJavadocTaskName = TaskNames.jarJavadoc.getName(depConfig);
            final TaskProvider<Jar> jarJavadocTaskProvider = tasks.register(jarJavadocTaskName, Jar.class);
            jarJavadocTaskProvider.configure(new JarJavadocConfigAction(depConfig));

            // 'jarEclipse' task
            final String eclipseTaskName = TaskNames.jarEclipse.getName(depConfig);
            final TaskProvider<Jar> jarEclipseTaskProvider = tasks.register(eclipseTaskName, Jar.class);
            jarEclipseTaskProvider.configure(new JarEclipseConfigAction(depConfig));

            // 'jarSonarqube' task
            TaskProvider<ShadowJar> jarSqTaskProvider = null;
            if (depConfig.isSonarQubeSupported()) {
                final String sqTaskName = TaskNames.jarSonarqube.getName(depConfig);
                jarSqTaskProvider = tasks.register(sqTaskName, ShadowJar.class);
                jarSqTaskProvider.configure(new JarSonarqubeConfigAction(depConfig));
            }
            final TaskProvider<ShadowJar> jarSqTaskProviderFinal = jarSqTaskProvider;

            // 'assemble' task for the dependency configuration
            TaskProvider<Task> assembleTaskProvider = null;
            if (depConfig.isDefaultConfig()) {
                assembleTaskProvider = tasks.named(BasePlugin.ASSEMBLE_TASK_NAME);
            }
            else {
                assembleTaskProvider = tasks.register(TaskNames.assemble.getName(depConfig));
                assembleTaskProvider.configure(assembleTask ->
                    assembleTask.setDescription("Assembles the artifacts belonging to dependency configuration '"
                        + depConfig.getName() + "'"));
                final TaskProvider<Task> assembleTaskProviderFinal = assembleTaskProvider;
                final TaskProvider<Task> buildTaskProvider = tasks.named(JavaBasePlugin.BUILD_TASK_NAME);
                buildTaskProvider.configure(buildTask -> buildTask.dependsOn(assembleTaskProviderFinal));
            }
            assembleTaskProvider.configure(assembleTask -> {
                assembleTask.setGroup(ARTIFACTS_GROUP_NAME);
                assembleTask.dependsOn(jarTaskProvider);
                assembleTask.dependsOn(shadowJarTaskProvider);
                assembleTask.dependsOn(jarSourcesTaskProvider);
                assembleTask.dependsOn(jarJavadocTaskProvider);
                assembleTask.dependsOn(jarEclipseTaskProvider);
                if (jarSqTaskProviderFinal != null) {
                    assembleTask.dependsOn(jarSqTaskProviderFinal);
                }
            });
        }

        // disable standard 'jar' task
        tasks.named(JavaPlugin.JAR_TASK_NAME).configure(t -> t.setEnabled(false));
    }



    /**
     * Assign some standard tasks and tasks created by third-party plugins to their task groups according to the
     * order of things for Checkstyle Addons.
     */
    public void adjustTaskGroupAssignments()
    {
        final TaskContainer tasks = project.getTasks();
        tasks.named(BasePlugin.ASSEMBLE_TASK_NAME).configure(t -> t.setGroup(ARTIFACTS_GROUP_NAME));
        tasks.named(JavaPlugin.JAR_TASK_NAME).configure(t -> t.setGroup(ARTIFACTS_GROUP_NAME));

        final SourceSet sqSourceSet = buildUtil.getSourceSet(BuildUtil.SONARQUBE_SOURCE_SET_NAME);
        tasks.named(JavaPlugin.COMPILE_JAVA_TASK_NAME).configure(t -> t.setGroup(BasePlugin.BUILD_GROUP));
        tasks.named(sqSourceSet.getCompileJavaTaskName()).configure(t -> t.setGroup(BasePlugin.BUILD_GROUP));
        tasks.named(JavaPlugin.COMPILE_TEST_JAVA_TASK_NAME).configure(t -> t.setGroup(BasePlugin.BUILD_GROUP));

        for (final Checkstyle csTask : tasks.withType(Checkstyle.class)) {
            csTask.setGroup(JavaBasePlugin.VERIFICATION_GROUP);
        }
        for (final Copy task : tasks.withType(Copy.class)) {
            if (task.getName().startsWith("process") && task.getName().endsWith("Resources")) {
                task.setGroup(LifecycleBasePlugin.BUILD_GROUP);
            }
        }
    }
}
