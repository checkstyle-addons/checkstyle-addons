package com.thomasjensen.checkstyle.addons.build.tasks;
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

import groovy.lang.Closure;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.javadoc.Javadoc;

import com.thomasjensen.checkstyle.addons.build.DependencyConfigs;
import com.thomasjensen.checkstyle.addons.build.ExtProp;
import com.thomasjensen.checkstyle.addons.build.NameFactory;
import com.thomasjensen.checkstyle.addons.build.TaskNames;


/**
 * Gradle task to create Javadoc JARs for publication.
 *
 * @author Thomas Jensen
 */
public class CreateJarJavadocTask
    extends AbstractAddonsJarTask
{
    /**
     * Constructor.
     */
    public CreateJarJavadocTask()
    {
        super();
        setGroup(BasePlugin.BUILD_GROUP);
        setDescription(getBuildUtil().getLongName() + ": Build the javadoc JAR for publication '");
        setClassifier("javadoc");
    }



    /**
     * Configure this task instance for a given dependency configuration.
     *
     * @param pCheckstyleVersion the Checkstyle version for which to configure
     */
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public void configureFor(final String pCheckstyleVersion)
    {
        final NameFactory nameFactory = getBuildUtil().getNameFactory();
        final DependencyConfigs depConfigs = getBuildUtil().getDepConfigs();
        final boolean isDefaultPublication = depConfigs.isDefault(pCheckstyleVersion);

        // set appendix for archive name
        if (!isDefaultPublication) {
            final String appendix = depConfigs.getDepConfig(pCheckstyleVersion).getPublicationSuffix();
            setAppendix(appendix);
            setDescription(getDescription() + appendix + "'");
        }
        else {
            setDescription(getDescription() + "Default'");
        }

        // Dependency on javadoc generating task
        Javadoc javadocTask = (Javadoc) nameFactory.getTask(TaskNames.javadoc, pCheckstyleVersion);
        dependsOn(javadocTask);

        // Configuration of JAR file contents
        from(javadocTask.getDestinationDir());
        intoFrom("META-INF", "LICENSE");

        // Manifest
        doFirst(new Closure<Void>(this)
        {
            @Override
            public Void call()
            {
                Jar jarTask = (Jar) nameFactory.getTask(TaskNames.jar, pCheckstyleVersion);
                setManifest(jarTask.getManifest());
                getManifest().getAttributes().put("Build-Timestamp",
                    getBuildUtil().getExtraPropertyValue(ExtProp.BuildTimestamp).toString());
                return null;
            }
        });
    }
}