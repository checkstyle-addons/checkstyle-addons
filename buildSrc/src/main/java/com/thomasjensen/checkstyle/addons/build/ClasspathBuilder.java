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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.file.FileCollection;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.api.tasks.compile.JavaCompile;


/**
 * Constructs classpaths in the form of a {@link FileCollection} for one Gradle configuration and one dependency
 * configuration. The Checkstyle version used can sometimes be individually overridden. Any references to JAR
 * dependencies may also be altered if the given dependency configuration requires it.
 */
public class ClasspathBuilder
{
    private final Project project;

    private final BuildUtil buildUtil;



    /**
     * Constructor.
     *
     * @param pProject the Gradle project
     */
    public ClasspathBuilder(@Nonnull final Project pProject)
    {
        project = pProject;
        buildUtil = new BuildUtil(pProject);
    }



    public FileCollection buildCompileClasspath(@Nonnull final DependencyConfig pDepConfig,
        @Nonnull final String pNameOfSourceSetToCompile)
    {
        final SourceSet sourceSetToCompile = buildUtil.getSourceSet(pNameOfSourceSetToCompile);
        final boolean isTestCompile = SourceSet.TEST_SOURCE_SET_NAME.equals(pNameOfSourceSetToCompile);
        final SourceSet mainSourceSet = buildUtil.getSourceSet(SourceSet.MAIN_SOURCE_SET_NAME);
        final SourceSet sqSourceSet = buildUtil.getSourceSet(BuildUtil.SONARQUBE_SOURCE_SET_NAME);

        FileCollection cp = project.getObjects().fileCollection();
        if (isTestCompile) {
            cp = cp
                .plus(getClassesDirs(mainSourceSet, pDepConfig))
                .plus(getClassesDirs(sqSourceSet, pDepConfig));
        }

        cp = cp
            .plus(calculateDependencies(pDepConfig, null, sourceSetToCompile.getCompileClasspathConfigurationName()));
        if (isTestCompile) {
            cp = cp
                .plus(calculateDependencies(pDepConfig, null, mainSourceSet.getCompileClasspathConfigurationName()))
                .plus(calculateDependencies(pDepConfig, null, sqSourceSet.getCompileClasspathConfigurationName()));
        }

        logClasspathInfo("Compile(" + pNameOfSourceSetToCompile + ")", pDepConfig, null, cp);
        return cp;
    }



    public FileCollection buildJavadocClasspath(@Nonnull final DependencyConfig pDepConfig)
    {
        final SourceSet mainSourceSet = buildUtil.getSourceSet(SourceSet.MAIN_SOURCE_SET_NAME);
        final SourceSet sqSourceSet = buildUtil.getSourceSet(BuildUtil.SONARQUBE_SOURCE_SET_NAME);

        FileCollection cp = project.getObjects().fileCollection();
        cp = cp
            .plus(getClassesDirs(mainSourceSet, pDepConfig))
            .plus(getClassesDirs(sqSourceSet, pDepConfig))
            .plus(calculateDependencies(pDepConfig, null, mainSourceSet.getCompileClasspathConfigurationName()))
            .plus(calculateDependencies(pDepConfig, null, sqSourceSet.getCompileClasspathConfigurationName()));

        logClasspathInfo("Javadoc", pDepConfig, null, cp);
        return cp;
    }



    /**
     * Run the classpath builder to produce a runtime classpath for test execution.
     * <p><b>This will resolve configurations in order to get at concrete file paths.</b></p>
     *
     * @param pDepConfig the dependency configuration
     * @param pCsVersion if a Checkstyle runtime should be used which is different from the base version
     *     given as part of the dependency configuration
     * @return the classpath
     */
    public FileCollection buildTestExecutionClasspath(@Nonnull final DependencyConfig pDepConfig,
        @Nonnull final String pCsVersion)
    {
        final SourceSet testSourceSet = buildUtil.getSourceSet(SourceSet.TEST_SOURCE_SET_NAME);
        final SourceSet mainSourceSet = buildUtil.getSourceSet(SourceSet.MAIN_SOURCE_SET_NAME);
        final SourceSet sqSourceSet = buildUtil.getSourceSet(BuildUtil.SONARQUBE_SOURCE_SET_NAME);

        FileCollection cp = project.getObjects().fileCollection();
        cp = cp
            .plus(sourceSetDirs(pDepConfig, testSourceSet))
            .plus(sourceSetDirs(pDepConfig, mainSourceSet))
            .plus(sourceSetDirs(pDepConfig, sqSourceSet))
            .plus(calculateDependencies(pDepConfig, pCsVersion, testSourceSet.getRuntimeClasspathConfigurationName()))
            .plus(calculateDependencies(pDepConfig, pCsVersion, mainSourceSet.getRuntimeClasspathConfigurationName()))
            .plus(calculateDependencies(pDepConfig, pCsVersion, sqSourceSet.getRuntimeClasspathConfigurationName()));

        logClasspathInfo("Test runtime", pDepConfig, pCsVersion, cp);
        return cp;
    }



    private FileCollection sourceSetDirs(@Nonnull final DependencyConfig pDepConfig,
        @Nonnull final SourceSet pSourceSet)
    {
        FileCollection result = project.getObjects().fileCollection();
        result = result
            .plus(getClassesDirs(pSourceSet, pDepConfig))
            .plus(project.files(pSourceSet.getOutput().getResourcesDir()));
        return result;
    }



