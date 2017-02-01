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
import org.gradle.api.Project;
import org.gradle.api.file.CopySpec;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.bundling.Jar;

import com.thomasjensen.checkstyle.addons.build.BuildUtil;
import com.thomasjensen.checkstyle.addons.build.DependencyConfigs;
import com.thomasjensen.checkstyle.addons.build.NameFactory;
import com.thomasjensen.checkstyle.addons.build.SourceSetNames;
import com.thomasjensen.checkstyle.addons.build.TaskNames;


/**
 * Gradle task to create a sources JAR.
 *
 * @author Thomas Jensen
 */
public class CreateJarSourcesTask
    extends Jar
{
    /**
     * Constructor.
     */
    public CreateJarSourcesTask()
    {
        super();
        setGroup(BasePlugin.BUILD_GROUP);
        final String longName = BuildUtil.getExtraPropertyValue(getProject(), "longName");
        setDescription(longName + ": Build the source JAR for publication '");

        setClassifier("sources");
    }



    /**
     * Configure this task instance for a given dependency configuration.
     *
     * @param pCheckstyleVersion the Checkstyle version for which to configure
     */
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public void configureFor(final String pCheckstyleVersion)
    {
        final Project project = getProject();
        final NameFactory nameFactory = BuildUtil.getExtraPropertyValue(project, "nameFactory");
        final DependencyConfigs depConfigs = BuildUtil.getExtraPropertyValue(project, "depConfigs");
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

        // SourceSet that fits the dependency configuration
        SourceSet mainSourceSet = nameFactory.getSourceSet(SourceSetNames.main, pCheckstyleVersion);

        // Configuration of JAR file contents
        from(mainSourceSet.getAllJava());
        into("META-INF", new Closure<Void>(this)
        {
            @Override
            public Void call(final Object... pArgs)
            {
                CopySpec spec = (CopySpec) getDelegate();
                spec.from("LICENSE");
                return null;
            }
        });

        // Manifest
        doFirst(new Closure<Void>(this)
        {
            @Override
            public Void call()
            {
                Jar jarTask = (Jar) nameFactory.getTask(TaskNames.jar, pCheckstyleVersion);
                setManifest(jarTask.getManifest());
                getManifest().getAttributes().put("Build-Timestamp",
                    BuildUtil.getExtraPropertyValue(project, "buildTimestamp").toString());
                return null;
            }
        });
    }
}
