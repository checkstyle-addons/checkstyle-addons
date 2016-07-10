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

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.jcip.annotations.Immutable;
import org.gradle.api.JavaVersion;


/**
 * Represents a dependency configuration. A dependency configuration can be <i>published</i>, which means that it
 * directly comes from a dependency configuration file, and artifacts will be created for it. Or, it can be
 * <i>ancillary</i>, which means it is derived from a published dependency configuration and exists for the purpose of
 * compatibility testing.
 *
 * @author Thomas Jensen
 */
@Immutable
public final class DependencyConfig
{
    private final String checkstyleBaseVersion;

    private final SortedSet<String> compatibleCheckstyleVersions;

    private final JavaVersion javaLevel;

    private final String findBugsVersion;

    private final List<String> javadocLinks;

    private final boolean sonarQubeSupport;

    private final String sonarQubeApiVersion;

    private final String sonarQubeMinPlatformVersion;

    private final String sonarQubeMinJavaPluginVersion;

    private final String sonarQubeMinCsPluginVersion;

    private final String sonarQubeSlf4jNopVersion;

    private final boolean defaultConfig;

    private final boolean published;

    private final String publicationSuffix;

    private final File configFile;



    /**
     * Constructor for a <i>published</i> dependency configuration.
     *
     * @param pCheckstyleBaseVersion the Checkstyle version
     * @param pCompatibleCheckstyleVersions the Checkstyle versions to which we must be compatible
     * @param pJavaLevel the Java level
     * @param pFindBugsVersion the FindBugs version
     * @param pJavadocLinks list of URLs to be passed to Javadoc for linking to external apidocs
     * @param pSonarQubeSupport flag if SonarQube is supported
     * @param pSonarQubeApiVersion SonarQube API version (for use in dependencies)
     * @param pSonarQubeMinPlatformVersion minimum SonarQube platform version (for use in manifest)
     * @param pSonarQubeMinJavaPluginVersion minimum SonarQube Java plugin version
     * @param pSonarQubeMinCsPluginVersion minimum SonarQube Checkstyle plugin version
     * @param pSonarQubeSlf4jNopVersion SLF4J Nop Binding version
     * @param pDefaultConfig flag if this is the default dependency configuration
     * @param pPublicationSuffix the publication suffix
     * @param pConfigFile the dependency config file
     */
    public DependencyConfig(@Nonnull final String pCheckstyleBaseVersion,
        @Nonnull final Set<String> pCompatibleCheckstyleVersions, @Nonnull final JavaVersion pJavaLevel,
        @Nonnull final String pFindBugsVersion, @Nonnull final List<String> pJavadocLinks,
        final boolean pSonarQubeSupport, @Nullable final String pSonarQubeApiVersion,
        @Nullable final String pSonarQubeMinPlatformVersion, @Nullable final String pSonarQubeMinJavaPluginVersion,
        @Nullable final String pSonarQubeMinCsPluginVersion, @Nullable final String pSonarQubeSlf4jNopVersion,
        final boolean pDefaultConfig, @Nonnull final String pPublicationSuffix, @Nonnull final File pConfigFile)
    {
        if (pCheckstyleBaseVersion == null) {
            throw new IllegalArgumentException("pCheckstyleBaseVersion is null");
        }
        if (pJavaLevel == null) {
            throw new IllegalArgumentException("pJavaLevel is null");
        }
        if (pFindBugsVersion == null) {
            throw new IllegalArgumentException("pFindBugsVersion is null");
        }
        if (pJavadocLinks == null) {
            throw new IllegalArgumentException("pJavadocLinks is null");
        }
        if (pConfigFile == null) {
            throw new IllegalArgumentException("pConfigFile is null");
        }

        SortedSet<String> ccv = new TreeSet<String>(new VersionComparator());
        ccv.addAll(pCompatibleCheckstyleVersions);
        compatibleCheckstyleVersions = Collections.unmodifiableSortedSet(ccv);
        checkstyleBaseVersion = pCheckstyleBaseVersion;
        javaLevel = pJavaLevel;
        findBugsVersion = pFindBugsVersion;
        javadocLinks = Collections.unmodifiableList(pJavadocLinks);
        sonarQubeSupport = pSonarQubeSupport;
        sonarQubeApiVersion = pSonarQubeApiVersion;
        sonarQubeMinPlatformVersion = pSonarQubeMinPlatformVersion;
        sonarQubeMinJavaPluginVersion = pSonarQubeMinJavaPluginVersion;
        sonarQubeMinCsPluginVersion = pSonarQubeMinCsPluginVersion;
        sonarQubeSlf4jNopVersion = pSonarQubeSlf4jNopVersion;
        defaultConfig = pDefaultConfig;
        published = true;
        publicationSuffix = pPublicationSuffix;
        configFile = pConfigFile;
    }



    /**
     * Constructor for an <i>ancillary</i> dependency configuration.
     *
     * @param pPublishedDepConfig the published dependency configuration upon which this ancillary dependency
     * configuration is based
     * @param pCheckstyleBaseVersion the Checkstyle version to be used in this ancillary dependency configuration
     */
    public DependencyConfig(@Nonnull final DependencyConfig pPublishedDepConfig,
        @Nonnull final String pCheckstyleBaseVersion)
    {
        compatibleCheckstyleVersions = Collections.unmodifiableSortedSet(new TreeSet<String>(new VersionComparator()));
        checkstyleBaseVersion = pCheckstyleBaseVersion;
        javaLevel = pPublishedDepConfig.getJavaLevel();
        findBugsVersion = pPublishedDepConfig.getFindBugsVersion();
        javadocLinks = pPublishedDepConfig.getJavadocLinks();
        sonarQubeSupport = false;
        sonarQubeApiVersion = null;
        sonarQubeMinPlatformVersion = null;
        sonarQubeMinJavaPluginVersion = null;
        sonarQubeMinCsPluginVersion = null;
        sonarQubeSlf4jNopVersion = null;
        defaultConfig = false;
        published = false;
        publicationSuffix = null;
        configFile = pPublishedDepConfig.getConfigFile();
    }



