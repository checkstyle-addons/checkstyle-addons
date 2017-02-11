package com.thomasjensen.checkstyle.addons.build;
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
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.gradle.api.GradleException;
import org.gradle.api.JavaVersion;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.file.collections.SimpleFileCollection;
import org.gradle.api.plugins.ExtraPropertiesExtension;


/**
 * Read all dependency configurations from 'project/dependencyConfigs' and make them available.
 *
 * @author Thomas Jensen
 */
public class DependencyConfigs
{
    private static final String DEFAULT_PUBL_SUFFIX = "default";

    private final Project project;

    private final BuildUtil buildUtil;

    private final File depConfigDir;

    private final FileCollection listOfDepConfigFiles;

    /**
     * map from dependency configuration names (which is the same as a Checkstyle version) to their corresponding
     * configuration maps
     */
    private final SortedMap<String, DependencyConfig> depVersions;

    /**
     * map from publication name (e.g. "checkstyleAddons-java6") to configuration maps for the corresponding dependency
     * configurations
     */
    private final Map<String, DependencyConfig> publications;

    /** the list of all known dependency configurations (published and ancillary) */
    private final Set<String> activeDepConfigs;



    /**
     * Constructor.
     *
     * @param pProject the project
     */
    public DependencyConfigs(@Nonnull final Project pProject)
    {
        project = pProject;
        buildUtil = new BuildUtil(pProject);
        depConfigDir = new File(pProject.getProjectDir(), "project/dependencyConfigs");
        listOfDepConfigFiles = readListOfDepConfigs(depConfigDir);
        depVersions = readAllDependencyVersions(listOfDepConfigFiles);
        publications = getPublicationList(depVersions);
        activeDepConfigs = Collections.unmodifiableSet(depVersions.keySet());

        String defaultCsVersion = null;
        for (Map.Entry<String, DependencyConfig> entry : depVersions.entrySet()) {
            if (entry.getValue().isDefaultConfig()) {
                defaultCsVersion = entry.getKey();
                ExtraPropertiesExtension extProps = pProject.getExtensions().getByType(ExtraPropertiesExtension.class);
                extProps.set("versionDefaults", entry.getValue());
                break;
            }
        }
        if (defaultCsVersion == null) {
            throw new GradleException("Broken dependency configuration - default Checkstyle version unknown");
        }

        pProject.getLogger().lifecycle(
            "Dependency configurations found: " + activeDepConfigs + "; default: " + defaultCsVersion);
    }



    private FileCollection readListOfDepConfigs(@Nonnull final File pDepConfigDir)
    {
        File[] listOfFiles = pDepConfigDir.listFiles(new PropertyFileFilter());
        return new SimpleFileCollection(listOfFiles);
    }



    private DependencyConfig readPublishedDependencyConfig(final File pDepConfig)
    {
        final Properties props = new Properties();

        FileInputStream fis = null;
        BufferedInputStream bis = null;
        try {
            fis = new FileInputStream(pDepConfig);
            bis = new BufferedInputStream(fis);
            props.load(bis);
        }
        catch (IOException e) {
            throw new GradleException("Unable to read dependency configuration: " + pDepConfig.getAbsolutePath(), e);
        }
        finally {
            buildUtil.closeQuietly(bis);
            buildUtil.closeQuietly(fis);
        }

        final Set<String> compatibles = new HashSet<String>();
        if (props.getProperty("CompatibleWithCheckstyle") != null) {
            compatibles.addAll(Arrays.asList(props.getProperty("CompatibleWithCheckstyle").split("\\s*,\\s*")));
        }

        final List<String> javadocLinks = new ArrayList<String>();
        if (props.getProperty("JavadocLinks") != null) {
            javadocLinks.addAll(Arrays.asList(props.getProperty("JavadocLinks").split("\\s*,\\s*")));
        }
        final String publicationSuffix = publicationSuffixFromFile(pDepConfig);

        DependencyConfig result = new DependencyConfig(props.getProperty("CheckstyleBase"), compatibles,
            JavaVersion.toVersion(props.getProperty("JavaLevel")), props.getProperty("FindBugsVersion"), javadocLinks,
            Boolean.parseBoolean(props.getProperty("SonarQubeSupport")), props.getProperty("SonarQubeApiVersion"),
            props.getProperty("SonarQubeMinPlatformVersion"), props.getProperty("SonarQubeMinJavaPlugin"),
            props.getProperty("SonarQubeMinCheckstylePlugin"), props.getProperty("SonarQubeSlf4jNopVersion"),
            DEFAULT_PUBL_SUFFIX.equals(publicationSuffix), publicationSuffix, pDepConfig);
        return result;
    }



    @CheckForNull
    private String publicationSuffixFromFile(@Nonnull final File pDepConfigFile)
    {
        String result = null;
        int dotPos = pDepConfigFile.getName().lastIndexOf('.');
        if (dotPos > 0) {
            result = pDepConfigFile.getName().substring(0, dotPos);
        }
        if (result == null || result.length() == 0) {
            throw new GradleException("Invalid dependency configuration file name: " + pDepConfigFile.getName());
        }
        return result;
    }