    /**
     * Determine the directory where the dependency configuration specific compile tasks store their compiled classes.
     * This can be just a single directory (the destination directory of the Java compile task), or a file collection
     * (the classes directories of the source set output).
     *
     * @param pSourceSet the source set
     * @param pDepConfig the dependency configuration whose classes dir we are interested in
     * @return the classes dir(s)
     */
    public FileCollection getClassesDirs(@Nonnull final SourceSet pSourceSet,
        @Nonnull final DependencyConfig pDepConfig)
    {
        FileCollection result = null;
        if (pDepConfig.isDefaultConfig()) {
            result = pSourceSet.getOutput().getClassesDirs();
        }
        else {
            TaskProvider<JavaCompile> compileTaskProvider = null;
            if (SourceSet.MAIN_SOURCE_SET_NAME.equals(pSourceSet.getName())) {
                compileTaskProvider = buildUtil.getTaskProvider(TaskNames.compileJava,
                    JavaCompile.class, pDepConfig);
            }
            else if (BuildUtil.SONARQUBE_SOURCE_SET_NAME.equals(pSourceSet.getName())) {
                compileTaskProvider = buildUtil.getTaskProvider(TaskNames.compileSonarqubeJava,
                    JavaCompile.class, pDepConfig);
            }
            else if (SourceSet.TEST_SOURCE_SET_NAME.equals(pSourceSet.getName())) {
                compileTaskProvider = buildUtil.getTaskProvider(TaskNames.compileTestJava,
                    JavaCompile.class, pDepConfig);
            }
            else {
                throw new GradleException("unknown source set: " + pSourceSet.getName());
            }
            result = project.files(compileTaskProvider.flatMap(AbstractCompile::getDestinationDirectory));
        }
        return result;
    }



    private FileCollection calculateDependencies(@Nonnull final DependencyConfig pDepConfig,
        @Nullable final String pCsVersionOverride, @Nonnull final String pClasspathConfigurationName)
    {
        Configuration cfg = buildDetachedConfiguration(pDepConfig, pCsVersionOverride, pClasspathConfigurationName);
        return project.files(cfg.resolve());
    }



    private Configuration buildDetachedConfiguration(@Nonnull final DependencyConfig pDepConfig,
        @Nullable final String pCsVersionOverride, @Nonnull final String pClasspathConfigurationName)
    {
        final Configuration compileConfig = project.getConfigurations().getByName(pClasspathConfigurationName);
        final Map<String, String> versionOverrides = pDepConfig.getArtifactVersions();
        final List<Dependency> newDeps = new ArrayList<>();
        for (final Dependency dependency : compileConfig.getAllDependencies()) {
            if (DependencyConfig.CHECKSTYLE_GROUPID.equals(dependency.getGroup()) && pCsVersionOverride != null) {
                final ModuleDependency newDep = (ModuleDependency) project.getDependencies().create(
                    dependency.getGroup() + ":" + dependency.getName() + ":" + pCsVersionOverride);
                newDeps.add(newDep);
            }
            else if (versionOverrides.containsKey(dependency.getGroup())) {
                final ModuleDependency newDep = (ModuleDependency) project.getDependencies().create(
                    dependency.getGroup() + ":" + dependency.getName() + ":" + versionOverrides
                        .get(dependency.getGroup()));
                newDeps.add(newDep);
            }
            else if (!pDepConfig.getJavaLevel().isJava8Compatible()
                && "com.github.spotbugs".equals(dependency.getGroup())) {
                // com.github.spotbugs requires minimum JDK8, so for older depConfigs, we must replace with FindBugs.
                // This is ok, because the annotation is only *used* by SpotBugs in the default depConfig.
                final ModuleDependency newDep = (ModuleDependency) project.getDependencies().create(
                    "com.google.code.findbugs:annotations:3.0.1");
                newDeps.add(newDep);
            }
            else {
                newDeps.add(dependency);
            }
        }
        return project.getConfigurations().detachedConfiguration(newDeps.toArray(new Dependency[0]));
    }



    /**
     * Return a detached configuration containing the runtime dependencies (without our own code). <b>This will resolve
     * the configuration to get at concrete file paths.</b>
     *
     * @param pDepConfig a dependency configuration
     * @return runtimeClasspath configuration of the main source set
     */
    public Configuration buildMainRuntimeConfiguration(@Nonnull final DependencyConfig pDepConfig)
    {
        final SourceSet mainSourceSet = buildUtil.getSourceSet(SourceSet.MAIN_SOURCE_SET_NAME);
        final String runtimeCpConfigName = mainSourceSet.getRuntimeClasspathConfigurationName();
        Configuration result = project.getConfigurations().getByName(runtimeCpConfigName);
        if (!pDepConfig.isDefaultConfig()) {
            result = buildDetachedConfiguration(pDepConfig, null, runtimeCpConfigName);
        }
        return result;
    }



    private void logClasspathInfo(@Nonnull final String pWhat, @Nonnull final DependencyConfig pDepConfig,
        @Nullable final String pCsVersionOverride, @Nonnull final FileCollection pClasspath)
    {
        final Logger logger = project.getLogger();
        logger.info("-----------------------------------------------------------------------------------------");
        logger.info(pWhat + " classpath of dependency configuration '" + pDepConfig.getName() + "', Checkstyle version "
            + (pCsVersionOverride != null ? pCsVersionOverride : pDepConfig.getCheckstyleBaseVersion()));
        for (File f : pClasspath) {
            logger.info("  - " + f.getAbsolutePath());
        }
        logger.info("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
    }
}
