package com.thomasjensen.checkstyle.addons.build;
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
import org.gradle.api.artifacts.VersionCatalog;
import org.gradle.api.artifacts.VersionCatalogsExtension;
import org.gradle.api.artifacts.VersionConstraint;
import org.gradle.api.file.FileCollection;


/**
 * Read all dependency configurations from 'project/dependencyConfigs' and make them available.
 */
public class DependencyConfigs
{
    /** name of the default dependency configuration */
    public static final String DEFAULT_DEPCONFIG_NAME = "default";

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
        depConfigDir = new File(pProject.getProjectDir(), "config/dependencyConfigs");
        final FileCollection listOfDepConfigFiles = readListOfDepConfigs(depConfigDir);
        depConfigs = readAllDependencyVersions(listOfDepConfigFiles);

        if (getDefault() == null || !DEFAULT_DEPCONFIG_NAME.equals(getDefault().getName())
            || !getDefault().isDefaultConfig()) {
            throw new GradleException("corrupt default dependency configuration");
        }
    }



    private FileCollection readListOfDepConfigs(@Nonnull final File pDepConfigDir)
    {
        File[] listOfFiles = pDepConfigDir.listFiles((dir, name) -> name.endsWith(".properties"));
        if (listOfFiles == null) {
            throw new GradleException("no dependency configurations found in dir: " + pDepConfigDir);
        }
        return project.files((Object[]) listOfFiles);
    }



    private DependencyConfig loadDependencyConfig(final File pDepConfigFile)
    {
        final Properties props = loadProperties(pDepConfigFile);

        final Set<String> compatibles = new HashSet<>();
        if (props.getProperty("CompatibleWithCheckstyle") != null) {
            compatibles.addAll(Arrays.asList(props.getProperty("CompatibleWithCheckstyle").split("\\s*,\\s*")));
        }
        final List<String> javadocLinks = new ArrayList<>();
        if (props.getProperty("JavadocLinks") != null) {
            javadocLinks.addAll(Arrays.asList(props.getProperty("JavadocLinks").split("\\s*,\\s*")));
        }
        final String name = getNameFromFile(pDepConfigFile);
        final Map<String, String> artifactVersions = getArtifactVersions(props, DEFAULT_DEPCONFIG_NAME.equals(name));

        DependencyConfig result = new DependencyConfig(name, compatibles,
            JavaVersion.toVersion(props.getProperty("JavaLevel")), javadocLinks,
            Boolean.parseBoolean(props.getProperty("SonarQubeSupport")),
            props.getProperty("SonarQubeMinPlatformVersion"), props.getProperty("SonarQubeMinCheckstylePlugin"),
            artifactVersions, DEFAULT_DEPCONFIG_NAME.equals(name), pDepConfigFile);
        return result;
    }



    private Properties loadProperties(final File pPropertyFile)
    {
        final Properties result = new Properties();

        FileInputStream fis = null;
        BufferedInputStream bis = null;
        try {
            fis = new FileInputStream(pPropertyFile);
            bis = new BufferedInputStream(fis);
            result.load(bis);
            return result;
        }
        catch (IOException e) {
            throw new GradleException("Error reading dependency configuration: " + pPropertyFile.getAbsolutePath(), e);
        }
        finally {
            buildUtil.closeQuietly(bis);
            buildUtil.closeQuietly(fis);
        }
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
        VersionCatalogsExtension catalogs = project.getExtensions().getByType(VersionCatalogsExtension.class);
        VersionCatalog deps = catalogs.named("checkstyleAddonsLibs");
        VersionConstraint vc = deps.findVersion("checkstyleBase").get();
        return vc.toString();
    }



    @Nonnull
    private String getNameFromFile(@Nonnull final File pDepConfigFile)
    {
        String result = null;
        int dotPos = pDepConfigFile.getName().lastIndexOf('.');
        if (dotPos > 0) {
            result = pDepConfigFile.getName().substring(0, dotPos);
        }
        if (result == null || "java".equalsIgnoreCase(result)) {
            throw new GradleException("Invalid dependency configuration file name: " + pDepConfigFile.getName());
        }
        return result;
    }



    private SortedMap<String, DependencyConfig> readAllDependencyVersions(final FileCollection pAllDepConfigs)
    {
        final SortedMap<String, DependencyConfig> result = new TreeMap<>();

        for (final File depCfgFile : pAllDepConfigs) {
            final DependencyConfig depConfig = loadDependencyConfig(depCfgFile);
            result.put(depConfig.getName(), depConfig);
        }
        return Collections.unmodifiableSortedMap(result);
    }



    @Nonnull
    public SortedMap<String, DependencyConfig> getAll()
    {
        return depConfigs;
    }



    @Nonnull
    public DependencyConfig getDefault()
    {
        return depConfigs.get(DEFAULT_DEPCONFIG_NAME);
    }
}
