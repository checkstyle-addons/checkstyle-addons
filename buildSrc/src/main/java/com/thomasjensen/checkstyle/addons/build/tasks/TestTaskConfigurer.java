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
import javax.annotation.Nonnull;

import org.gradle.api.JavaVersion;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.Directory;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.testing.Test;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension;
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension;

import com.thomasjensen.checkstyle.addons.build.BuildUtil;
import com.thomasjensen.checkstyle.addons.build.ClasspathBuilder;
import com.thomasjensen.checkstyle.addons.build.DependencyConfig;
import com.thomasjensen.checkstyle.addons.build.JavaLevelUtil;
import com.thomasjensen.checkstyle.addons.build.TaskNames;
import com.thomasjensen.checkstyle.addons.build.TestConfigAction;


/**
 * Configure a Test task to execute tests according to a given dependency configuration.
 */
public class TestTaskConfigurer
{
    public static final String CSVERSION_SYSPROP_NAME = "com.thomasjensen.checkstyle.addons.checkstyle.version";

    private final Test testTask;



    public TestTaskConfigurer(@Nonnull final Test pTestTask)
    {
        super();
        testTask = pTestTask;
    }



    public void configureFor(@Nonnull final DependencyConfig pDepConfig, @Nonnull final String pCsVersion)
    {
        final Project project = testTask.getProject();
        final BuildUtil buildUtil = new BuildUtil(project);
        final JavaVersion javaLevel = pDepConfig.getJavaLevel();
        final String baseCsVersion = pDepConfig.getCheckstyleBaseVersion();

        testTask.setGroup(LifecycleBasePlugin.VERIFICATION_GROUP);

        // Only produce JUnit XMLs for the default test task in order to minimize noise.
        testTask.getReports().getJunitXml().getRequired().set(Boolean.FALSE);

        if (baseCsVersion.equals(pCsVersion)) {
            testTask.setDescription("Run the unit tests using dependency configuration '" + pDepConfig.getName()
                + "' (Checkstyle " + baseCsVersion + ", Java level: " + javaLevel + ")");
        }
        else {
            testTask.setDescription("Run the unit tests compiled for Checkstyle " + baseCsVersion
                + " against a Checkstyle " + pCsVersion + " runtime (Java level: " + javaLevel + ")");
        }

        testTask.dependsOn(buildUtil.getTaskProvider(TaskNames.testClasses, Task.class, pDepConfig));

        new TestConfigAction().execute(testTask);

        final TaskProvider<JavaCompile> compileTaskProvider =
            buildUtil.getTaskProvider(TaskNames.compileTestJava, JavaCompile.class, pDepConfig);
        Provider<Directory> destDir = compileTaskProvider.flatMap(AbstractCompile::getDestinationDirectory);
        testTask.setTestClassesDirs(project.files(destDir));

        if (baseCsVersion.equals(pCsVersion)) {
            project.getTasks().getByName(JavaBasePlugin.CHECK_TASK_NAME).dependsOn(testTask);
        }

        final JavaPluginExtension javaExt = project.getExtensions().getByType(JavaPluginExtension.class);
        testTask.getReports().getHtml().getOutputLocation().fileValue(
            new File(javaExt.getTestReportDir().getAsFile().get(), testTask.getName()));

        final JacocoTaskExtension jacoco = (JacocoTaskExtension) testTask.getExtensions().getByName(
            JacocoPluginExtension.TASK_EXTENSION_NAME);
        jacoco.setEnabled(false);

        testTask.setClasspath(new ClasspathBuilder(project).buildTestExecutionClasspath(pDepConfig, pCsVersion));

        final JavaLevelUtil javaLevelUtil = new JavaLevelUtil(project);
        if (javaLevelUtil.isOlderSupportedJava(javaLevel)) {
            testTask.setExecutable(javaLevelUtil.getJvmExecutable(javaLevel));
        }

        // Make the Checkstyle version available to the test cases via a system property.
        testTask.systemProperty(CSVERSION_SYSPROP_NAME, pCsVersion);
    }
}
