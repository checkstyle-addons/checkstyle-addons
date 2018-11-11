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

import java.io.Closeable;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import groovy.lang.Closure;
import org.ajoberstar.grgit.Grgit;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.java.archives.Attributes;
import org.gradle.api.plugins.ExtraPropertiesExtension;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.bundling.Jar;


/**
 * Utility class for use by the build.
 */
public final class BuildUtil
{
    /** the Maven metadata name {@code "artifactId"} */
    public static final String ARTIFACT_ID = "artifactId";

    /** the Maven metadata name {@code "groupId"} */
    public static final String GROUP_ID = "groupId";

    /** the Maven metadata name {@code "version"} */
    public static final String VERSION = "version";

    /** the name of our 'sonarqube' source set */
    public static final String SONARQUBE_SOURCE_SET_NAME = "sonarqube";

    private final Project project;

    private final SourceSetContainer sourceSets;



    /**
     * Constructor.
     *
     * @param pProject the Gradle project
     */
    public BuildUtil(@Nonnull final Project pProject)
    {
        super();
        project = pProject;
        final JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
        sourceSets = javaConvention.getSourceSets();
    }



    /**
     * Close the closeable while ignoring any {@code IOException}s this may throw.
     *
     * @param pCloseable the closeable to close
     */
    public void closeQuietly(@Nullable final Closeable pCloseable)
    {
        if (pCloseable != null) {
            try {
                pCloseable.close();
            }
            catch (IOException e) {
                // ignore
            }
        }
    }



    /**
     * Retrieve the dep configs from the project's extra properties.
     *
     * @return the dependency configuration container
     */
    public DependencyConfigs getDepConfigs()
    {
        return getExtraPropertyValue(ExtProp.DepConfigs);
    }



    /**
     * Retrieve the longName from the project's extra properties.
     *
     * @return the long name of this software
     */
    public String getLongName()
    {
        return getExtraPropertyValue(ExtProp.LongName);
    }



    /**
     * Read the value of an extra property of the project.
     *
     * @param pExtraPropName the reference to the extra property name
     * @param <T> type of the property value
     * @return the property's value
     */
    @SuppressWarnings("unchecked")
    public <T> T getExtraPropertyValue(@Nonnull final ExtProp pExtraPropName)
    {
        ExtraPropertiesExtension extraProps = project.getExtensions().getByType(ExtraPropertiesExtension.class);
        if (extraProps.has(pExtraPropName.getPropertyName())) {
            return (T) extraProps.get(pExtraPropName.getPropertyName());
        }
        throw new GradleException(
            "Reference to non-existent project extra property '" + pExtraPropName.getPropertyName() + "'");
    }



    /**
     * Convenience method for getting a specific task directly.
     *
     * @param pTaskName the task to get
     * @param pDepConfig the dependency configuration for which the task is intended
     * @return a task object
     */
    @Nonnull
    public Task getTask(@Nonnull final TaskNames pTaskName, @Nonnull final DependencyConfig pDepConfig)
    {
        return project.getTasks().getByName(pTaskName.getName(pDepConfig));
    }



    /**
     * Get a source set by name.
     *
     * @param pName the source set name
     * @return the source set
     */
    @Nonnull
    public SourceSet getSourceSet(@Nonnull final String pName)
    {
        return sourceSets.getByName(pName);
    }



    private void addBuildTimestamp(@Nonnull final Attributes pAttributes)
    {
        pAttributes.put("Build-Timestamp", getExtraPropertyValue(ExtProp.BuildTimestamp).toString());
    }



    /**
     * Add build timestamp to some manifest attributes in the execution phase, so that it does not count for the
     * up-to-date check.
     *
     * @param pTask the executing task
     * @param pAttributes the attributes map to add to
     */
    public void addBuildTimestampDeferred(@Nonnull final Task pTask, @Nonnull final Attributes pAttributes)
    {
        pTask.doFirst(new Closure<Void>(pTask)
        {
            @Override
            @SuppressWarnings("MethodDoesntCallSuperMethod")
            public Void call()
            {
                addBuildTimestamp(pAttributes);
                return null;
            }
        });
    }



    /**
     * Make the given Jar task inherit its manifest from the "main" "thin" Jar task. Also set the build timestamp.
     *
     * @param pTask the executing task
     * @param pDepConfig the dependency configuration for which the Jar task is intended
     */
    public void inheritManifest(@Nonnull final Jar pTask, @Nonnull final DependencyConfig pDepConfig)
    {
        pTask.doFirst(new Closure<Void>(pTask)
        {
            @Override
            @SuppressWarnings("MethodDoesntCallSuperMethod")
            public Void call()
            {
                final Jar jarTask = (Jar) getTask(TaskNames.jar, pDepConfig);
                pTask.setManifest(jarTask.getManifest());
                addBuildTimestamp(pTask.getManifest().getAttributes());
                return null;
            }
        });
    }



    /**
     * Determine the git commit hash of the most recent commit in this repo.
     *
     * @return the hash
     */
    public String currentGitCommitHash()
    {
        try (Grgit gitRepo = Grgit.open()) {
            return gitRepo.head().getId();
        }
    }
}
