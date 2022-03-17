package com.thomasjensen.checkstyle.addons.build.tasks;
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
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.gradle.api.JavaVersion;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.api.tasks.compile.JavaCompile;

import com.thomasjensen.checkstyle.addons.build.ClasspathBuilder;
import com.thomasjensen.checkstyle.addons.build.DependencyConfig;
import com.thomasjensen.checkstyle.addons.build.JavaLevelUtil;
import com.thomasjensen.checkstyle.addons.build.TaskNames;


/**
 * Configure a compilation task to compile the sources with settings from the given dependency configuration.
 */
public class CompileConfigAction
    extends AbstractTaskConfigAction<JavaCompile>
{
    public CompileConfigAction(@Nonnull DependencyConfig pDepConfig, @Nonnull final SourceSet pSourceSetToCompile)
    {
        super(pDepConfig, pSourceSetToCompile);
    }



    @Nonnull
    private SourceSet getSourceSetToCompile()
    {
        if (getExtraParams() != null && getExtraParams()[0] instanceof SourceSet) {
            return (SourceSet) getExtraParams()[0];
        }
        throw new NullPointerException("extraParams[0] in " + CompileConfigAction.class.getSimpleName());
    }



    @Nonnull
    @Override
    protected String getExtraLogInfo(@Nonnull JavaCompile pTask)
    {
        SourceSet sourceSetToCompile = getSourceSetToCompile();
        return " and sourceSet '" + sourceSetToCompile.getName() + "' (isTest = " + isTest(sourceSetToCompile) + ")";
    }



    private boolean isTest(@Nonnull SourceSet pSourceSetToCompile)
    {
        return SourceSet.TEST_SOURCE_SET_NAME.equals(pSourceSetToCompile.getName());
    }



    @Override
    protected void configureTaskFor(@Nonnull JavaCompile pCompileTask, @Nullable DependencyConfig pDepConfig)
    {
        Objects.requireNonNull(pDepConfig, "required dependency config not present");
        final SourceSet sourceSetToCompile = getSourceSetToCompile();

        final JavaVersion javaLevel = pDepConfig.getJavaLevel();
        pCompileTask.setGroup(BasePlugin.BUILD_GROUP);
        pCompileTask.setDescription("Compile sources from '" + sourceSetToCompile.getName()
            + "' source set using dependency configuration '" + pDepConfig.getName() + "' (Java level: " + javaLevel
            + ")");

        // Additional Task Input: the dependency configuration file
        pCompileTask.getInputs().file(pDepConfig.getConfigFile());

        final CompileOptions options = pCompileTask.getOptions();
        options.setEncoding(StandardCharsets.UTF_8.toString());
        options.setDeprecation(true);  // show deprecation warnings in compiler output

        final JavaLevelUtil javaLevelUtil = new JavaLevelUtil(project);
        if (javaLevelUtil.isOlderSupportedJava(javaLevel)) {
            options.setFork(true);
            options.getForkOptions().setExecutable(javaLevelUtil.getCompilerExecutable(javaLevel));
        }

        final File destDir = calculateDestDirFromSourceSet(sourceSetToCompile, pDepConfig.getName());
        pCompileTask.setSource(sourceSetToCompile.getAllJava());
        pCompileTask.getDestinationDirectory().set(destDir);
        pCompileTask.setSourceCompatibility(javaLevel.toString());
        pCompileTask.setTargetCompatibility(javaLevel.toString());

        FileCollection cp = new ClasspathBuilder(project)
            .buildCompileClasspath(pDepConfig, sourceSetToCompile.getName());
        pCompileTask.setClasspath(cp);

        if (isTest(sourceSetToCompile)) {
            TaskProvider<Task> mainCompile =
                buildUtil.getTaskProvider(TaskNames.compileJava, Task.class, pDepConfig);
            TaskProvider<Task> sonarqubeCompile =
                buildUtil.getTaskProvider(TaskNames.compileSonarqubeJava, Task.class, pDepConfig);
            pCompileTask.dependsOn(mainCompile, sonarqubeCompile);
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
