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
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

import groovy.lang.Closure;
import org.apache.tools.ant.filters.ReplaceTokens;
import org.gradle.api.Project;
import org.gradle.api.file.CopySpec;
import org.gradle.api.java.archives.Attributes;
import org.gradle.api.java.archives.Manifest;
import org.gradle.api.tasks.SourceSet;
import org.gradle.internal.jvm.Jvm;
import org.gradle.util.GradleVersion;

import com.thomasjensen.checkstyle.addons.build.BuildUtil;
import com.thomasjensen.checkstyle.addons.build.ClasspathBuilder;
import com.thomasjensen.checkstyle.addons.build.DependencyConfig;
import com.thomasjensen.checkstyle.addons.build.ExtProp;
import com.thomasjensen.checkstyle.addons.build.TaskNames;


/**
 * Gradle task to create the main binary JAR.
 */
public class CreateJarTask
    extends AbstractAddonsJarTask
{
    public CreateJarTask()
    {
        super();
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
        result.put("Website", new BuildUtil(pProject).getExtraPropertyValue(ExtProp.Website));
        result.put("Created-By", GradleVersion.current().toString());
        result.put("Built-By", System.getProperty("user.name"));
        result.put("Build-Jdk", Jvm.current().toString());
        return result;
    }



    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public void configureFor(@Nonnull final DependencyConfig pDepConfig)
    {
        final Project project = getProject();

        setDescription("Assembles a jar archive containing the '" + SourceSet.MAIN_SOURCE_SET_NAME
                + "' classes for dependency configuration '" + pDepConfig.getName() + "'");

        // set appendix for archive name
        final String appendix = pDepConfig.getName();
        if (!pDepConfig.isDefaultConfig()) {
            getArchiveAppendix().set(appendix);
        }

        // Dependency on pom.properties generating task
        dependsOn(TaskNames.generatePomProperties.getName(pDepConfig));

        // Task Input pom.properties file
        final File pomPropsUsed = ((GeneratePomPropsTask) getBuildUtil().getTask(TaskNames.generatePomProperties,
            pDepConfig)).getPluginPomProps();
        getInputs().file(pomPropsUsed);

        // Dependency on pom.xml generating task
        dependsOn(TaskNames.generatePom.getName(pDepConfig));

        // Task Input: pom.xml
        final File pomUsed = ((GeneratePomFileTask) getBuildUtil().getTask(TaskNames.generatePom, pDepConfig))
            .getPomFile();
        getInputs().file(pomUsed);

        // Dependency on 'classes' task (compile and resources)
        dependsOn(getBuildUtil().getTask(TaskNames.mainClasses, pDepConfig));

        // Configuration of JAR file contents
        from(pomUsed);
        final SourceSet mainSourceSet = getBuildUtil().getSourceSet(SourceSet.MAIN_SOURCE_SET_NAME);
        from(new ClasspathBuilder(project).getClassesDirs(mainSourceSet, pDepConfig));
        from(mainSourceSet.getOutput().getResourcesDir());

        exclude("download-guide.html",   //
            "**/*.md",                   //
            "**/checks/all_checks.html", //
            "eclipsecs-plugin.xml",      //
            "**/checkstyle-metadata.*");

        intoFrom("META-INF", "LICENSE");

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
                String buildTimestamp = getBuildUtil().getExtraPropertyValue(ExtProp.BuildTimestamp).toString();
                placeHolders.put("buildTimestamp", buildTimestamp);
                Map<String, Object> propsMap = new HashMap<>();
                propsMap.put("tokens", placeHolders);
                spec.filter(propsMap, ReplaceTokens.class);
                return null;
            }
        });

        // Manifest
        String effectiveName = project.getName();
        if (!pDepConfig.isDefaultConfig()) {
            effectiveName += '-' + appendix;
        }
        Manifest mafest = getManifest();
        final Attributes attrs = mafest.getAttributes();
        attrs.clear();
        attrs.put("Specification-Title", effectiveName);
        attrs.put("Specification-Vendor", getBuildUtil().getExtraPropertyValue(ExtProp.AuthorName));
        attrs.put("Specification-Vendor-Id", "com.thomasjensen");
        attrs.put("Specification-Version", project.getVersion());
        attrs.put("Implementation-Title", effectiveName);
        attrs.put("Implementation-Vendor", getBuildUtil().getExtraPropertyValue(ExtProp.AuthorName));
        attrs.put("Implementation-Vendor-Id", "com.thomasjensen");
        attrs.put("Implementation-Version", project.getVersion());
        attrs.put("Implementation-Build", getBuildUtil().getExtraPropertyValue(ExtProp.GitHash));
        attrs.put("Checkstyle-Version", pDepConfig.getCheckstyleBaseVersion());
        attrs.putAll(mfAttrStd(project));

        getBuildUtil().addBuildTimestampDeferred(this, attrs);
    }
}
