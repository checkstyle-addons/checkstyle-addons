package com.thomasjensen.checkstyle.addons.build;
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;

import org.gradle.api.GradleException;
import org.gradle.api.JavaVersion;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.file.FileCollection;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.JavaPlugin;


/**
 * Read all dependency configurations from 'project/dependencyConfigs' and make them available.
 */
public class DependencyConfigs
{
    /** name of the default dependency configuration */
    public static final String DEFAULT_NAME = "default";

    private final Project project;

    private final BuildUtil buildUtil;

    private final File depConfigDir;

    /**
     * map from dependency configuration names (e.g. {@code "default"}, or {@code "java7"}) to their corresponding
     * dependency configurations
     */
    private final SortedMap<String, DependencyConfig> depConfigs;



    public DependencyConfigs(@Nonnull final Project pProject)
    {
        project = pProject;
        buildUtil = new BuildUtil(pProject);
        depConfigDir = new File(pProject.getProjectDir(), "project/dependencyConfigs");
        final FileCollection listOfDepConfigFiles = readListOfDepConfigs(depConfigDir);
        depConfigs = readAllDependencyVersions(listOfDepConfigFiles);

        if (getDefault() == null || !DEFAULT_NAME.equals(getDefault().getName()) || !getDefault().isDefaultConfig()) {
            throw new GradleException("corrupt default dependency configuration");
        }

        final Logger log = pProject.getLogger();
        log.lifecycle("Default Checkstyle version: " + getDefault().getCheckstyleBaseVersion());
        log.lifecycle("Active dependency configurations:");
        for (Map.Entry<String, DependencyConfig> entry : depConfigs.entrySet()) {
            DependencyConfig depConfig = entry.getValue();
            log.lifecycle("  - " + entry.getKey() + ": Checkstyle " + depConfig.getCheckstyleBaseVersion() //
                + ", Java " + depConfig.getJavaLevel() //
                + ", compatible: " + depConfig.getCompatibleCheckstyleVersions());
        }
    }



    private FileCollection readListOfDepConfigs(@Nonnull final File pDepConfigDir)
    {
        File[] listOfFiles = pDepConfigDir.listFiles(new PropertyFileFilter());
        if (listOfFiles == null) {
            throw new GradleException("no dependency configurations found in dir: " + pDepConfigDir);
        }
        return project.files((Object[]) listOfFiles);
    }



    private DependencyConfig loadDependencyConfig(final File pDepConfigFile)
    {
        final Properties props = new Properties();

        FileInputStream fis = null;
        BufferedInputStream bis = null;
        try {
            fis = new FileInputStream(pDepConfigFile);
            bis = new BufferedInputStream(fis);
            props.load(bis);
        }
        catch (IOException e) {
            throw new GradleException("Error reading dependency configuration: " + pDepConfigFile.getAbsolutePath(), e);
        }
        finally {
            buildUtil.closeQuietly(bis);
            buildUtil.closeQuietly(fis);
        }

        final Set<String> compatibles = new HashSet<>();
        if (props.getProperty("CompatibleWithCheckstyle") != null) {
            compatibles.addAll(Arrays.asList(props.getProperty("CompatibleWithCheckstyle").split("\\s*,\\s*")));
        }
        final List<String> javadocLinks = new ArrayList<>();
        if (props.getProperty("JavadocLinks") != null) {
            javadocLinks.addAll(Arrays.asList(props.getProperty("JavadocLinks").split("\\s*,\\s*")));
        }
        final String name = getNameFromFile(pDepConfigFile);
        final Map<String, String> artifactVersions = getArtifactVersions(props, DEFAULT_NAME.equals(name));

        DependencyConfig result = new DependencyConfig(name, compatibles,
            JavaVersion.toVersion(props.getProperty("JavaLevel")), javadocLinks,
            Boolean.parseBoolean(props.getProperty("SonarQubeSupport")),
            props.getProperty("SonarQubeMinPlatformVersion"), props.getProperty("SonarQubeMinCheckstylePlugin"),
            artifactVersions, DEFAULT_NAME.equals(name), pDepConfigFile);
        return result;
    }



