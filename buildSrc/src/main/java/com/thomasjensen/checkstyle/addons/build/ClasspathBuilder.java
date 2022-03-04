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
import java.util.Set;
import java.util.TreeSet;
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



    /**
     * Determine the directory where the dependency configuration specific compile tasks store their compiled classes.
     * This can be just a single directory (the destination directory of the Java compile task), or a file collection
     * (the classes directories of the source set output).
     *
     * @param pSourceSet the source set
     * @param pDepConfig the dependency configuration whose classes dir we are interested in
     * @return the classes dir
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
            result = project.files(compileTaskProvider.flatMap(compileTask -> compileTask.getDestinationDirectory()));
        }
        return result;
    }



    private String getConfigName(@Nonnull final SourceSet pSourceSet, final boolean pIsTestRun)
    {
        String result = pSourceSet.getCompileClasspathConfigurationName();
        if (pIsTestRun) {
            result = pSourceSet.getRuntimeClasspathConfigurationName();
        }
        return result;
    }



    /**
     * Run the classpath builder to produce a classpath for compilation, running the Javadoc generation, or running
     * unit tests. <b>This will resolve configurations in order to get at concrete file paths.</b>
     *
     * @param pDepConfig the dependency configuration
     * @param pCsVersionOverride if a Checkstyle runtime should be used which is different from the base version
     *     given as part of the dependency configuration
     * @param pIsTestRun if the resulting classpath if to be used to <em>execute</em> tests (rather than compile
     *     them)
     * @param pSourceSet1 source set to include first in the constructed classpath
     * @param pOtherSourceSets more source sets to include
     * @return the classpath
     */
    public FileCollection buildClassPath(@Nonnull final DependencyConfig pDepConfig,
        @Nullable final String pCsVersionOverride, final boolean pIsTestRun, @Nonnull final SourceSet pSourceSet1,
        @Nullable final SourceSet... pOtherSourceSets)
    {
        FileCollection cp = getClassesDirs(pSourceSet1, pDepConfig).plus(
            project.files(pSourceSet1.getOutput().getResourcesDir()));
        if (pOtherSourceSets != null && pOtherSourceSets.length > 0) {
            for (final SourceSet sourceSet : pOtherSourceSets) {
                cp = cp.plus(getClassesDirs(sourceSet, pDepConfig)).plus(
                    project.files(sourceSet.getOutput().getResourcesDir()));
            }
        }

        cp = cp.plus(project.files(
            calculateDependencies(pDepConfig, pCsVersionOverride, getConfigName(pSourceSet1, pIsTestRun))));
        if (pOtherSourceSets != null && pOtherSourceSets.length > 0) {
            for (final SourceSet sourceSet : pOtherSourceSets) {
                cp = cp.plus(project.files(
                    calculateDependencies(pDepConfig, pCsVersionOverride, getConfigName(sourceSet, pIsTestRun))));
            }
        }

        logClasspathInfo(pDepConfig, pCsVersionOverride, pIsTestRun, pSourceSet1, pOtherSourceSets, cp);
        return cp;
    }



    private void logClasspathInfo(@Nonnull final DependencyConfig pDepConfig,
        @Nullable final String pCsVersionOverride, final boolean pIsTestRun, @Nonnull final SourceSet pSourceSet1,
        @Nullable final SourceSet[] pOtherSourceSets, @Nonnull final FileCollection pClasspath)
    {
        final Logger logger = project.getLogger();
        logger.info("-----------------------------------------------------------------------------------------");
        logger.info("Classpath of dependency configuration '" + pDepConfig.getName() + "'");
        logger.info("Checkstyle version: "
            + (pCsVersionOverride != null ? pCsVersionOverride : pDepConfig.getCheckstyleBaseVersion()));
        logger.info("isTestRun = " + pIsTestRun);
        Set<String> sourceSetNames = new TreeSet<>();
        sourceSetNames.add(pSourceSet1.getName());
        if (pOtherSourceSets != null && pOtherSourceSets.length > 0) {
            for (final SourceSet sourceSet : pOtherSourceSets) {
                sourceSetNames.add(sourceSet.getName());
            }
        }
        logger.info("SourceSets = " + sourceSetNames);
        logger.info("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
        for (File f : pClasspath) {
            logger.info("  - " + f.getAbsolutePath());
        }
        logger.info("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
    }



    private Set<File> calculateDependencies(@Nonnull final DependencyConfig pDepConfig,
        @Nullable final String pCsVersionOverride, @Nonnull final String pClasspathConfigurationName)
    {
        Configuration cfg = buildDetachedConfiguration(pDepConfig, pCsVersionOverride, pClasspathConfigurationName);
        return cfg.resolve();
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
        String runtimeCpConfigName = buildUtil.getSourceSet(SourceSet.MAIN_SOURCE_SET_NAME)
            .getRuntimeClasspathConfigurationName();
        Configuration result = project.getConfigurations().getByName(runtimeCpConfigName);
        if (!pDepConfig.isDefaultConfig()) {
            result = buildDetachedConfiguration(pDepConfig, null, runtimeCpConfigName);
        }
        return result;
    }
}
