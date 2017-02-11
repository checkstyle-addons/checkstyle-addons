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

import java.io.Closeable;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtraPropertiesExtension;


/**
 * Utility class for use by the build.
 *
 * @author Thomas Jensen
 */
public final class BuildUtil
{
    /** the Maven metadata name {@code "artifactId"} */
    public static final String ARTIFACT_ID = "artifactId";

    /** the Maven metadata name {@code "groupId"} */
    public static final String GROUP_ID = "groupId";

    /** the Maven metadata name {@code "version"} */
    public static final String VERSION = "version";

    private final Project project;



    /**
     * Constructor.
     *
     * @param pProject the Gradle project
     */
    public BuildUtil(@Nonnull final Project pProject)
    {
        super();
        project = pProject;
    }



    /**
     * Close the closeable while ignoring any {@code IOException}s this may throw.
     *
     * @param pCloseable the closeable to close
     */
    public void closeQuietly(@Nullable final Closeable pCloseable)
    {
        if (pCloseable != null) {
            try {
                pCloseable.close();
            }
            catch (IOException e) {
                // ignore
            }
        }
    }



    /**
     * Get the value of the project or system property whose name is set in the project's extra property {@code
     * "jdk6PropName"}. If the project property exists, it has precedence over the system property.
     *
     * @return the absolute path to the javac executable for Java 6
     */
    public String getJdk6Compiler()
    {
        return getProjectPropertyValue(ExtProp.Jdk6PropName);
    }



    /**
     * Get the value of the project or system property whose name is set in the project's extra property {@code
     * "javadoc6PropName"}. If the project property exists, it has precedence over the system property.
     *
     * @return the absolute path to the javadoc executable for Java 6
     */
    public String getJdk6Javadoc()
    {
        return getProjectPropertyValue(ExtProp.Javadoc6PropName);
    }



    /**
     * Get the value of the project or system property whose name is set in the project's extra property {@code
     * "jdk7PropName"}. If the project property exists, it has precedence over the system property.
     *
     * @return the absolute path to the javac executable for Java 7
     */
    public String getJdk7Compiler()
    {
        return getProjectPropertyValue(ExtProp.Jdk7PropName);
    }



    /**
     * Getter.
     *
     * @return the name factory
     */
    public NameFactory getNameFactory()
    {
        return getExtraPropertyValue(ExtProp.NameFactory);
    }



    /**
     * Retrieve the dep configs from the project's extra properties.
     *
     * @return the dependency configuration container
     */
    public DependencyConfigs getDepConfigs()
    {
        return getExtraPropertyValue(ExtProp.DepConfigs);
    }



    /**
     * Retrieve the longName from the project's extra properties.
     *
     * @return the long name of this software
     */
    public String getLongName()
    {
        return getExtraPropertyValue(ExtProp.LongName);
    }



    /**
     * Get the value of the project or system property whose name is set in the project's extra property {@code
     * "javadoc7PropName"}. If the project property exists, it has precedence over the system property.
     *
     * @return the absolute path to the javadoc executable for Java 7
     */
    public String getJdk7Javadoc()
    {
        return getProjectPropertyValue(ExtProp.Javadoc7PropName);
    }



    private String getProjectPropertyValue(@Nonnull final ExtProp pExtraPropNameRef)
    {
        final String propName = getExtraPropertyValue(pExtraPropNameRef);
        String result = System.getenv(propName);
        if (project.hasProperty(propName)) {
            result = (String) project.property(propName);
        }
        return result;
    }



    /**
     * Read the value of an extra property of the project.
     *
     * @param pExtraPropName the reference to the extra property name
     * @param <T> type of the property value
     * @return the property's value
     */
    @SuppressWarnings("unchecked")
    public <T> T getExtraPropertyValue(@Nonnull final ExtProp pExtraPropName)
    {
        ExtraPropertiesExtension extraProps = project.getExtensions().getByType(ExtraPropertiesExtension.class);
        if (extraProps.has(pExtraPropName.getPropertyName())) {
            return (T) extraProps.get(pExtraPropName.getPropertyName());
        }
        throw new GradleException(
            "Reference to non-existent project extra property '" + pExtraPropName.getPropertyName() + "'");
    }
}
