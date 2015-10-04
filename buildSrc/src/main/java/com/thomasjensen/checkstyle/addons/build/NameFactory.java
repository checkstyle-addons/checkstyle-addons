package com.thomasjensen.checkstyle.addons.build;
/*
 * Checkstyle-Addons - Additional Checkstyle checks
 * Copyright (C) 2015 Thomas Jensen
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

import java.util.regex.Pattern;
import javax.annotation.Nonnull;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.UnknownConfigurationException;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;


/**
 * Factory for dynamically creating the names of tasks, configurations, and source sets used by the build process.
 *
 * @author Thomas Jensen
 */
public class NameFactory
{
    private static final Pattern DOT_PATTERN = Pattern.compile(Pattern.quote("."));

    private final Project project;

    private final String defaultCheckstyleVersion;



    /**
     * Constructor.
     *
     * @param pProject the project
     * @param pDefaultCheckstyleVersion the configured default checkstyle version
     */
    public NameFactory(@Nonnull final Project pProject, @Nonnull final String pDefaultCheckstyleVersion)
    {
        project = pProject;
        defaultCheckstyleVersion = pDefaultCheckstyleVersion;
    }



    private String gradlify(final String pCheckstyleVersion)
    {
        return DOT_PATTERN.matcher(pCheckstyleVersion).replaceAll("_");
    }



    /**
     * Generate the version-specific name of the given object via the object's name rule.
     *
     * @param pNameEnum the name enum to use
     * @param pCheckstyleVersion the Checkstyle version, e.g. <code>"6.10.1"</code>
     * @return the version-specific name
     */
    @Nonnull
    public String getName(@Nonnull final NameWithVersion pNameEnum, @Nonnull final String pCheckstyleVersion)
    {
        if (pCheckstyleVersion.equals(defaultCheckstyleVersion) && !pNameEnum.useVersionForDefault()) {
            return pNameEnum.getNameWithoutVersion();
        }
        else {
            return pNameEnum.getNameWithVersion(gradlify(pCheckstyleVersion));
        }
    }



    /**
     * Specialized variant of {@link #getName} used for <code>xtest</code> task names.
     * @param pVersionToCheck the Checkstyle version the task shall run
     * @param pVersionAgainst the runtime Checkstyle version against which the task shall run
     * @return the task name
     */
    @Nonnull
    public String getNameXCheck(@Nonnull final String pVersionToCheck, @Nonnull final String pVersionAgainst)
    {
        return TaskNames.xtest.getNameWithVersion(gradlify(pVersionToCheck), gradlify(pVersionAgainst));
    }



    /**
     * Get the version-specific configuration from the project's list of configurations.
     *
     * @param pConfigName Enum instance indicating which configuration to return
     * @param pCheckstyleVersion the Checkstyle version for which the configuration is sought, e.g.
     * <code>"6.10.1"</code>
     * @return a Configuration object
     *
     * @throws UnknownConfigurationException the given configuration name with version resolved to an unknown name
     */
    @Nonnull
    public Configuration getConfiguration(@Nonnull final ConfigNames pConfigName,
        @Nonnull final String pCheckstyleVersion)
    {
        return project.getConfigurations().getByName(getName(pConfigName, pCheckstyleVersion));
    }



    /**
     * Convenience method for getting a specific source set directly.
     * @param pSourceSetName the source set to get
     * @param pCheckstyleVersion the Checkstyle version for which the source set is configured, e.g.
     * <code>"6.10.1"</code>
     * @return a source set object
     */
    @Nonnull
    public SourceSet getSourceSet(@Nonnull final SourceSetNames pSourceSetName,
        @Nonnull final String pCheckstyleVersion)
    {
        SourceSetContainer sourceSets = (SourceSetContainer) project.getProperties().get("sourceSets");
        return sourceSets.getByName(getName(pSourceSetName, pCheckstyleVersion));
    }



    /**
     * Convenience method for getting a specific task directly.
     * @param pTaskName the task to get
     * @param pCheckstyleVersion the Checkstyle version for which the task is configured, e.g. <code>"6.10.1"</code>
     * @return a task object
     */
    @Nonnull
    public Task getTask(@Nonnull final TaskNames pTaskName, @Nonnull final String pCheckstyleVersion)
    {
        return project.getTasks().getByName(getName(pTaskName, pCheckstyleVersion));
    }
}
