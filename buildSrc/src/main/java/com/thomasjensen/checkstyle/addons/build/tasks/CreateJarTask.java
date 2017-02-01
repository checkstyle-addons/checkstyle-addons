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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import groovy.lang.Closure;
import org.apache.tools.ant.filters.ReplaceTokens;
import org.gradle.api.Project;
import org.gradle.api.file.CopySpec;
import org.gradle.api.java.archives.Attributes;
import org.gradle.api.java.archives.Manifest;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.internal.jvm.Jvm;
import org.gradle.util.GradleVersion;

import com.thomasjensen.checkstyle.addons.build.BuildUtil;
import com.thomasjensen.checkstyle.addons.build.DependencyConfigs;
import com.thomasjensen.checkstyle.addons.build.NameFactory;
import com.thomasjensen.checkstyle.addons.build.SourceSetNames;
import com.thomasjensen.checkstyle.addons.build.TaskNames;


/**
 * Gradle task to create the main binary JAR.
 *
 * @author Thomas Jensen
 */
public class CreateJarTask
    extends Jar
{
    /**
     * Constructor.
     */
    public CreateJarTask()
    {
        super();
        setGroup(BasePlugin.BUILD_GROUP);
        final String longName = BuildUtil.getExtraPropertyValue(getProject(), "longName");
        setDescription(longName + ": Assembles a jar archive containing the classes of '");
    }



    /**
     * Build a little map with standard manifest attributes.
     *
     * @param pProject Gradle project
     * @return the map
     */
    public static Map<String, String> mfAttrStd(final Project pProject)
    {
        Map<String, String> result = new HashMap<>();
        result.put("Manifest-Version", "1.0");
        result.put("Website", BuildUtil.getExtraPropertyValue(pProject, "website"));
        result.put("Created-By", GradleVersion.current().toString());
        result.put("Built-By", System.getProperty("user.name"));
        result.put("Build-Jdk", Jvm.current().toString());
        return result;
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
        final String appendix = depConfigs.getDepConfig(pCheckstyleVersion).getPublicationSuffix();
        if (!isDefaultPublication) {
            setAppendix(appendix);
        }

        // Dependency on pom.properties generating task
        dependsOn(nameFactory.getName(TaskNames.generatePomProperties, pCheckstyleVersion));
        final File pomPropsUsed = ((GeneratePomPropsTask) nameFactory.getTask(TaskNames.generatePomProperties,
            pCheckstyleVersion)).getPluginPomProps();
        getInputs().file(pomPropsUsed);

        // Dependency on pom.xml generating task
        dependsOn(nameFactory.getName(TaskNames.generatePom, pCheckstyleVersion));
        final File pomUsed = ((GeneratePomFileTask) nameFactory.getTask(TaskNames.generatePom, pCheckstyleVersion))
            .getPomFile();
        getInputs().file(pomUsed);

        // Dependency on 'classes' task (compile and resources)
        dependsOn(nameFactory.getName(TaskNames.mainClasses, pCheckstyleVersion));

        // SourceSet that fits the dependency configuration
        SourceSet mainSourceSet = nameFactory.getSourceSet(SourceSetNames.main, pCheckstyleVersion);
        setDescription(getDescription() + mainSourceSet.getName() + "'.");

        // Configuration of JAR file contents
        from(mainSourceSet.getOutput().getClassesDir());
        from(mainSourceSet.getOutput().getResourcesDir());
        from(pomUsed);

        exclude("**/sonarqube/**",       //
            "download-guide.html",       //
            "sonarqube.xml",             //
            "**/*.md",                   //
            "**/checks/all_checks.html", //
            "eclipsecs-plugin.xml",      //
            "**/checkstyle-metadata.*");

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

        // add generated pom.xml and pom.properties to archive, setting build timestamp in the process
        into("META-INF/maven/" + project.getGroup() + "/" + project.getName(), new Closure<Void>(this)
        {
            @Override
            public Void call(final Object... pArgs)
            {
                CopySpec spec = (CopySpec) getDelegate();
                spec.from(pomUsed);
                spec.from(pomPropsUsed);
                Map<String, String> placeHolders = new HashMap<>();
                String buildTimestamp = BuildUtil.getExtraPropertyValue(project, "buildTimestamp").toString();
                placeHolders.put("buildTimestamp", buildTimestamp);
                Map<String, Object> propsMap = new HashMap<>();
                propsMap.put("tokens", placeHolders);
                spec.filter(propsMap, ReplaceTokens.class);
                return null;
            }
        });

        // Manifest
        String effectiveName = project.getName();
        if (!isDefaultPublication) {
            effectiveName += '-' + appendix;
        }
        Manifest mafest = getManifest();
        final Attributes attrs = mafest.getAttributes();
        attrs.clear();
        attrs.put("Specification-Title", effectiveName);
        attrs.put("Specification-Vendor", BuildUtil.getExtraPropertyValue(project, "authorName"));
        attrs.put("Specification-Vendor-Id", "com.thomasjensen");
        attrs.put("Specification-Version", project.getVersion());
        attrs.put("Implementation-Title", effectiveName);
        attrs.put("Implementation-Vendor", BuildUtil.getExtraPropertyValue(project, "authorName"));
        attrs.put("Implementation-Vendor-Id", "com.thomasjensen");
        attrs.put("Implementation-Version", project.getVersion());
        attrs.put("Implementation-Build", BuildUtil.getExtraPropertyValue(project, "gitHash"));
        attrs.put("Checkstyle-Version", pCheckstyleVersion);
        attrs.putAll(mfAttrStd(project));

        doFirst(new Closure<Void>(this)
        {
            @Override
            public Void call()
            {
                // add build timestamp in execution phase so that it does not count for the up-to-date check
                attrs.put("Build-Timestamp", BuildUtil.getExtraPropertyValue(project, "buildTimestamp").toString());
                return null;
            }
        });
    }
}
