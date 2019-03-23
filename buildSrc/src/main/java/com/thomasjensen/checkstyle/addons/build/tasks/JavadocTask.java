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

import java.io.File;
import java.nio.charset.StandardCharsets;
import javax.annotation.Nonnull;

import org.gradle.api.JavaVersion;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.StandardJavadocDocletOptions;

import com.thomasjensen.checkstyle.addons.build.BuildUtil;
import com.thomasjensen.checkstyle.addons.build.ClasspathBuilder;
import com.thomasjensen.checkstyle.addons.build.DependencyConfig;
import com.thomasjensen.checkstyle.addons.build.JavaLevelUtil;
import com.thomasjensen.checkstyle.addons.build.TaskNames;


/**
 * Create Javadoc for the given dependency configuration.
 */
public class JavadocTask
    extends Javadoc
    implements ConfigurableAddonsTask
{
    private final BuildUtil buildUtil;



    public JavadocTask()
    {
        super();
        buildUtil = new BuildUtil(getProject());
        setGroup(JavaBasePlugin.DOCUMENTATION_GROUP);
    }



    @Override
    public void configureFor(@Nonnull final DependencyConfig pDepConfig)
    {
        final Project project = getProject();
        final JavaVersion javaLevel = pDepConfig.getJavaLevel();

        setDescription("Generate Javadoc API documentation for dependency configuration '" + pDepConfig.getName()
            + "' (Java level: " + javaLevel + ")");
        dependsOn(buildUtil.getTask(TaskNames.compileJava, pDepConfig));

        final JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
        setDestinationDir(new File(javaConvention.getDocsDir(), getName()));

        configureJavadocTask(this, pDepConfig);

        final JavaLevelUtil javaLevelUtil = new JavaLevelUtil(project);
        if (javaLevelUtil.isOlderSupportedJava(javaLevel)) {
            setExecutable(javaLevelUtil.getJavadocExecutable(javaLevel));
        }
    }



    public static void configureJavadocTask(final Javadoc pTask, final DependencyConfig pDepConfig)
    {
        final Project project = pTask.getProject();
        final BuildUtil buildUtil = new BuildUtil(project);

        pTask.setTitle(buildUtil.getLongName() + " v" + project.getVersion());
        final StandardJavadocDocletOptions options = (StandardJavadocDocletOptions) pTask.getOptions();
        options.setEncoding(StandardCharsets.UTF_8.toString());
        options.setDocEncoding(StandardCharsets.UTF_8.toString());
        options.setCharSet(StandardCharsets.UTF_8.toString());
        options.setAuthor(false);
        options.setUse(true);
        options.setNoDeprecated(true);
        options.setWindowTitle(buildUtil.getLongName());
        options.setSplitIndex(false);
        options.setHeader(buildUtil.getLongName());
        options.setLinks(pDepConfig.getJavadocLinks());

        pTask.setSource(buildUtil.getSourceSet(SourceSet.MAIN_SOURCE_SET_NAME).getAllJava()
            .plus(buildUtil.getSourceSet(BuildUtil.SONARQUBE_SOURCE_SET_NAME).getAllJava()));
        pTask.setClasspath(new ClasspathBuilder(project)
            .buildClassPath(pDepConfig, null, false, buildUtil.getSourceSet(SourceSet.MAIN_SOURCE_SET_NAME),
                buildUtil.getSourceSet(BuildUtil.SONARQUBE_SOURCE_SET_NAME)));

        // javadoc does not inherit the proxy settings (https://issues.gradle.org/browse/GRADLE-1228)
        if (System.getProperty("http.proxyHost") != null) {
            options.jFlags("-DproxyHost=" + System.getProperty("http.proxyHost"),
                "-DproxyPort=" + System.getProperty("http.proxyPort"),
                "-DproxyUser=" + System.getProperty("http.proxyUser"),
                "-DproxyPassword=" + System.getProperty("http.proxyPassword"));
        }
    }
}
