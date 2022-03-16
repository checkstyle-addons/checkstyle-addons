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

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.jengelman.gradle.plugins.shadow.internal.DependencyFilter;
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.bundling.Jar;

import com.thomasjensen.checkstyle.addons.build.ClasspathBuilder;
import com.thomasjensen.checkstyle.addons.build.DependencyConfig;
import com.thomasjensen.checkstyle.addons.build.TaskCreator;
import com.thomasjensen.checkstyle.addons.build.TaskNames;


/**
 * Configure a ShadowJar task to create a Checkstyle Addons FatJar.
 */
public class FatJarConfigAction
    extends AbstractTaskConfigAction<ShadowJar>
{
    public FatJarConfigAction(@Nonnull DependencyConfig pDepConfig)
    {
        super(pDepConfig);
    }



    @Override
    protected void configureTaskFor(@Nonnull ShadowJar pFatJarTask, @Nullable DependencyConfig pDepConfig)
    {
        Objects.requireNonNull(pDepConfig, "required dependency config not present");
        pFatJarTask.setGroup(TaskCreator.ARTIFACTS_GROUP_NAME);
        pFatJarTask.getArchiveClassifier().set("all");

        // set appendix for archive name
        final String appendix = pDepConfig.getName();
        if (!pDepConfig.isDefaultConfig()) {
            pFatJarTask.getArchiveAppendix().set(appendix);
        }

        // dependency on the corresponding (thin) Jar task
        final Jar thinJarTask = buildUtil.getTask(TaskNames.jar, Jar.class, pDepConfig);
        pFatJarTask.dependsOn(thinJarTask);

        pFatJarTask.setDescription("Create a combined JAR of project and runtime dependencies of '"
            + SourceSet.MAIN_SOURCE_SET_NAME + "' for dependency configuration '" + pDepConfig.getName() + "'");

        buildUtil.inheritManifest(pFatJarTask, pDepConfig);

        pFatJarTask.from(thinJarTask.getArchiveFile());
        Configuration cfg = new ClasspathBuilder(project).buildMainRuntimeConfiguration(pDepConfig);
        pFatJarTask.setConfigurations(Collections.singletonList(cfg));
        pFatJarTask.exclude("META-INF/INDEX.LIST", "META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA",
            "META-INF/maven/**/*", "pom.xml");
        final Set<String> deps = getNonCheckstyleDeps(cfg);
        if (deps.size() > 0) {
            pFatJarTask.append("META-INF/LICENSE");  // append all licenses found in dependent Jars into one
            pFatJarTask.dependencies((final DependencyFilter pDependencyFilter) -> {
                for (String dep : deps) {
                    pDependencyFilter.include(pDependencyFilter.dependency(dep));
                }
            });
        }
    }



    private Set<String> getNonCheckstyleDeps(final Configuration pConfiguration)
    {
        Set<String> result = new HashSet<>();
        for (ResolvedDependency dep : pConfiguration.getResolvedConfiguration().getFirstLevelModuleDependencies()) {
            if (!JarEclipseConfigAction.isCheckstyle(dep)) {
                result.add(dep.getName());
                for (final ResolvedDependency rd : dep.getChildren()) {
                    result.add(rd.getName());
                }
            }
        }
        return result;
    }
}
