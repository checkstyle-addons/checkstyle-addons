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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.annotation.Nonnull;

import groovy.lang.Closure;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.file.CopySpec;
import org.gradle.api.java.archives.Attributes;
import org.gradle.api.java.archives.Manifest;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskInputs;
import org.gradle.api.tasks.compile.JavaCompile;

import com.thomasjensen.checkstyle.addons.build.BuildUtil;
import com.thomasjensen.checkstyle.addons.build.ClasspathBuilder;
import com.thomasjensen.checkstyle.addons.build.DependencyConfig;
import com.thomasjensen.checkstyle.addons.build.ExtProp;
import com.thomasjensen.checkstyle.addons.build.TaskNames;


/**
 * Gradle task to create the Eclipse plugin.
 */
public class CreateJarEclipseTask
    extends AbstractAddonsJarTask
{
    /**
     * Constructor.
     */
    public CreateJarEclipseTask()
    {
        super();
        getArchiveAppendix().set("eclipse");
    }



    static boolean isCheckstyle(final ResolvedDependency pDependency)
    {
        return pDependency != null && "com.puppycrawl.tools".equals(pDependency.getModuleGroup()) && "checkstyle"
            .equals(pDependency.getModuleName());
    }



    /**
     * Scan the dependencies of the specified configurations and return a list of File objects for each dependency.
     * Resolves the configurations if they are still unresolved.
     *
     * @param pTask the calling task
     * @param pDepConfig the current dependency configuration
     * @return list of files
     */
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



    public static String flattenPrefixLibs(final String pPrefix, final Set<File> pLibs, final char pSeparator)
    {
        Set<String> set = new HashSet<>();
        final String prefix = pPrefix.endsWith("/") ? pPrefix : (pPrefix + "/");
        for (final File f : pLibs) {
            set.add(prefix + f.getName());
        }
        StringBuilder sb = new StringBuilder();
        for (final Iterator<String> iter = set.iterator(); iter.hasNext();) {
            sb.append(iter.next());
            if (iter.hasNext()) {
                sb.append(pSeparator);
            }
        }
        return sb.toString();
    }



    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public void configureFor(@Nonnull final DependencyConfig pDepConfig)
    {
        final Project project = getProject();
        final String baseCsVersion = pDepConfig.getCheckstyleBaseVersion();
        final String myJavaLevel = pDepConfig.getJavaLevel().toString();

        setDescription("Assembles the Eclipse-CS plugin for dependency configuration '" + pDepConfig.getName() + "'");

        // adjust archive name
        if (!pDepConfig.isDefaultConfig()) {
            final String pubSuffix = pDepConfig.getName();
            getArchiveAppendix().set(pubSuffix + '-' + getArchiveAppendix().get());
        }

        // Dependency on 'classes' task (compile and resources)
        dependsOn(TaskNames.mainClasses.getName(pDepConfig));

        // Inputs for up-to-date checking
        final TaskInputs inputs = getInputs();
        inputs.property(BuildUtil.GROUP_ID, project.getGroup());
        inputs.property(BuildUtil.VERSION, project.getVersion());
        inputs.property("name", getBuildUtil().getLongName());
        inputs.property("authorName", getBuildUtil().getExtraPropertyValue(ExtProp.AuthorName));

        // Configuration of JAR file contents
        intoFrom("META-INF", "LICENSE");

        final JavaCompile compileTask = (JavaCompile) getBuildUtil().getTask(TaskNames.compileJava, pDepConfig);
        from(compileTask.getDestinationDir());

        final SourceSet mainSourceSet = getBuildUtil().getSourceSet(SourceSet.MAIN_SOURCE_SET_NAME);
        from(mainSourceSet.getOutput().getResourcesDir(), new Closure<Void>(this)
        {
            @Override
            public Void call(final Object... pArgs)
            {
                final CopySpec spec = (CopySpec) getDelegate();
                spec.exclude("**/*.html", "**/*.md");
                spec.rename(new Closure<String>(this)
                {
                    @Override
                    public String call(final Object... pArgs)
                    {
                        final String fileName = pArgs != null && pArgs.length > 0 ? ((String) pArgs[0]) : null;
                        if (fileName != null) {
                            return fileName.replace("eclipsecs-plugin.xml", "plugin.xml");
                        }
                        else {
                            return null;
                        }
                    }
                });
                filterVersion(spec, inputs.getProperties().get(BuildUtil.VERSION).toString());
                return null;
            }
        });

        final Set<File> pubLibs = getPublishedDependencyLibs(this, pDepConfig);
        intoFrom("lib", pubLibs);

        Manifest mafest = getManifest();
        final Attributes attrs = mafest.getAttributes();
        attrs.clear();
        attrs.put("Bundle-ManifestVersion", "2");
        attrs.put("Bundle-Name", inputs.getProperties().get("name") //
            + " Eclipse-CS Extension (based on Checkstyle " + baseCsVersion + ")");
        attrs.put("Bundle-SymbolicName", inputs.getProperties().get(BuildUtil.GROUP_ID) + ";singleton:=true");
        attrs.put("Bundle-Version", inputs.getProperties().get(BuildUtil.VERSION));
        attrs.put("Require-Bundle", "net.sf.eclipsecs.checkstyle," + "net.sf.eclipsecs.core," + "net.sf.eclipsecs.ui");
        attrs.put("Bundle-RequiredExecutionEnvironment", "JavaSE-" + myJavaLevel);
        attrs.put("Eclipse-LazyStart", "true");
        attrs.put("Bundle-Vendor", inputs.getProperties().get("authorName"));
        attrs.put("Import-Package",
            "org.eclipse.core.resources," + "org.eclipse.jdt.core.dom," + "org.eclipse.jface.resource,"
                + "org.eclipse.jface.text," + "org.eclipse.swt.graphics," + "org.eclipse.ui");
        attrs.putAll(CreateJarTask.mfAttrStd(project));
        if (pubLibs != null && pubLibs.size() > 0) {
            attrs.put("Bundle-ClassPath", ".," + flattenPrefixLibs("lib/", pubLibs, ','));
        }
        getBuildUtil().addBuildTimestampDeferred(this, attrs);
    }
}
