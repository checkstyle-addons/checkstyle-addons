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

import java.io.File;
import java.nio.charset.StandardCharsets;
import javax.annotation.Nonnull;

import org.gradle.api.JavaVersion;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.compile.JavaCompile;
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
public class JavadocTaskConfigurer
    implements ConfigurableAddonsTask
{
    private final Javadoc javadocTask;

    private final BuildUtil buildUtil;



    public JavadocTaskConfigurer(@Nonnull final Javadoc pJavadocTask)
    {
        super();
        javadocTask = pJavadocTask;
        buildUtil = new BuildUtil(pJavadocTask.getProject());
    }



    @Override
    public void configureFor(@Nonnull final DependencyConfig pDepConfig)
    {
        final Project project = javadocTask.getProject();
        final JavaVersion javaLevel = pDepConfig.getJavaLevel();

        javadocTask.setGroup(JavaBasePlugin.DOCUMENTATION_GROUP);
        javadocTask.setDescription("Generate Javadoc API documentation for dependency configuration '"
            + pDepConfig.getName() + "' (Java level: " + javaLevel + ")");

        javadocTask.dependsOn(
            buildUtil.getTaskProvider(TaskNames.compileJava, JavaCompile.class, pDepConfig),
            buildUtil.getTaskProvider(TaskNames.compileSonarqubeJava, JavaCompile.class, pDepConfig)
        );

        final JavaPluginExtension javaExt = project.getExtensions().getByType(JavaPluginExtension.class);
        javadocTask.setDestinationDir(new File(javaExt.getDocsDir().getAsFile().get(), javadocTask.getName()));

        configureJavadocTask(pDepConfig);

        final JavaLevelUtil javaLevelUtil = new JavaLevelUtil(project);
        if (javaLevelUtil.isOlderSupportedJava(javaLevel)) {
            javadocTask.setExecutable(javaLevelUtil.getJavadocExecutable(javaLevel));
        }
    }



    /**
     * These configurations are also applied to the standard 'javadoc' task, plus they are applied to ours.
     *
     * @param pDepConfig dependency config
     */
    public void configureJavadocTask(final DependencyConfig pDepConfig)
    {
        final Project project = javadocTask.getProject();

        final StandardJavadocDocletOptions options = (StandardJavadocDocletOptions) javadocTask.getOptions();
        options.setEncoding(StandardCharsets.UTF_8.toString());
        options.setDocEncoding(StandardCharsets.UTF_8.toString());
        options.setCharSet(StandardCharsets.UTF_8.toString());
        options.setAuthor(false);
        options.setUse(true);
        options.setNoDeprecated(true);
        options.setSplitIndex(false);
        options.setLinks(pDepConfig.getJavadocLinks());
        options.setHeader(buildUtil.getBuildConfig().getLongName().get());
        options.setWindowTitle(buildUtil.getBuildConfig().getLongName().get());
        javadocTask.setTitle(buildUtil.getBuildConfig().getLongName().get() + " v" + project.getVersion());

        javadocTask.setSource(buildUtil.getSourceSet(SourceSet.MAIN_SOURCE_SET_NAME).getAllJava()
            .plus(buildUtil.getSourceSet(BuildUtil.SONARQUBE_SOURCE_SET_NAME).getAllJava()));
        javadocTask.setClasspath(new ClasspathBuilder(project).buildJavadocClasspath(pDepConfig));

        // javadoc does not inherit the proxy settings (https://issues.gradle.org/browse/GRADLE-1228)
        if (System.getProperty("http.proxyHost") != null) {
            options.jFlags("-DproxyHost=" + System.getProperty("http.proxyHost"),
                "-DproxyPort=" + System.getProperty("http.proxyPort"),
                "-DproxyUser=" + System.getProperty("http.proxyUser"),
                "-DproxyPassword=" + System.getProperty("http.proxyPassword"));
        }
    }
}
