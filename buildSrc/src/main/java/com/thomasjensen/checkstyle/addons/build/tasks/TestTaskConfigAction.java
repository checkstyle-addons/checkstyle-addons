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
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.gradle.api.JavaVersion;
import org.gradle.api.Task;
import org.gradle.api.file.Directory;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskContainer;
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


/**
 * Configure a Test task to execute tests according to a given dependency configuration.
 */
public class TestTaskConfigAction
    extends AbstractTaskConfigAction<Test>
{
    public static final String CSVERSION_SYSPROP_NAME = "com.thomasjensen.checkstyle.addons.checkstyle.version";



    public TestTaskConfigAction(@Nonnull DependencyConfig pDepConfig, @Nonnull final String pCsVersion)
    {
        super(pDepConfig, pCsVersion);
    }



    @Nonnull
    @Override
    protected String getExtraLogInfo(@Nonnull Test pTask)
    {
        return " and Checkstyle version " + getCsVersion();
    }



    @Nonnull
    private String getCsVersion()
    {
        if (getExtraParams() != null && getExtraParams()[0] instanceof String) {
            return (String) getExtraParams()[0];
        }
        throw new NullPointerException("extraParams[0] in " + TestTaskConfigAction.class.getSimpleName());
    }



    @Override
    protected void configureTaskFor(@Nonnull Test pTestTask, @Nullable DependencyConfig pDepConfig)
    {
        Objects.requireNonNull(pDepConfig, "required dependency config not present");
        final String csVersion = getCsVersion();
        final String baseCsVersion = pDepConfig.getCheckstyleBaseVersion();
        final TaskContainer tasks = project.getTasks();
        final JavaVersion javaLevel = pDepConfig.getJavaLevel();
        pTestTask.setGroup(LifecycleBasePlugin.VERIFICATION_GROUP);

        // Only produce JUnit XMLs for the default test task in order to minimize noise.
        pTestTask.getReports().getJunitXml().getRequired().set(Boolean.FALSE);

        if (baseCsVersion.equals(csVersion)) {
            pTestTask.setDescription("Run the unit tests using dependency configuration '" + pDepConfig.getName()
                + "' (Checkstyle " + baseCsVersion + ", Java level: " + javaLevel + ")");
        }
        else {
            pTestTask.setDescription("Run the unit tests compiled for Checkstyle " + baseCsVersion
                + " against a Checkstyle " + csVersion + " runtime (Java level: " + javaLevel + ")");
        }

        final SourceSet testSourceSet = buildUtil.getSourceSet(SourceSet.TEST_SOURCE_SET_NAME);
        final SourceSet mainSourceSet = buildUtil.getSourceSet(SourceSet.MAIN_SOURCE_SET_NAME);
        final SourceSet sqSourceSet = buildUtil.getSourceSet(BuildUtil.SONARQUBE_SOURCE_SET_NAME);
        pTestTask.dependsOn(
            buildUtil.getTaskProvider(TaskNames.compileTestJava, Task.class, pDepConfig),
            tasks.named(testSourceSet.getProcessResourcesTaskName()),
            buildUtil.getTaskProvider(TaskNames.compileJava, Task.class, pDepConfig),
            tasks.named(mainSourceSet.getProcessResourcesTaskName()),
            buildUtil.getTaskProvider(TaskNames.compileSonarqubeJava, Task.class, pDepConfig),
            tasks.named(sqSourceSet.getProcessResourcesTaskName())
        );

        new com.thomasjensen.checkstyle.addons.build.TestConfigAction().execute(pTestTask);

        final TaskProvider<JavaCompile> compileTaskProvider =
            buildUtil.getTaskProvider(TaskNames.compileTestJava, JavaCompile.class, pDepConfig);
        Provider<Directory> destDir = compileTaskProvider.flatMap(AbstractCompile::getDestinationDirectory);
        pTestTask.setTestClassesDirs(project.files(destDir));

        final JavaPluginExtension javaExt = project.getExtensions().getByType(JavaPluginExtension.class);
        pTestTask.getReports().getHtml().getOutputLocation().fileValue(
            new File(javaExt.getTestReportDir().getAsFile().get(), pTestTask.getName()));

        final JacocoTaskExtension jacoco = (JacocoTaskExtension) pTestTask.getExtensions().getByName(
            JacocoPluginExtension.TASK_EXTENSION_NAME);
        jacoco.setEnabled(false);

        pTestTask.setClasspath(new ClasspathBuilder(project).buildTestExecutionClasspath(pDepConfig, csVersion));

        final JavaLevelUtil javaLevelUtil = new JavaLevelUtil(project);
        if (javaLevelUtil.isOlderSupportedJava(javaLevel)) {
            pTestTask.setExecutable(javaLevelUtil.getJvmExecutable(javaLevel));
        }

        // Make the Checkstyle version available to the test cases via a system property.
        pTestTask.systemProperty(CSVERSION_SYSPROP_NAME, csVersion);
    }
}
