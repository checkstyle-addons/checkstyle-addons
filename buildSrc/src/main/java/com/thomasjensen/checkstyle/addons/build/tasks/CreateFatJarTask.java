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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;

import com.github.jengelman.gradle.plugins.shadow.internal.DependencyFilter;
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.bundling.Jar;

import com.thomasjensen.checkstyle.addons.build.BuildUtil;
import com.thomasjensen.checkstyle.addons.build.ClasspathBuilder;
import com.thomasjensen.checkstyle.addons.build.DependencyConfig;
import com.thomasjensen.checkstyle.addons.build.TaskCreator;
import com.thomasjensen.checkstyle.addons.build.TaskNames;


/**
 * Gradle task to create a Checkstyle Addons FatJar.
 */
public class CreateFatJarTask
    extends ShadowJar
    implements ConfigurableAddonsTask
{
    private final BuildUtil buildUtil;



    public CreateFatJarTask()
    {
        super();
        final Project project = getProject();
        buildUtil = new BuildUtil(project);
        setGroup(TaskCreator.ARTIFACTS_GROUP_NAME);
        getArchiveClassifier().set("all");
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



    @Override
    public void configureFor(@Nonnull final DependencyConfig pDepConfig)
    {
        // set appendix for archive name
        final String appendix = pDepConfig.getName();
        if (!pDepConfig.isDefaultConfig()) {
            getArchiveAppendix().set(appendix);
        }

        // dependency on the corresponding (thin) Jar task
        final Jar thinJarTask = (Jar) buildUtil.getTask(TaskNames.jar, pDepConfig);
        dependsOn(thinJarTask);

        setDescription("Create a combined JAR of project and runtime dependencies of '"
            + SourceSet.MAIN_SOURCE_SET_NAME + "' for dependency configuration '" + pDepConfig.getName() + "'");

        getManifest().inheritFrom(thinJarTask.getManifest());

        from(thinJarTask.getArchiveFile().get().getAsFile());
        Configuration cfg = new ClasspathBuilder(getProject()).buildMainRuntimeConfiguration(pDepConfig);
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
