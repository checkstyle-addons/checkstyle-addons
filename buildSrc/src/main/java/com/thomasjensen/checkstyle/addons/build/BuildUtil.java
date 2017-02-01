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



    private BuildUtil()
    {
        super();
    }



    /**
     * Close the closeable while ignoring any {@code IOException}s this may throw.
     *
     * @param pCloseable the closeable to close
     */
    public static void closeQuietly(@Nullable final Closeable pCloseable)
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
     * @param pProject the project object
     * @return the absolute path to the javac executable for Java 6
     */
    public static String getJdk6Compiler(@Nonnull final Project pProject)
    {
        return getPropertyValue(pProject, "jdk6PropName");
    }



    /**
     * Get the value of the project or system property whose name is set in the project's extra property {@code
     * "javadoc6PropName"}. If the project property exists, it has precedence over the system property.
     *
     * @param pProject the project object
     * @return the absolute path to the javadoc executable for Java 6
     */
    public static String getJdk6Javadoc(@Nonnull final Project pProject)
    {
        return getPropertyValue(pProject, "javadoc6PropName");
    }



    /**
     * Get the value of the project or system property whose name is set in the project's extra property {@code
     * "jdk7PropName"}. If the project property exists, it has precedence over the system property.
     *
     * @param pProject the project object
     * @return the absolute path to the javac executable for Java 7
     */
    public static String getJdk7Compiler(@Nonnull final Project pProject)
    {
        return getPropertyValue(pProject, "jdk7PropName");
    }



    /**
     * Get the value of the project or system property whose name is set in the project's extra property {@code
     * "javadoc7PropName"}. If the project property exists, it has precedence over the system property.
     *
     * @param pProject the project object
     * @return the absolute path to the javadoc executable for Java 7
     */
    public static String getJdk7Javadoc(@Nonnull final Project pProject)
    {
        return getPropertyValue(pProject, "javadoc7PropName");
    }



    private static String getPropertyValue(@Nonnull final Project pProject, @Nonnull final String pExtraPropNameRef)
    {
        final String propName = getExtraPropertyValue(pProject, pExtraPropNameRef);
        String result = System.getenv(propName);
        if (pProject.hasProperty(propName)) {
            result = (String) pProject.property(propName);
        }
        return result;
    }



    /**
     * Read the value of an extra property of the given project.
     *
     * @param pProject the project
     * @param pExtraPropName the name of the extra property as defined in the build script
     * @param <T> type of the property value
     * @return the property's value
     */
    @SuppressWarnings("unchecked")
    public static <T> T getExtraPropertyValue(@Nonnull final Project pProject, @Nonnull final String pExtraPropName)
    {
        // TODO transform into method of common super task, use enum instead of string
        ExtraPropertiesExtension extraProps = pProject.getExtensions().getByType(ExtraPropertiesExtension.class);
        if (extraProps.has(pExtraPropName)) {
            return (T) extraProps.get(pExtraPropName);
        }
        throw new GradleException("Reference to non-existing project extra property '" + pExtraPropName + "'");
    }
}
