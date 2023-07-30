package com.thomasjensen.checkstyle.addons.build;
/*
 * Checkstyle-Addons - Additional Checkstyle checks
 * Copyright (c) 2015-2023, the Checkstyle Addons contributors
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

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.java.archives.Attributes;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
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



    public BuildUtil(@Nonnull final Project pProject)
    {
        super();
        project = pProject;
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
     * Convenience method for getting a specific task directly. <b>This will realize the referenced task. Therefore,
     * prefer {@link #getTaskProvider} over this method.</b>
     *
     * @param pTaskName the task to get
     * @param pTaskType the type of the task to get
     * @param pDepConfig the dependency configuration for which the task is intended
     * @return a task
     */
    @Nonnull
    public <T extends Task> T getTask(@Nonnull final TaskNames pTaskName, @Nonnull final Class<T> pTaskType,
        @Nonnull final DependencyConfig pDepConfig)
    {
        return pTaskType.cast(project.getTasks().getByName(pTaskName.getName(pDepConfig)));
    }



    /**
     * Convenience method for getting a specific task provider directly.
     *
     * @param pTaskName the name of the task whose provider to get
     * @param pTaskType the type of the task whose provider to get
     * @param pDepConfig the dependency configuration for which the task is intended
     * @return a task provider
     */
    @Nonnull
    public <T extends Task> TaskProvider<T> getTaskProvider(@Nonnull final TaskNames pTaskName,
        @Nonnull final Class<T> pTaskType, @Nonnull final DependencyConfig pDepConfig)
    {
        return project.getTasks().named(pTaskName.getName(pDepConfig), pTaskType);
    }



    @Nonnull
    public SourceSetContainer getSourceSets()
    {
        final JavaPluginExtension javaExt = project.getExtensions().getByType(JavaPluginExtension.class);
        return javaExt.getSourceSets();
    }



    @Nonnull
    public SourceSet getSourceSet(@Nonnull final String pName)
    {
        return getSourceSets().getByName(pName);
    }



    private void addBuildTimestamp(@Nonnull final Attributes pAttributes)
    {
        pAttributes.put("Build-Timestamp", getBuildConfig().getBuildTimestamp().get().toString());
    }



    /**
     * Add build timestamp to manifest attributes in a doFirst() action, so that it does not count for the
     * up-to-date check.
     *
     * @param pJarTask the task to whose manifest to add the build timestamp
     */
    @SuppressWarnings("Convert2Lambda")  // MUST NOT USE LAMBDA, as this would cause Gradle errors
    public void addBuildTimestampDeferred(@Nonnull final Jar pJarTask)
    {
        pJarTask.doFirst(new Action<>() {
            @Override
            public void execute(@Nonnull final Task pJarTask)
            {
                // https://docs.gradle.org/7.4/userguide/validation_problems.html#implementation_unknown
                addBuildTimestamp(((Jar) pJarTask).getManifest().getAttributes());
            }
        });
    }



    /**
     * Make the given Jar task inherit its manifest from the "main" "thin" Jar task. Also set the build timestamp.
     *
     * @param pJarTask the executing task
     * @param pDepConfig the dependency configuration for which the Jar task is intended
     */
    public void inheritManifest(@Nonnull final Jar pJarTask, @Nonnull final DependencyConfig pDepConfig)
    {
        pJarTask.getManifest().from(getTask(TaskNames.jar, Jar.class, pDepConfig).getManifest());
    }



    @Nonnull
    public BuildConfigExtension getBuildConfig()
    {
        return project.getExtensions().getByType(BuildConfigExtension.class);
    }
}
