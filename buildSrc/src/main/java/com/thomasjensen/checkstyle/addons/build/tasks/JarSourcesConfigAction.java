package com.thomasjensen.checkstyle.addons.build.tasks;
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

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.bundling.Jar;

import com.thomasjensen.checkstyle.addons.build.BuildUtil;
import com.thomasjensen.checkstyle.addons.build.DependencyConfig;
import com.thomasjensen.checkstyle.addons.build.TaskCreator;


/**
 * Configure a Gradle task to create a sources JAR.
 */
public class JarSourcesConfigAction
    extends AbstractTaskConfigAction<Jar>
{
    public JarSourcesConfigAction(@Nonnull DependencyConfig pDepConfig)
    {
        super(pDepConfig);
    }



    @Override
    protected void configureTaskFor(@Nonnull Jar pJarTask, @Nullable DependencyConfig pDepConfig)
    {
        Objects.requireNonNull(pDepConfig, "required dependency config not present");
        pJarTask.setGroup(TaskCreator.ARTIFACTS_GROUP_NAME);
        pJarTask.getArchiveClassifier().set("sources");

        // set appendix for archive name
        if (!pDepConfig.isDefaultConfig()) {
            final String appendix = pDepConfig.getName();
            pJarTask.getArchiveAppendix().set(appendix);
        }
        pJarTask.setDescription("Build the source JAR for dependency configuration '" + pDepConfig.getName() + "'");

        // SourceSet that fits the dependency configuration
        final SourceSet mainSourceSet = buildUtil.getSourceSet(SourceSet.MAIN_SOURCE_SET_NAME);
        final SourceSet sqSourceSet = buildUtil.getSourceSet(BuildUtil.SONARQUBE_SOURCE_SET_NAME);

        // Configuration of JAR file contents
        pJarTask.from(mainSourceSet.getAllJava());
        pJarTask.from(sqSourceSet.getAllJava());
        pJarTask.into("META-INF", copySpec -> copySpec.from("LICENSE"));

        // Manifest
        buildUtil.inheritManifest(pJarTask, pDepConfig);
        buildUtil.addBuildTimestampDeferred(pJarTask);
    }
}
