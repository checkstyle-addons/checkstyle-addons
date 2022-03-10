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
import java.nio.charset.StandardCharsets;
import javax.annotation.Nonnull;

import org.gradle.api.JavaVersion;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.api.tasks.compile.JavaCompile;

import com.thomasjensen.checkstyle.addons.build.BuildUtil;
import com.thomasjensen.checkstyle.addons.build.ClasspathBuilder;
import com.thomasjensen.checkstyle.addons.build.DependencyConfig;
import com.thomasjensen.checkstyle.addons.build.JavaLevelUtil;
import com.thomasjensen.checkstyle.addons.build.TaskNames;


/**
 * Configure a compilation task to compile the sources with settings from the given dependency configuration.
 */
public class CompileTaskConfigurer
{
    private final JavaCompile compileTask;



    public CompileTaskConfigurer(final JavaCompile pCompileTask)
    {
        super();
        compileTask = pCompileTask;
    }



    public void configureFor(@Nonnull final DependencyConfig pDepConfig, @Nonnull final SourceSet pSourceSetToCompile)
    {
        final boolean isTest = SourceSet.TEST_SOURCE_SET_NAME.equals(pSourceSetToCompile.getName());
        if (compileTask.getLogger().isInfoEnabled()) {
            compileTask.getLogger().info("Configuring task '" + compileTask.getPath() + "' for depConfig '"
                + pDepConfig.getName() + "' and sourceSet '" + pSourceSetToCompile.getName() + "' (isTest = "
                + isTest + ")");
        }
        final Project project = compileTask.getProject();
        final BuildUtil buildUtil = new BuildUtil(project);
        final JavaVersion javaLevel = pDepConfig.getJavaLevel();

        compileTask.setGroup(BasePlugin.BUILD_GROUP);
        compileTask.setDescription("Compile sources from '" + pSourceSetToCompile.getName()
            + "' source set using dependency configuration '" + pDepConfig.getName() + "' (Java level: " + javaLevel
            + ")");

        // Additional Task Input: the dependency configuration file
        compileTask.getInputs().file(pDepConfig.getConfigFile());

        final CompileOptions options = compileTask.getOptions();
        options.setEncoding(StandardCharsets.UTF_8.toString());
        options.setDeprecation(true);  // show deprecation warnings in compiler output

        final JavaLevelUtil javaLevelUtil = new JavaLevelUtil(project);
        if (javaLevelUtil.isOlderSupportedJava(javaLevel)) {
            options.setFork(true);
            options.getForkOptions().setExecutable(javaLevelUtil.getCompilerExecutable(javaLevel));
        }

        final File destDir = calculateDestDirFromSourceSet(pSourceSetToCompile, pDepConfig.getName());
        compileTask.setSource(pSourceSetToCompile.getAllSource());
        compileTask.getDestinationDirectory().set(destDir);
        compileTask.setSourceCompatibility(javaLevel.toString());
        compileTask.setTargetCompatibility(javaLevel.toString());

        FileCollection cp = new ClasspathBuilder(project)
            .buildCompileClasspath(pDepConfig, pSourceSetToCompile.getName());
        compileTask.setClasspath(cp);

        if (isTest) {
            TaskProvider<Task> mainClasses =
                buildUtil.getTaskProvider(TaskNames.mainClasses, Task.class, pDepConfig);
            TaskProvider<Task> sonarqubeClasses =
                buildUtil.getTaskProvider(TaskNames.sonarqubeClasses, Task.class, pDepConfig);
            compileTask.dependsOn(mainClasses, sonarqubeClasses);
        }
    }



    @Nonnull
    private File calculateDestDirFromSourceSet(@Nonnull final SourceSet pSourceSetToCompile,
        @Nonnull final String pDepConfigName)
    {
        final FileCollection sourceSetClassesDirs = pSourceSetToCompile.getOutput().getClassesDirs();
        final File firstSourceSetClassesDir = sourceSetClassesDirs.iterator().next();  // normally the one for 'java'
        final File result = new File(firstSourceSetClassesDir.getParent(),
            firstSourceSetClassesDir.getName() + Character.toUpperCase(pDepConfigName.charAt(0))
                + pDepConfigName.substring(1));
        return result;
    }
}