    /**
     * Getter.
     *
     * @return the version of Checkstyle against which this dependency configuration is built. This is the version that
     * Checkstyle classes have in the IDE. Example: {@code "6.12.1"}
     */
    @Nonnull
    public String getCheckstyleBaseVersion()
    {
        return checkstyleBaseVersion;
    }



    /**
     * Getter.
     *
     * @return Comma-separated list of Checkstyle versions that are supposed to be compatible with the artifact produced
     * with this dependency configuration. These compatibility relationships are tested during the build.
     */
    @Nonnull
    public SortedSet<String> getCompatibleCheckstyleVersions()
    {
        return compatibleCheckstyleVersions;
    }



    /**
     * Getter.
     *
     * @return The Java level required by the Checkstyle version given in {@link #CheckstyleBase}, for example {@code
     * 1.7}.
     */
    @Nonnull
    public JavaVersion getJavaLevel()
    {
        return javaLevel;
    }



    /**
     * Getter.
     *
     * @return The version of the FindBugs annotation JARs to use. Depends on the Java level.
     */
    @Nonnull
    public String getFindBugsVersion()
    {
        return findBugsVersion;
    }



    /**
     * Getter.
     *
     * @return list of URLs to be passed to Javadoc for linking to external apidocs
     */
    public List<String> getJavadocLinks()
    {
        return javadocLinks;
    }



    /**
     * Getter.
     *
     * @return Flag indicating if the dependency configuration supports SonarQube
     */
    public boolean isSonarQubeSupported()
    {
        return sonarQubeSupport;
    }



    /**
     * Getter.
     *
     * @return The version of the SonarQube API to use in this dependency configuration. This should really be merged
     * with {@link #SonarQubeMinPlatformVersion}, but well, it's more work than it sounds.
     */
    @CheckForNull
    public String getSonarQubeApiVersion()
    {
        return sonarQubeApiVersion;
    }



    /**
     * Getter.
     *
     * @return The minimum SonarQube platform version required at runtime. This will be declared in the manifest.
     */
    @CheckForNull
    public String getSonarQubeMinPlatformVersion()
    {
        return sonarQubeMinPlatformVersion;
    }



    /**
     * Getter.
     *
     * @return The minimum required runtime version of the SonarQube Java plugin. This will be declared in the manifest.
     */
    @CheckForNull
    public String getSonarQubeMinJavaPluginVersion()
    {
        return sonarQubeMinJavaPluginVersion;
    }



    /**
     * Getter.
     *
     * @return The minimum required runtime version of the SonarQube Checkstyle plugin. This will be declared in the
     * manifest.
     */
    @CheckForNull
    public String getSonarQubeMinCsPluginVersion()
    {
        return sonarQubeMinCsPluginVersion;
    }



    /**
     * Getter.
     *
     * @return In order to avoid some warnings during test execution, we must add an SLF4J mapping to the classpath.
     * This property specifies the version of the slf4j-nop mapping to use.
     */
    @Nullable
    public String getSonarQubeSlf4jNopVersion()
    {
        return sonarQubeSlf4jNopVersion;
    }



    /**
     * Getter.
     *
     * @return Flag indicating if this dependency configuration is the default dependency configuration.
     */
    public boolean isDefaultConfig()
    {
        return defaultConfig;
    }



    /**
     * Getter.
     *
     * @return Flag indicating if this dependency configuration is published (i.e. a file) or virtual (i.e. for
     * compatibility checks).
     */
    public boolean isPublished()
    {
        return published;
    }



    /**
     * Getter.
     *
     * @return The suffix of the publication, directly derived from the file name. For the default publication, this is
     * <code>null</code>.
     */
    @CheckForNull
    public String getPublicationSuffix()
    {
        return publicationSuffix;
    }



    /**
     * Getter.
     *
     * @return A {@link java.io.File} object pointing to the dependency configuration file upon which this dependency
     * configuration is based.
     */
    @Nonnull
    public File getConfigFile()
    {
        return configFile;
    }



    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("DependencyConfig{");
        sb.append("checkstyleBaseVersion='").append(checkstyleBaseVersion).append('\'');
        sb.append(", compatibleCheckstyleVersions=").append(compatibleCheckstyleVersions);
        sb.append(", javaLevel=").append(javaLevel);
        sb.append(", findBugsVersion='").append(findBugsVersion).append('\'');
        sb.append(", javadocLinks=").append(javadocLinks);
        sb.append(", sonarQubeSupport=").append(sonarQubeSupport);
        sb.append(", sonarQubeApiVersion='").append(sonarQubeApiVersion).append('\'');
        sb.append(", sonarQubeMinPlatformVersion='").append(sonarQubeMinPlatformVersion).append('\'');
        sb.append(", sonarQubeMinJavaPluginVersion='").append(sonarQubeMinJavaPluginVersion).append('\'');
        sb.append(", sonarQubeMinCsPluginVersion='").append(sonarQubeMinCsPluginVersion).append('\'');
        sb.append(", sonarQubeSlf4jNopVersion='").append(sonarQubeSlf4jNopVersion).append('\'');
        sb.append(", defaultConfig=").append(defaultConfig);
        sb.append(", published=").append(published);
        sb.append(", publicationSuffix='").append(publicationSuffix).append('\'');
        sb.append(", configFile=").append(configFile);
        sb.append('}');
        return sb.toString();
    }
}
