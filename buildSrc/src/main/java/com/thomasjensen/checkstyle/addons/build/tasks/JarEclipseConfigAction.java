package com.thomasjensen.checkstyle.addons.build.tasks;
/*
 * Checkstyle-Addons - Additional Checkstyle checks
 * Copyright (c) 2015-2024, the Checkstyle Addons contributors
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
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.tools.ant.filters.ReplaceTokens;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.java.archives.Attributes;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskInputs;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.jvm.toolchain.JavaToolchainService;

import com.thomasjensen.checkstyle.addons.build.BuildConfigExtension;
import com.thomasjensen.checkstyle.addons.build.BuildUtil;
import com.thomasjensen.checkstyle.addons.build.ClasspathBuilder;
import com.thomasjensen.checkstyle.addons.build.DependencyConfig;
import com.thomasjensen.checkstyle.addons.build.TaskCreator;
import com.thomasjensen.checkstyle.addons.build.TaskNames;


/**
 * Gradle task configuration for the Eclipse plugin JAR creation task.
 */
public class JarEclipseConfigAction
    extends AbstractTaskConfigAction<Jar>
{
    public JarEclipseConfigAction(@Nonnull DependencyConfig pDepConfig,
        @Nonnull final JavaToolchainService pJavaToolchainService)
    {
        super(pDepConfig, pJavaToolchainService);
    }



    @Override
    protected void configureTaskFor(@Nonnull Jar pJarTask, @Nullable DependencyConfig pDepConfig)
    {
        Objects.requireNonNull(pDepConfig, "required dependency config not present");
        final BuildConfigExtension buildConfig = buildUtil.getBuildConfig();
        final String baseCsVersion = pDepConfig.getCheckstyleBaseVersion();
        final String myJavaLevel = pDepConfig.getJavaLevel().toString();
        pJarTask.setGroup(TaskCreator.ARTIFACTS_GROUP_NAME);

        pJarTask.setDescription("Assembles the Eclipse-CS plugin for dependency configuration '"
            + pDepConfig.getName() + "'");

        // adjust archive name
        if (!pDepConfig.isDefaultConfig()) {
            pJarTask.getArchiveBaseName().set(pJarTask.getArchiveBaseName().get() + '-' + pDepConfig.getName());
        }
        pJarTask.getArchiveClassifier().set("eclipse");

        // Dependency on 'classes' task (compile and resources)
        pJarTask.dependsOn(buildUtil.getTaskProvider(TaskNames.mainClasses, Task.class, pDepConfig));

        // Inputs for up-to-date checking
        final TaskInputs inputs = pJarTask.getInputs();
        inputs.property(BuildUtil.GROUP_ID, project.getGroup());
        inputs.property(BuildUtil.VERSION, project.getVersion());
        inputs.property("name", buildConfig.getLongName());
        inputs.property("authorName", buildConfig.getAuthorName());

        // Configuration of JAR file contents
        pJarTask.into("META-INF", copySpec -> copySpec.from("LICENSE"));

        final JavaCompile compileTask = buildUtil.getTask(TaskNames.compileJava, JavaCompile.class, pDepConfig);
        pJarTask.from(compileTask.getDestinationDirectory());

        final SourceSet mainSourceSet = buildUtil.getSourceSet(SourceSet.MAIN_SOURCE_SET_NAME);
        pJarTask.from(Objects.requireNonNull(mainSourceSet.getOutput().getResourcesDir()), copySpec -> {
            copySpec.exclude("**/*.html", "**/*.md");
            copySpec.rename(filename -> filename.replace("eclipsecs-plugin.xml", "plugin.xml"));
            copySpec.filter(versionReplacement(project.getVersion().toString()), ReplaceTokens.class);
        });

        final Set<File> pubLibs = getPublishedDependencyLibs(pJarTask, pDepConfig);
        pJarTask.into("lib", copySpec -> copySpec.from(pubLibs));

        final Attributes attrs = pJarTask.getManifest().getAttributes();
        attrs.clear();
        attrs.put("Bundle-ManifestVersion", "2");
        attrs.put("Bundle-Name",
            inputs.getProperties().get("name") + " Eclipse-CS Extension (based on Checkstyle " + baseCsVersion + ")");
        attrs.put("Bundle-SymbolicName", inputs.getProperties().get(BuildUtil.GROUP_ID) + ";singleton:=true");
        attrs.put("Bundle-Version", eclipsifyVersion(inputs.getProperties().get(BuildUtil.VERSION).toString()));
        attrs.put("Require-Bundle", "net.sf.eclipsecs.checkstyle," + "net.sf.eclipsecs.core," + "net.sf.eclipsecs.ui");
        attrs.put("Bundle-RequiredExecutionEnvironment", "JavaSE-" + myJavaLevel);
        attrs.put("Eclipse-LazyStart", "true");
        attrs.put("Bundle-Vendor", inputs.getProperties().get("authorName"));
        attrs.put("Import-Package", "org.eclipse.core.resources," + "org.eclipse.jdt.core.dom,"
            + "org.eclipse.jface.resource," + "org.eclipse.jface.text," + "org.eclipse.swt.graphics,"
            + "org.eclipse.ui");
        attrs.putAll(JarConfigAction.mfAttrStd(project));
        if (!pubLibs.isEmpty()) {
            attrs.put("Bundle-ClassPath", ".," + flattenPrefixLibs("lib/", pubLibs, ','));
        }
        buildUtil.addBuildTimestampDeferred(pJarTask);
    }



    /**
     * Modify our declared plugin version so that it conforms with OSGI version syntax:
     * <pre>    version ::= major('.'minor('.'micro('.'qualifier)?)?)?
     *    major ::= digit+
     *    minor ::= digit+
     *    micro ::= digit+
     *    qualifier ::= (alpha|digit|'_'|'-')+
     *    digit ::= [0..9]
     *    alpha ::= [a..zA..Z]</pre>
     * This is only necessary for snapshots. Releases always conform naturally.
     *
     * @param pVersion our normal version
     * @return the "eclipsified" version
     */
    private String eclipsifyVersion(@Nonnull final String pVersion)
    {
        int dashPos = pVersion.indexOf('-');
        if (dashPos >= 0) {
            return pVersion.substring(0, dashPos) + '.' + pVersion.substring(dashPos + 1);
        }
        return pVersion;
    }



    private Map<String, Object> versionReplacement(@Nonnull final String pVersion)
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
     * @return set of files
     */
    @Nonnull
    private Set<File> getPublishedDependencyLibs(@Nonnull final Task pTask, @Nonnull final DependencyConfig pDepConfig)
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



    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    static boolean isCheckstyle(final ResolvedDependency pDependency)
    {
        return pDependency != null && DependencyConfig.CHECKSTYLE_GROUPID.equals(pDependency.getModuleGroup())
            && "checkstyle".equals(pDependency.getModuleName());
    }



    @SuppressWarnings("SameParameterValue")
    private String flattenPrefixLibs(final String pPrefix, final Set<File> pLibs, final char pSeparator)
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
