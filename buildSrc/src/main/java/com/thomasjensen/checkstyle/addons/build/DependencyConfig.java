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

import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.gradle.api.JavaVersion;

import net.jcip.annotations.Immutable;


/**
 * Represents a dependency configuration. A dependency configuration is the major structuring entity of a Checkstyle
 * Addons build. For each dependency configuration, Gradle tasks are created and artifacts published. It has a base
 * Checkstyle version against which sources are compiled. The compiled classes are expected (and tested) to be
 * compatible with a number of other Checkstyle runtimes, whose versions are listed in the dependency configuration.
 * Each dependency configuration may also modify some of the third party artifact versions that Checkstyle Addons
 * depends on.
 */
@Immutable
public final class DependencyConfig
{
    /** Maven group ID of the Checkstyle artifacts */
    public static final String CHECKSTYLE_GROUPID = "com.puppycrawl.tools";

    private final String name;

    private final SortedSet<String> compatibleCheckstyleVersions;

    private final JavaVersion javaLevel;

    private final List<String> javadocLinks;

    private final boolean sonarQubeSupport;

    private final String sonarQubeMinPlatformVersion;

    private final String sonarQubeMinCsPluginVersion;

    private final Map<String, String> artifactVersions;

    private final boolean defaultConfig;

    private final File configFile;



    /**
     * Constructor.
     *
     * @param pName the publication suffix
     * @param pCompatibleCheckstyleVersions the Checkstyle versions to which we must be compatible
     * @param pJavaLevel the Java level
     * @param pJavadocLinks list of URLs to be passed to Javadoc for linking to external apidocs
     * @param pSonarQubeSupport flag if SonarQube is supported
     * @param pSonarQubeMinPlatformVersion minimum SonarQube platform version (for use in manifest)
     * @param pSonarQubeMinCsPluginVersion minimum SonarQube Checkstyle plugin version
     * @param pArtifactVersions versions of certain artifact dependencies
     * @param pDefaultConfig flag if this is the default dependency configuration
     * @param pConfigFile the dependency config file
     */
    public DependencyConfig(@Nonnull final String pName, @Nonnull final Set<String> pCompatibleCheckstyleVersions,
        @Nonnull final JavaVersion pJavaLevel, @Nonnull final List<String> pJavadocLinks,
        final boolean pSonarQubeSupport, @Nullable final String pSonarQubeMinPlatformVersion,
        @Nullable final String pSonarQubeMinCsPluginVersion, @Nonnull final Map<String, String> pArtifactVersions,
        final boolean pDefaultConfig, @Nonnull final File pConfigFile)
    {
        if (pName == null) {
            throw new IllegalArgumentException("pName is null");
        }
        if (pJavaLevel == null) {
            throw new IllegalArgumentException("pJavaLevel is null");
        }
        if (pArtifactVersions == null) {
            throw new IllegalArgumentException("pArtifactVersions is null");
        }
        if (pJavadocLinks == null) {
            throw new IllegalArgumentException("pJavadocLinks is null");
        }
        if (pConfigFile == null) {
            throw new IllegalArgumentException("pConfigFile is null");
        }
        if (pArtifactVersions.get(CHECKSTYLE_GROUPID) == null) {
            throw new IllegalArgumentException("Checkstyle version is null");
        }

        name = pName;
        SortedSet<String> ccv = new TreeSet<>(new VersionComparator());
        ccv.addAll(pCompatibleCheckstyleVersions);
        compatibleCheckstyleVersions = Collections.unmodifiableSortedSet(ccv);
        javaLevel = pJavaLevel;
        javadocLinks = Collections.unmodifiableList(pJavadocLinks);
        sonarQubeSupport = pSonarQubeSupport;
        sonarQubeMinPlatformVersion = pSonarQubeMinPlatformVersion;
        sonarQubeMinCsPluginVersion = pSonarQubeMinCsPluginVersion;
        artifactVersions = pArtifactVersions;
        defaultConfig = pDefaultConfig;
        configFile = pConfigFile;
    }



    /**
     * Getter.
     *
     * @return the name of the dependency configuration, also the name without extension of the configuration file, and
     * the suffix of the corresponding publication name
     */
    @Nonnull
    public String getName()
    {
        return name;
    }



    /**
     * Getter.
     *
     * @return the version of Checkstyle against which this dependency configuration is built. Example: {@code "6.12.1"}
     */
    @Nonnull
    public String getCheckstyleBaseVersion()
    {
        return artifactVersions.get(CHECKSTYLE_GROUPID);
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
     * @return The minimum required runtime version of the SonarQube Checkstyle plugin. This will be declared in the
     * manifest.
     */
    @CheckForNull
    public String getSonarQubeMinCsPluginVersion()
    {
        return sonarQubeMinCsPluginVersion;
    }



    @Nonnull
    public Map<String, String> getArtifactVersions()
    {
        return artifactVersions;
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
        sb.append("name='").append(name).append('\'');
        sb.append(", defaultConfig=").append(defaultConfig);
        sb.append(", javaLevel=").append(javaLevel);
        sb.append(", artifactVersions=");
        if (artifactVersions != null) {
            sb.append('{');
            for (Iterator<Map.Entry<String, String>> iter = artifactVersions.entrySet().iterator(); iter.hasNext();) {
                final Map.Entry<String, String> entry = iter.next();
                sb.append(entry.getKey());
                sb.append("->'").append(entry.getValue()).append('\'');
                if (iter.hasNext()) {
                    sb.append(", ");
                }
            }
            sb.append('}');
        }
        else {
            sb.append("null");
        }
        sb.append(", compatibleCheckstyleVersions=").append(compatibleCheckstyleVersions);
        sb.append(", sonarQubeSupport=").append(sonarQubeSupport);
        sb.append(", sonarQubeMinPlatformVersion='").append(sonarQubeMinPlatformVersion).append('\'');
        sb.append(", sonarQubeMinCsPluginVersion='").append(sonarQubeMinCsPluginVersion).append('\'');
        sb.append(", configFile=").append(configFile);
        sb.append(", javadocLinks=").append(javadocLinks);
        sb.append('}');
        return sb.toString();
    }
}
