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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.github.jengelman.gradle.plugins.shadow.internal.DependencyFilter;
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.tasks.bundling.Jar;

import com.thomasjensen.checkstyle.addons.build.BuildUtil;
import com.thomasjensen.checkstyle.addons.build.DependencyConfigs;
import com.thomasjensen.checkstyle.addons.build.NameFactory;
import com.thomasjensen.checkstyle.addons.build.SourceSetNames;
import com.thomasjensen.checkstyle.addons.build.TaskNames;


/**
 * Gradle task to create a Checkstyle Addons FatJar.
 *
 * @author Thomas Jensen
 */
public class CreateFatJarTask
    extends ShadowJar
{
    /**
     * Constructor.
     */
    public CreateFatJarTask()
    {
        super();
        final String longName = BuildUtil.getExtraPropertyValue(getProject(), "longName");
        setDescription(longName + ": Create a combined JAR of project and runtime dependencies of '");
        setClassifier("all");
    }



    protected static Set<String> getNonCheckstyleDeps(final Configuration pConfiguration)
    {
        Set<String> result = new HashSet<>();
        for (ResolvedDependency dep : pConfiguration.getResolvedConfiguration().getFirstLevelModuleDependencies()) {
            if (!CreateJarEclipseTask.isCheckstyle(dep)) {
                result.add(dep.getName());
                for (final ResolvedDependency rd : dep.getChildren()) {
                    result.add(rd.getName());
                }
            }
        }
        return result;
    }



    /**
     * Configure this task instance for a given dependency configuration.
     *
     * @param pCheckstyleVersion the Checkstyle version for which to configure
     */
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

        // dependency on the corresponding (thin) Jar task
        final Jar thinJarTask = (Jar) nameFactory.getTask(TaskNames.jar, pCheckstyleVersion);
        dependsOn(thinJarTask);

        setDescription(getDescription() + nameFactory.getName(SourceSetNames.main, pCheckstyleVersion) + "'.");

        getManifest().inheritFrom(thinJarTask.getManifest());

        from(thinJarTask.getArchivePath());
        Configuration cfg = project.getConfigurations().getByName(
            nameFactory.getSourceSet(SourceSetNames.main, pCheckstyleVersion).getRuntimeConfigurationName());
        setConfigurations(Collections.singletonList(cfg));
        exclude("META-INF/INDEX.LIST", "META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "META-INF/maven/**/*",
            "pom.xml");
        final Set<String> deps = getNonCheckstyleDeps(cfg);
        if (deps.size() > 0) {
            append("META-INF/LICENSE");  // append all licenses found in dependent Jars into one
            dependencies((final DependencyFilter pDependencyFilter) -> {
                for (String dep : deps) {
                    pDependencyFilter.include(pDependencyFilter.dependency(dep));
                }
            });
        }
    }
}
