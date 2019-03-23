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
import java.nio.charset.StandardCharsets;
import javax.annotation.Nonnull;

import org.gradle.api.JavaVersion;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.api.tasks.compile.JavaCompile;

import com.thomasjensen.checkstyle.addons.build.BuildUtil;
import com.thomasjensen.checkstyle.addons.build.ClasspathBuilder;
import com.thomasjensen.checkstyle.addons.build.DependencyConfig;
import com.thomasjensen.checkstyle.addons.build.JavaLevelUtil;
import com.thomasjensen.checkstyle.addons.build.TaskNames;


/**
 * Compile the sources with the settings specified in the given dependency configuration.
 */
public class CompileTask
    extends JavaCompile
{
    private final BuildUtil buildUtil;



    public CompileTask()
    {
        super();
        final Project project = getProject();
        buildUtil = new BuildUtil(project);
        setGroup(BasePlugin.BUILD_GROUP);
    }



    public void configureFor(@Nonnull final DependencyConfig pDepConfig, @Nonnull final SourceSet pSourceSetToCompile,
        final boolean pIsTest)
    {
        final Project project = getProject();
        final JavaVersion javaLevel = pDepConfig.getJavaLevel();
        setDescription(
            "Compile sources from '" + pSourceSetToCompile.getName() + "' source set using dependency configuration '"
                + pDepConfig.getName() + "' (Java level: " + javaLevel + ")");

        // Additional Task Input: the dependency configuration file
        getInputs().file(pDepConfig.getConfigFile());

        final CompileOptions options = getOptions();
        options.setEncoding(StandardCharsets.UTF_8.toString());
        options.setDeprecation(true);

        // The warning "Supported source version 'RELEASE_6' from annotation processor 'org.antlr.v4.runtime.misc.
        // NullUsageProcessor' less than -source '1.7'" is okay and may be ignored. It goes away when Checkstyle
        // updates to ANTLR 4.5: https://github.com/antlr/antlr4/issues/487 (which they did in version 6.5)

        final JavaLevelUtil javaLevelUtil = new JavaLevelUtil(project);
        if (javaLevelUtil.isOlderSupportedJava(javaLevel)) {
            options.setFork(true);
            options.getForkOptions().setExecutable(javaLevelUtil.getCompilerExecutable(javaLevel));
        }

        final File destDir = calculateDestDirFromSourceSet(pSourceSetToCompile, pDepConfig.getName());

        setSource(pSourceSetToCompile.getJava());
        setDestinationDir(destDir);
        setSourceCompatibility(javaLevel.toString());
        setTargetCompatibility(javaLevel.toString());
        //setDependencyCacheDir(new File(project.getBuildDir(), "dependency-cache"));
        FileCollection cp = null;
        if (pIsTest) {
            cp = new ClasspathBuilder(project).buildClassPath(pDepConfig, null, false, pSourceSetToCompile,
                buildUtil.getSourceSet(SourceSet.MAIN_SOURCE_SET_NAME),
                buildUtil.getSourceSet(BuildUtil.SONARQUBE_SOURCE_SET_NAME));
        }
        else {
            cp = new ClasspathBuilder(project).buildClassPath(pDepConfig, null, false, pSourceSetToCompile);
        }
        setClasspath(cp);

        if (pIsTest) {
            Task mainClassesTask = buildUtil.getTask(TaskNames.mainClasses, pDepConfig);
            Task sqClassesTask = buildUtil.getTask(TaskNames.sonarqubeClasses, pDepConfig);
            dependsOn(mainClassesTask, sqClassesTask);
        }
    }



    @Nonnull
    private File calculateDestDirFromSourceSet(@Nonnull final SourceSet pSourceSetToCompile,
        @Nonnull final String pDepConfigName)
    {
        final FileCollection sourceSetClassesDirs = pSourceSetToCompile.getOutput().getClassesDirs();
        final File firstSourceSetClassesDir = sourceSetClassesDirs.iterator().next();  // normally the one for 'java'
        final File result = new File(firstSourceSetClassesDir.getParent(),
            firstSourceSetClassesDir.getName() + Character.toUpperCase(pDepConfigName.charAt(0)) + pDepConfigName
                .substring(1));
        return result;
    }
}
