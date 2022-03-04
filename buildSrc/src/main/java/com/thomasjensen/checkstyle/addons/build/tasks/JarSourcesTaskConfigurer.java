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

import javax.annotation.Nonnull;

import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.bundling.Jar;

import com.thomasjensen.checkstyle.addons.build.BuildUtil;
import com.thomasjensen.checkstyle.addons.build.DependencyConfig;
import com.thomasjensen.checkstyle.addons.build.TaskCreator;


/**
 * Gradle task to create a sources JAR.
 */
public class JarSourcesTaskConfigurer
    implements ConfigurableAddonsTask
{
    private final Jar jarTask;



    public JarSourcesTaskConfigurer(@Nonnull final Jar pJarTask)
    {
        super();
        jarTask = pJarTask;
    }



    @Override
    public void configureFor(@Nonnull final DependencyConfig pDepConfig)
    {
        final BuildUtil buildUtil = new BuildUtil(jarTask.getProject());

        jarTask.setGroup(TaskCreator.ARTIFACTS_GROUP_NAME);
        jarTask.getArchiveClassifier().set("sources");

        // set appendix for archive name
        if (!pDepConfig.isDefaultConfig()) {
            final String appendix = pDepConfig.getName();
            jarTask.getArchiveAppendix().set(appendix);
        }
        jarTask.setDescription("Build the source JAR for dependency configuration '" + pDepConfig.getName() + "'");

        // SourceSet that fits the dependency configuration
        final SourceSet mainSourceSet = buildUtil.getSourceSet(SourceSet.MAIN_SOURCE_SET_NAME);
        final SourceSet sqSourceSet = buildUtil.getSourceSet(BuildUtil.SONARQUBE_SOURCE_SET_NAME);

        // Configuration of JAR file contents
        jarTask.from(mainSourceSet.getAllJava());
        jarTask.from(sqSourceSet.getAllJava());
        jarTask.into("META-INF", copySpec -> copySpec.from("LICENSE"));

        // Manifest
        buildUtil.inheritManifest(jarTask, pDepConfig);
    }
}