    @Nonnull
    private Map<String, String> getArtifactVersions(@Nonnull final Properties pProps, final boolean pIsDefault)
    {
        final String prefix = "dependencyVersion.";
        final Map<String, String> result = new HashMap<>();
        for (final String key : pProps.stringPropertyNames()) {
            if (key.length() > prefix.length() && key.startsWith(prefix)) {
                final String groupId = key.substring(prefix.length());
                final String version = pProps.getProperty(key);
                if (version == null) {
                    throw new GradleException("Invalid entry in dependency configuration: " + key + "=null");
                }
                result.put(groupId, version);
            }
        }
        if (pIsDefault && !result.containsKey(DependencyConfig.CHECKSTYLE_GROUPID)) {
            result.put(DependencyConfig.CHECKSTYLE_GROUPID, getDefaultCheckstyleVersion());
        }
        return Collections.unmodifiableMap(result);
    }



    private String getDefaultCheckstyleVersion()
    {
        String result = null;
        final Configuration apiConfig = project.getConfigurations().getByName(JavaPlugin.API_CONFIGURATION_NAME);
        for (final Dependency dependency : apiConfig.getAllDependencies()) {
            if (DependencyConfig.CHECKSTYLE_GROUPID.equals(dependency.getGroup()) && "checkstyle".equals(
                dependency.getName())) {
                result = dependency.getVersion();
                break;
            }
        }
        if (result == null) {
            throw new GradleException("Checkstyle dependency not found in build script");
        }
        return result;
    }



    @Nonnull
    private String getNameFromFile(@Nonnull final File pDepConfigFile)
    {
        String result = null;
        int dotPos = pDepConfigFile.getName().lastIndexOf('.');
        if (dotPos > 0) {
            result = pDepConfigFile.getName().substring(0, dotPos);
        }
        if (result == null || result.length() == 0 || "java".equalsIgnoreCase(result)) {
            throw new GradleException("Invalid dependency configuration file name: " + pDepConfigFile.getName());
        }
        return result;
    }



    private SortedMap<String, DependencyConfig> readAllDependencyVersions(final FileCollection pAllDepConfigs)
    {
        final SortedMap<String, DependencyConfig> result = new TreeMap<>();
        final JavaLevelUtil javaLevelUtil = new JavaLevelUtil(project);

        for (final File depCfgFile : pAllDepConfigs) {
            final DependencyConfig depConfig = loadDependencyConfig(depCfgFile);

            final JavaVersion myJavaLevel = depConfig.getJavaLevel();
            if (myJavaLevel.isJava7() && !javaLevelUtil.java7Configured()) {
                project.getLogger().warn(
                    "WARNING: Skipping dependency configuration file '" + depConfig.getConfigFile().getName()
                        + "' because of missing JDK" + myJavaLevel.getMajorVersion() + " compiler configuration.");
            }
            else {
                result.put(depConfig.getName(), depConfig);
            }
        }
        return Collections.unmodifiableSortedMap(result);
    }



    /**
     * Prints all dependency configurations with full contents for debugging purposes.
     */
    @SuppressWarnings("unused")
    public void printAll()
    {
        project.getLogger().lifecycle("Full contents of dependency configurations:");
        project.getLogger().lifecycle("-------------------------------------------");
        for (final Map.Entry<String, DependencyConfig> entry : depConfigs.entrySet()) {
            project.getLogger().lifecycle("- " + entry.getKey() + ":\t" + entry.getValue());
        }
    }



    @Nonnull
    public SortedMap<String, DependencyConfig> getAll()
    {
        return depConfigs;
    }



    @Nonnull
    public Map<String, DependencyConfig> getPublications()
    {
        final Map<String, DependencyConfig> result = new HashMap<>();
        for (final DependencyConfig depConfig : depConfigs.values()) {
            String pubName = buildUtil.getExtraPropertyValue(ExtProp.DefaultPublication);
            if (!depConfig.isDefaultConfig()) {
                pubName += '-' + depConfig.getName();
            }
            result.put(pubName, depConfig);
        }
        return Collections.unmodifiableMap(result);
    }



    @Nonnull
    public DependencyConfig getDefault()
    {
        return depConfigs.get(DEFAULT_NAME);
    }
}
