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

import javax.annotation.Nonnull;

import org.gradle.api.tasks.javadoc.Javadoc;

import com.thomasjensen.checkstyle.addons.build.DependencyConfig;
import com.thomasjensen.checkstyle.addons.build.TaskNames;


/**
 * Gradle task to create Javadoc JARs for publication.
 */
public class CreateJarJavadocTask
    extends AbstractAddonsJarTask
{
    public CreateJarJavadocTask()
    {
        super();
        getArchiveClassifier().set("javadoc");
    }



    @Override
    public void configureFor(@Nonnull final DependencyConfig pDepConfig)
    {
        // set appendix for archive name
        if (!pDepConfig.isDefaultConfig()) {
            final String appendix = pDepConfig.getName();
            getArchiveAppendix().set(appendix);
        }
        setDescription("Build the javadoc JAR for dependency configuration '" + pDepConfig.getName() + "'");

        // Dependency on javadoc generating task
        final Javadoc javadocTask = (Javadoc) getBuildUtil().getTask(TaskNames.javadoc, pDepConfig);
        dependsOn(javadocTask);

        // Configuration of JAR file contents
        from(javadocTask.getDestinationDir());
        intoFrom("META-INF", "LICENSE");

        // Manifest
        getBuildUtil().inheritManifest(this, pDepConfig);
    }
}