    @SuppressWarnings("unchecked")
    private SortedMap<String, DependencyConfig> readAllDependencyVersions(final FileCollection pAllDepConfigs)
    {
        final SortedMap<String, DependencyConfig> result = new TreeMap<String, DependencyConfig>(
            new VersionComparator());

        for (final File depCfgFile : pAllDepConfigs) {
            final DependencyConfig publishedDepConfig = readPublishedDependencyConfig(depCfgFile);

            final JavaVersion myJavaLevel = publishedDepConfig.getJavaLevel();
            if ((JavaVersion.VERSION_1_6 == myJavaLevel && buildUtil.getJdk6Compiler() == null) || (
                JavaVersion.VERSION_1_7 == myJavaLevel && buildUtil.getJdk7Compiler() == null)) {
                final String javacPropName =
                    JavaVersion.VERSION_1_6 == myJavaLevel ? buildUtil.<String>getExtraPropertyValue(
                        ExtProp.Jdk6PropName) : buildUtil.<String>getExtraPropertyValue(ExtProp.Jdk7PropName);
                project.getLogger().warn(
                    "WARNING: Skipping dependency configuration file '" + publishedDepConfig.getConfigFile().getName()
                        + "' because of missing JDK" + myJavaLevel.getMajorVersion() + " compiler configuration.");
                project.getLogger().warn(
                    "Property '" + javacPropName + "' not defined in gradle.properties. " + "It must point to a Java "
                        + myJavaLevel.getMajorVersion() + " compiler executable.");
            }
            else {
                result.put(publishedDepConfig.getCheckstyleBaseVersion(), publishedDepConfig);
            }

            // Create ancillary dependency configurations for compatible versions of Checkstyle
            for (String compatibleVersion : (Set<String>) publishedDepConfig.getCompatibleCheckstyleVersions()) {
                if (result.containsKey(compatibleVersion)) {
                    throw new GradleException("Checkstyle version '" + compatibleVersion + "' defined twice");
                }
                result.put(compatibleVersion, new DependencyConfig(publishedDepConfig, compatibleVersion));
            }
        }
        return Collections.unmodifiableSortedMap(result);
    }



    private Map<String, DependencyConfig> getPublicationList(
        @Nonnull final SortedMap<String, DependencyConfig> pDepVersions)
    {
        final Map<String, DependencyConfig> result = new HashMap<String, DependencyConfig>();
        for (final DependencyConfig depConfig : pDepVersions.values()) {
            if (depConfig.isPublished()) {
                String pubName = buildUtil.getExtraPropertyValue(ExtProp.DefaultPublication);
                if (!depConfig.isDefaultConfig()) {
                    pubName += '-' + depConfig.getPublicationSuffix();
                }
                result.put(pubName, depConfig);
            }
        }
        return Collections.unmodifiableMap(result);
    }



    /**
     * Prints all dependency configurations with full contents for debugging purposes.
     */
    public void printAll()
    {
        project.getLogger().lifecycle("Full contents of dependency configurations:");
        project.getLogger().lifecycle("-------------------------------------------");
        for (final Map.Entry<String, DependencyConfig> entry : depVersions.entrySet()) {
            project.getLogger().lifecycle("- " + entry.getKey() + ":\t" + entry.getValue());
        }
    }



    @Nonnull
    public Set<String> getActiveDepConfigs()
    {
        return activeDepConfigs;
    }



    @Nonnull
    public FileCollection getListOfDepConfigFiles()
    {
        return listOfDepConfigFiles;
    }



    @Nonnull
    public File getDepConfigDir()
    {
        return depConfigDir;
    }



    @Nonnull
    public SortedMap<String, DependencyConfig> getDepVersions()
    {
        return depVersions;
    }



    /**
     * Get one particular dependency configuration.
     *
     * @param pCheckstyleVersion the Checkstyle version, e.g. {@code "6.12.1"}
     * @return the dependency configuration for the given Checkstyle version
     */
    @Nonnull
    public DependencyConfig getDepConfig(@Nonnull final String pCheckstyleVersion)
    {
        if (depVersions.containsKey(pCheckstyleVersion)) {
            return depVersions.get(pCheckstyleVersion);
        }
        throw new GradleException("Unknown dependency configuration - " + pCheckstyleVersion);
    }



    @Nonnull
    public Map<String, DependencyConfig> getPublications()
    {
        return publications;
    }



    /**
     * Convenience method for checking SonarQube support of a given Checkstyle version.
     *
     * @param pCheckstyleVersion the Checkstyle version, e.g. {@code "6.12.1"}
     * @return the flag as given in the dependency configuration
     */
    public boolean supportsSonarQube(@Nonnull final String pCheckstyleVersion)
    {
        return depVersions.get(pCheckstyleVersion).isSonarQubeSupported();
    }



    /**
     * Convenience method for checking if the given Checkstyle version is the base version in one of the dependency
     * configuration files, so that we must create artifacts for it.
     *
     * @param pCheckstyleVersion the Checkstyle version, e.g. {@code "6.12.1"}
     * @return the flag as given in the dependency configuration
     */
    public boolean isPublished(@Nonnull final String pCheckstyleVersion)
    {
        return depVersions.get(pCheckstyleVersion).isPublished();
    }



    /**
     * Convenience method for checking if the given Checkstyle version is the <i>default</i> version, which means that
     * it is used in the IDE.
     *
     * @param pCheckstyleVersion the Checkstyle version, e.g. {@code "6.12.1"}
     * @return the flag as given in the dependency configuration
     */
    public boolean isDefault(@Nonnull final String pCheckstyleVersion)
    {
        return depVersions.get(pCheckstyleVersion).isDefaultConfig();
    }
}
