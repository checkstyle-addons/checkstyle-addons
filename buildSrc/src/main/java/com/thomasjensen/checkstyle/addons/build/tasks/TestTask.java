package com.thomasjensen.checkstyle.addons.build.tasks;
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
import javax.annotation.Nonnull;

import groovy.lang.Closure;
import org.gradle.api.JavaVersion;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.testing.Test;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension;
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension;

import com.thomasjensen.checkstyle.addons.build.BuildUtil;
import com.thomasjensen.checkstyle.addons.build.ClasspathBuilder;
import com.thomasjensen.checkstyle.addons.build.DependencyConfig;
import com.thomasjensen.checkstyle.addons.build.ExtProp;
import com.thomasjensen.checkstyle.addons.build.JavaLevelUtil;
import com.thomasjensen.checkstyle.addons.build.TaskNames;


/**
 * Execute the tests according to a given dependency configuration.
 */
public class TestTask
    extends Test
{
    public static final String CSVERSION_SYSPROP_NAME = "com.thomasjensen.checkstyle.addons.checkstyle.version";

    private final BuildUtil buildUtil;



    public TestTask()
    {
        super();
        final Project project = getProject();
        buildUtil = new BuildUtil(project);
        setGroup(LifecycleBasePlugin.VERIFICATION_GROUP);
        getReports().getJunitXml().setEnabled(false);
    }



    public void configureFor(@Nonnull final DependencyConfig pDepConfig, @Nonnull final String pCsVersion)
    {
        final Project project = getProject();
        final JavaVersion javaLevel = pDepConfig.getJavaLevel();
        final String baseCsVersion = pDepConfig.getCheckstyleBaseVersion();

        if (baseCsVersion.equals(pCsVersion)) {
            setDescription(
                "Run the unit tests using dependency configuration '" + pDepConfig.getName() + "' (Checkstyle "
                    + baseCsVersion + ", Java level: " + javaLevel + ")");
        }
        else {
            setDescription(
                "Run the unit tests compiled for Checkstyle " + baseCsVersion + " against a Checkstyle " + pCsVersion
                    + " runtime (Java level: " + javaLevel + ")");
        }

        dependsOn(TaskNames.testClasses.getName(pDepConfig));

        configure(buildUtil.getExtraPropertyValue(ExtProp.TestConfigClosure));
        setTestClassesDirs(project.files(((JavaCompile) buildUtil.getTask(TaskNames.compileTestJava, pDepConfig))
            .getDestinationDir()));

        if (baseCsVersion.equals(pCsVersion)) {
            project.getTasks().getByName(JavaBasePlugin.CHECK_TASK_NAME).dependsOn(this);
        }

        final JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
        getReports().getHtml().setDestination(new File(javaConvention.getTestReportDir(), getName()));

        final JacocoTaskExtension jacoco = (JacocoTaskExtension) getExtensions().getByName(
            JacocoPluginExtension.TASK_EXTENSION_NAME);
        jacoco.setEnabled(false);

        setClasspath(new ClasspathBuilder(project)
            .buildClassPath(pDepConfig, pCsVersion, true, buildUtil.getSourceSet(SourceSet.TEST_SOURCE_SET_NAME),
                buildUtil.getSourceSet(SourceSet.MAIN_SOURCE_SET_NAME),
                buildUtil.getSourceSet(BuildUtil.SONARQUBE_SOURCE_SET_NAME)));

        final JavaLevelUtil javaLevelUtil = new JavaLevelUtil(project);
        if (javaLevelUtil.isOlderSupportedJava(javaLevel)) {
            setExecutable(javaLevelUtil.getJvmExecutable(javaLevel));
        }

        // Make the Checkstyle version available to the test cases via a system property.
        configure(new Closure<Void>(this)
        {
            @Override
            public Void call()
            {
                systemProperty(CSVERSION_SYSPROP_NAME, pCsVersion);
                return null;
            }
        });
    }
}
