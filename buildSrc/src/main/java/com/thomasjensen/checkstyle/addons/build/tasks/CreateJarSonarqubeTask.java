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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Set;

import groovy.lang.Closure;
import org.gradle.api.Project;
import org.gradle.api.file.CopySpec;
import org.gradle.api.java.archives.Attributes;
import org.gradle.api.java.archives.Manifest;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskInputs;
import org.gradle.api.tasks.bundling.Jar;

import com.thomasjensen.checkstyle.addons.build.BuildUtil;
import com.thomasjensen.checkstyle.addons.build.DependencyConfig;
import com.thomasjensen.checkstyle.addons.build.DependencyConfigs;
import com.thomasjensen.checkstyle.addons.build.NameFactory;
import com.thomasjensen.checkstyle.addons.build.SourceSetNames;
import com.thomasjensen.checkstyle.addons.build.TaskNames;


/**
 * Gradle task to produce the SonarQube plugin.
 *
 * @author Thomas Jensen
 */
public class CreateJarSonarqubeTask
    extends AbstractAddonsJarTask
{
    /**
     * Constructor.
     */
    public CreateJarSonarqubeTask()
    {
        super();
        setGroup(BasePlugin.BUILD_GROUP);
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
        final DependencyConfig theVersions = depConfigs.getDepConfig(pCheckstyleVersion);
        final boolean isDefaultConfig = depConfigs.isDefault(pCheckstyleVersion);
        final String longName = BuildUtil.getExtraPropertyValue(project, "longName");

        setDescription(longName + ": Assembles the SonarQube plugin for Checkstyle " + pCheckstyleVersion);

        // Inputs for up-to-date checking
        TaskInputs inputs = getInputs();
        inputs.property(BuildUtil.GROUP_ID, project.getGroup());
        inputs.property(BuildUtil.VERSION, project.getVersion());
        inputs.property("name", longName);
        inputs.property("description", project.getDescription());
        inputs.property("authorName", BuildUtil.getExtraPropertyValue(project, "authorName"));
        inputs.property("sqPluginKey", BuildUtil.getExtraPropertyValue(project, "sqPluginKey"));
        inputs.property("sqPackage", BuildUtil.getExtraPropertyValue(project, "sqPackage"));
        inputs.property("gitHash", BuildUtil.getExtraPropertyValue(project, "gitHash"));
        inputs.property("orgUrl", BuildUtil.getExtraPropertyValue(project, "orgUrl"));
        inputs.property("issueTrackerUrl", BuildUtil.getExtraPropertyValue(project, "issueTrackerUrl"));
        inputs.property("website", BuildUtil.getExtraPropertyValue(project, "website"));

        // archive name
        setArchiveName(
            "sonar-" + inputs.getProperties().get("sqPluginKey") + "-" + inputs.getProperties().get(BuildUtil.VERSION)
                + (isDefaultConfig ? "" : ("-csp" + theVersions.getSonarQubeMinCsPluginVersion())) + ".jar");

        // Dependency on 'jar' task
        final Jar jarTask = (Jar) nameFactory.getTask(TaskNames.jar, pCheckstyleVersion);
        dependsOn(jarTask);

        // SourceSet that fits the dependency configuration
        final SourceSet mainSourceSet = nameFactory.getSourceSet(SourceSetNames.main, pCheckstyleVersion);

        // Configuration of JAR file contents
        intoFrom("META-INF", "LICENSE");

        into(inputs.getProperties().get("sqPackage"), new Closure<Void>(this)
        {
            @Override
            public Void call(final Object... pArgs)
            {
                CopySpec spec = (CopySpec) getDelegate();
                spec.from(new File(mainSourceSet.getOutput().getClassesDir(),
                    (String) inputs.getProperties().get("sqPackage")), new Closure<Void>(this)
                {
                    @Override
                    public Void call(final Object... pArgs)
                    {
                        CopySpec spec = (CopySpec) getDelegate();
                        spec.include("CheckstyleExtensionPlugin.class", "CheckstyleExtensionRepository.class");
                        return null;
                    }
                });
                return null;
            }
        });

        into(inputs.getProperties().get("sqPackage"), new Closure<Void>(this)
        {
            @Override
            public Void call(final Object... pArgs)
            {
                CopySpec spec = (CopySpec) getDelegate();
                spec.from(new File(mainSourceSet.getOutput().getResourcesDir(), "sonarqube.xml"));
                filterVersion(spec, inputs.getProperties().get(BuildUtil.VERSION).toString());
                return null;
            }
        });

        Set<File> pubLibs = CreateJarEclipseTask.getPublishedDependencyLibs(project, false);
        intoFrom("META-INF/lib", jarTask.getArchivePath());
        intoFrom("META-INF/lib", pubLibs);

        Manifest mafest = getManifest();
        final Attributes attrs = mafest.getAttributes();
        attrs.clear();
        attrs.put("Plugin-Name", inputs.getProperties().get("name"));
        attrs.put("Plugin-Base", "checkstyle");
        attrs.put("Plugin-Key", inputs.getProperties().get("sqPluginKey"));
        attrs.put("Implementation-Build", inputs.getProperties().get("gitHash"));
        attrs.put("Plugin-Description",
            inputs.getProperties().get("description") + " (based on Checkstyle " + pCheckstyleVersion + ")");
        attrs.put("Plugin-Version", inputs.getProperties().get(BuildUtil.VERSION));
        attrs.put("Plugin-Organization", inputs.getProperties().get("authorName"));
        attrs.put("Plugin-OrganizationUrl", inputs.getProperties().get("orgUrl"));
        attrs.put("Plugin-SourcesUrl", "https://github.com/checkstyle-addons/checkstyle-addons");
        attrs.put("Plugin-IssueTrackerUrl", inputs.getProperties().get("issueTrackerUrl"));
        attrs.put("Plugin-Class", "com.thomasjensen.checkstyle.addons.sonarqube.CheckstyleExtensionPlugin");
        attrs.put("Plugin-RequirePlugins", "java:" + theVersions.getSonarQubeMinJavaPluginVersion() //
            + ",checkstyle:" + theVersions.getSonarQubeMinCsPluginVersion());
        attrs.put("Plugin-Dependencies", "META-INF/lib/" + jarTask.getArchiveName() //
            + (pubLibs.size() > 0 ? " " : "") + CreateJarEclipseTask.flattenPrefixLibs("META-INF/lib/", pubLibs, ' '));
        attrs.put("Plugin-License", "GPLv3");
        attrs.put("Plugin-Homepage", inputs.getProperties().get("website"));
        //attrs.put("Plugin-TermsConditionsUrl", "");
        // TODO use same SonarQube version in manifest and compile dependencies
        attrs.put("Sonar-Version", theVersions.getSonarQubeMinPlatformVersion());
        attrs.putAll(CreateJarTask.mfAttrStd(project));
        attrs.remove("Website");

        doFirst(new Closure<Void>(this)
        {
            @Override
            public Void call()
            {
                final DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");  // required by SonarQube
                attrs.put("Plugin-BuildDate", sdf.format(BuildUtil.getExtraPropertyValue(project, "buildTimestamp")));
                return null;
            }
        });
    }
}
