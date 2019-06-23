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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

import groovy.lang.Closure;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.CopySpec;
import org.gradle.api.java.archives.Attributes;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskInputs;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.compile.JavaCompile;

import com.thomasjensen.checkstyle.addons.build.BuildUtil;
import com.thomasjensen.checkstyle.addons.build.DependencyConfig;
import com.thomasjensen.checkstyle.addons.build.ExtProp;
import com.thomasjensen.checkstyle.addons.build.TaskNames;


/**
 * Gradle task to produce the SonarQube plugin.
 */
public class CreateJarSonarqubeTask
    extends AbstractAddonsJarTask
{
    public CreateJarSonarqubeTask()
    {
        super();
    }



    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public void configureFor(@Nonnull final DependencyConfig pDepConfig)
    {
        final Project project = getProject();

        setDescription("Assembles the SonarQube plugin for dependency configuration '" + pDepConfig.getName() + "'");

        // Inputs for up-to-date checking
        TaskInputs inputs = getInputs();
        inputs.property(BuildUtil.GROUP_ID, project.getGroup());
        inputs.property(BuildUtil.VERSION, project.getVersion());
        inputs.property("name", getBuildUtil().getLongName());
        inputs.property("description", project.getDescription());
        inputs.property("authorName", getBuildUtil().getExtraPropertyValue(ExtProp.AuthorName));
        inputs.property("sqPluginKey", getBuildUtil().getExtraPropertyValue(ExtProp.SqPluginKey));
        inputs.property("sqPackage", getBuildUtil().getExtraPropertyValue(ExtProp.SqPackage));
        inputs.property("gitHash", getBuildUtil().getExtraPropertyValue(ExtProp.GitHash));
        inputs.property("orgUrl", getBuildUtil().getExtraPropertyValue(ExtProp.OrgUrl));
        inputs.property("issueTrackerUrl", getBuildUtil().getExtraPropertyValue(ExtProp.IssueTrackerUrl));
        inputs.property("website", getBuildUtil().getExtraPropertyValue(ExtProp.Website));

        // archive name
        getArchiveFileName().set(
            "sonar-" + inputs.getProperties().get("sqPluginKey") + "-" + inputs.getProperties().get(BuildUtil.VERSION)
                + (pDepConfig.isDefaultConfig() ? "" : ("-csp" + pDepConfig.getSonarQubeMinCsPluginVersion()))
                + ".jar");

        // Task Dependencies
        final Jar jarTask = (Jar) getBuildUtil().getTask(TaskNames.jar, pDepConfig);
        dependsOn(jarTask);
        final Task sqClassesTask = getBuildUtil().getTask(TaskNames.sonarqubeClasses, pDepConfig);
        dependsOn(sqClassesTask);

        // Configuration of JAR file contents
        intoFrom("META-INF", "LICENSE");

        final JavaCompile compileTask = (JavaCompile) getBuildUtil().getTask(TaskNames.compileSonarqubeJava,
            pDepConfig);
        from(compileTask.getDestinationDir());

        into(inputs.getProperties().get("sqPackage"), new Closure<Void>(this)
        {
            @Override
            public Void call(final Object... pArgs)
            {
                CopySpec spec = (CopySpec) getDelegate();
                final SourceSet sqSourceSet = getBuildUtil().getSourceSet(BuildUtil.SONARQUBE_SOURCE_SET_NAME);
                spec.from(new File(sqSourceSet.getOutput().getResourcesDir(), "sonarqube.xml"));
                filterVersion(spec, inputs.getProperties().get(BuildUtil.VERSION).toString());
                return null;
            }
        });

        final Set<File> pubLibs = CreateJarEclipseTask.getPublishedDependencyLibs(this, pDepConfig);
        intoFrom("META-INF/lib", jarTask.getArchiveFile().get().getAsFile());
        intoFrom("META-INF/lib", pubLibs);

        // Manifest
        setManifestAttributes(getManifest().getAttributes(), inputs, pDepConfig, pubLibs);
    }



    private void setManifestAttributes(@Nonnull final Attributes pAttributes, @Nonnull final TaskInputs pInputs,
        @Nonnull final DependencyConfig pDepConfig, @Nonnull final Set<File> pPubLibs)
    {
        final String baseCsVersion = pDepConfig.getCheckstyleBaseVersion();
        final Map<String, Object> inputProps = pInputs.getProperties();
        final Jar jarTask = (Jar) getBuildUtil().getTask(TaskNames.jar, pDepConfig);

        pAttributes.clear();
        pAttributes.put("Plugin-Name", inputProps.get("name"));
        pAttributes.put("Plugin-Base", "checkstyle");
        pAttributes.put("Plugin-Key", inputProps.get("sqPluginKey"));
        pAttributes.put("Implementation-Build", inputProps.get("gitHash"));
        pAttributes.put("Plugin-Description",
            inputProps.get("description") + " (based on Checkstyle " + baseCsVersion + ")");
        pAttributes.put("Plugin-Version", inputProps.get(BuildUtil.VERSION));
        pAttributes.put("Plugin-Organization", inputProps.get("authorName"));
        pAttributes.put("Plugin-OrganizationUrl", inputProps.get("orgUrl"));
        pAttributes.put("Plugin-SourcesUrl", "https://github.com/checkstyle-addons/checkstyle-addons");
        pAttributes.put("Plugin-IssueTrackerUrl", inputProps.get("issueTrackerUrl"));
        pAttributes.put("Plugin-Class", "com.thomasjensen.checkstyle.addons.sonarqube.CheckstyleExtensionPlugin");
        pAttributes.put("Plugin-RequirePlugins", "checkstyle:" + pDepConfig.getSonarQubeMinCsPluginVersion());
        pAttributes.put("Plugin-Dependencies", "META-INF/lib/" + jarTask.getArchiveFileName().get() //
            + (pPubLibs.size() > 0 ? " " : "") //
            + CreateJarEclipseTask.flattenPrefixLibs("META-INF/lib/", pPubLibs, ' '));
        pAttributes.put("Plugin-License", "GPLv3");
        pAttributes.put("Plugin-Homepage", inputProps.get("website"));
        //attrs.put("Plugin-TermsConditionsUrl", "");
        // TODO use same SonarQube version in manifest and compile dependencies
        pAttributes.put("Sonar-Version", pDepConfig.getSonarQubeMinPlatformVersion());
        pAttributes.putAll(CreateJarTask.mfAttrStd(getProject()));
        pAttributes.remove("Website");

        doFirst(new Closure<Void>(this)
        {
            @Override
            @SuppressWarnings("MethodDoesntCallSuperMethod")
            public Void call()
            {
                final DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");  // required by SonarQube
                pAttributes.put("Plugin-BuildDate",
                    sdf.format(getBuildUtil().getExtraPropertyValue(ExtProp.BuildTimestamp)));
                return null;
            }
        });
    }
}
