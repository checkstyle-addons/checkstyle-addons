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

import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.javadoc.Javadoc;

import com.thomasjensen.checkstyle.addons.build.BuildUtil;
import com.thomasjensen.checkstyle.addons.build.DependencyConfig;
import com.thomasjensen.checkstyle.addons.build.TaskNames;


/**
 * Gradle task to create Javadoc JARs for publication.
 */
public class JarJavadocTaskConfigurer
    implements ConfigurableAddonsTask
{
    private final Jar jarTask;



    public JarJavadocTaskConfigurer(@Nonnull final Jar pJarTask)
    {
        super();
        jarTask = pJarTask;
    }



    @Override
    public void configureFor(@Nonnull final DependencyConfig pDepConfig)
    {
        final BuildUtil buildUtil = new BuildUtil(jarTask.getProject());

        jarTask.getArchiveClassifier().set("javadoc");

        // set appendix for archive name
        if (!pDepConfig.isDefaultConfig()) {
            final String appendix = pDepConfig.getName();
            jarTask.getArchiveAppendix().set(appendix);
        }
        jarTask.setDescription("Build the javadoc JAR for dependency configuration '" + pDepConfig.getName() + "'");

        // Dependency on javadoc generating task
        final Javadoc javadocTask = buildUtil.getTask(TaskNames.javadoc, Javadoc.class, pDepConfig);
        jarTask.dependsOn(javadocTask);

        // Configuration of JAR file contents
        jarTask.from(javadocTask.getDestinationDir());
        jarTask.into("META-INF", copySpec -> copySpec.from("LICENSE"));

        // Manifest
        buildUtil.inheritManifest(jarTask, pDepConfig);
    }
}
