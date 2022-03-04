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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

import org.apache.tools.ant.filters.ReplaceTokens;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.java.archives.Attributes;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskInputs;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.compile.JavaCompile;

import com.thomasjensen.checkstyle.addons.build.BuildConfigExtension;
import com.thomasjensen.checkstyle.addons.build.BuildUtil;
import com.thomasjensen.checkstyle.addons.build.ClasspathBuilder;
import com.thomasjensen.checkstyle.addons.build.DependencyConfig;
import com.thomasjensen.checkstyle.addons.build.TaskCreator;
import com.thomasjensen.checkstyle.addons.build.TaskNames;


/**
 * Gradle task to create the Eclipse plugin.
 */
public class JarEclipseTaskConfigurer
    implements ConfigurableAddonsTask
{
    private final Jar jarTask;



    public JarEclipseTaskConfigurer(@Nonnull final Jar pJarTask)
    {
        super();
        jarTask = pJarTask;
    }



    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public void configureFor(@Nonnull final DependencyConfig pDepConfig)
    {
        final Project project = jarTask.getProject();
        final BuildUtil buildUtil = new BuildUtil(project);
        final BuildConfigExtension buildConfig = buildUtil.getBuildConfig();
        final String baseCsVersion = pDepConfig.getCheckstyleBaseVersion();
        final String myJavaLevel = pDepConfig.getJavaLevel().toString();

        jarTask.setGroup(TaskCreator.ARTIFACTS_GROUP_NAME);
        jarTask.setDescription("Assembles the Eclipse-CS plugin for dependency configuration '" + pDepConfig.getName()
            + "'");

        // adjust archive name
        String appendix = "eclipse";
        if (!pDepConfig.isDefaultConfig()) {
            appendix = pDepConfig.getName() + '-' + appendix;
        }
        jarTask.getArchiveAppendix().set(appendix);

        // Dependency on 'classes' task (compile and resources)
        jarTask.dependsOn(buildUtil.getTaskProvider(TaskNames.mainClasses, Task.class, pDepConfig));

        // Inputs for up-to-date checking
        final TaskInputs inputs = jarTask.getInputs();
        inputs.property(BuildUtil.GROUP_ID, project.getGroup());
        inputs.property(BuildUtil.VERSION, project.getVersion());
        inputs.property("name", buildConfig.getLongName());
        inputs.property("authorName", buildConfig.getAuthorName());

        // Configuration of JAR file contents
        jarTask.into("META-INF", copySpec -> copySpec.from("LICENSE"));

        final JavaCompile compileTask = buildUtil.getTask(TaskNames.compileJava, JavaCompile.class, pDepConfig);
        jarTask.from(compileTask.getDestinationDirectory());

        final SourceSet mainSourceSet = buildUtil.getSourceSet(SourceSet.MAIN_SOURCE_SET_NAME);
        jarTask.from(mainSourceSet.getOutput().getResourcesDir(), copySpec -> {
            copySpec.exclude("**/*.html", "**/*.md");
            copySpec.rename(filename -> filename.replace("eclipsecs-plugin.xml", "plugin.xml"));
            copySpec.filter(versionReplacement(project.getVersion().toString()), ReplaceTokens.class);
        });

        final Set<File> pubLibs = getPublishedDependencyLibs(jarTask, pDepConfig);
        jarTask.into("lib", copySpec -> copySpec.from(pubLibs));

        final Attributes attrs = jarTask.getManifest().getAttributes();
        attrs.clear();
        attrs.put("Bundle-ManifestVersion", "2");
        attrs.put("Bundle-Name", inputs.getProperties().get("name")
            + " Eclipse-CS Extension (based on Checkstyle " + baseCsVersion + ")");
        attrs.put("Bundle-SymbolicName", inputs.getProperties().get(BuildUtil.GROUP_ID) + ";singleton:=true");
        attrs.put("Bundle-Version", inputs.getProperties().get(BuildUtil.VERSION));
        attrs.put("Require-Bundle", "net.sf.eclipsecs.checkstyle,"
            + "net.sf.eclipsecs.core,"
            + "net.sf.eclipsecs.ui");
        attrs.put("Bundle-RequiredExecutionEnvironment", "JavaSE-" + myJavaLevel);
        attrs.put("Eclipse-LazyStart", "true");
        attrs.put("Bundle-Vendor", inputs.getProperties().get("authorName"));
        attrs.put("Import-Package", "org.eclipse.core.resources,"
            + "org.eclipse.jdt.core.dom,"
            + "org.eclipse.jface.resource,"
            + "org.eclipse.jface.text,"
            + "org.eclipse.swt.graphics,"
            + "org.eclipse.ui");
        attrs.putAll(JarTaskConfigurer.mfAttrStd(project));
        if (!pubLibs.isEmpty()) {
            attrs.put("Bundle-ClassPath", ".," + flattenPrefixLibs("lib/", pubLibs, ','));
        }
        buildUtil.addBuildTimestampDeferred(jarTask);
    }



    static Map<String, Object> versionReplacement(@Nonnull final String pVersion)
    {
        Map<String, String> placeHolders = new HashMap<>();
        placeHolders.put("version", pVersion);
        Map<String, Object> result = new HashMap<>();
        result.put("tokens", placeHolders);
        return result;
    }



    /**
     * Scan the dependencies of the specified configurations and return a list of File objects for each dependency.
     * Resolves the configurations if they are still unresolved.
     *
     * @param pTask the calling task
     * @param pDepConfig the current dependency configuration
     * @return list of files
     */
    @Nonnull
    public static Set<File> getPublishedDependencyLibs(@Nonnull final Task pTask,
        @Nonnull final DependencyConfig pDepConfig)
    {
        Set<File> result = new HashSet<>();
        Configuration cfg = new ClasspathBuilder(pTask.getProject()).buildMainRuntimeConfiguration(pDepConfig);
        for (ResolvedDependency dep : cfg.getResolvedConfiguration().getFirstLevelModuleDependencies()) {
            if (!isCheckstyle(dep)) {
                for (ResolvedArtifact artifact : dep.getAllModuleArtifacts()) {
                    result.add(artifact.getFile());
                }
            }
        }
        return result;
    }



    static boolean isCheckstyle(final ResolvedDependency pDependency)
    {
        return pDependency != null && DependencyConfig.CHECKSTYLE_GROUPID.equals(pDependency.getModuleGroup())
            && "checkstyle".equals(pDependency.getModuleName());
    }



    public static String flattenPrefixLibs(final String pPrefix, final Set<File> pLibs, final char pSeparator)
    {
        Set<String> set = new HashSet<>();
        final String prefix = pPrefix.endsWith("/") ? pPrefix : (pPrefix + "/");
        for (final File f : pLibs) {
            set.add(prefix + f.getName());
        }
        StringBuilder sb = new StringBuilder();
        for (final Iterator<String> iter = set.iterator(); iter.hasNext(); ) {
            sb.append(iter.next());
            if (iter.hasNext()) {
                sb.append(pSeparator);
            }
        }
        return sb.toString();
    }

}
