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

import javax.annotation.Nonnull;

import org.gradle.api.GradleException;
import org.gradle.api.JavaVersion;
import org.gradle.api.Project;
import org.gradle.internal.jvm.Jvm;


/**
 * Handles property based configuration of older JDKs.
 *
 * @author Thomas Jensen
 */
public class JavaLevelUtil
{
    /** Name of the system / project property pointing to the VM executable for Java&nbsp;6 */
    public static final String JAVA6_VM_EXEC_PROPNAME = "checkstyleaddons_jdk6_java";

    /** Name of the system / project property pointing to the Javadoc executable for Java&nbsp;6 */
    public static final String JAVA6_JAVADOC_EXEC_PROPNAME = "checkstyleaddons_jdk6_javadoc";

    /** Name of the system / project property pointing to the compiler executable for Java&nbsp;6 */
    public static final String JAVA6_COMPILER_EXEC_PROPNAME = "checkstyleaddons_jdk6_javac";

    /** Name of the system / project property pointing to the VM executable for Java&nbsp;7 */
    public static final String JAVA7_VM_EXEC_PROPNAME = "checkstyleaddons_jdk7_java";

    /** Name of the system / project property pointing to the Javadoc executable for Java&nbsp;7 */
    public static final String JAVA7_JAVADOC_EXEC_PROPNAME = "checkstyleaddons_jdk7_javadoc";

    /** Name of the system / project property pointing to the compiler executable for Java&nbsp;7 */
    public static final String JAVA7_COMPILER_EXEC_PROPNAME = "checkstyleaddons_jdk7_javac";

    private final Project project;



    public JavaLevelUtil(final Project pProject)
    {
        project = pProject;
    }



    @Nonnull
    private String getPropertyValue(@Nonnull final String pPropName)
    {
        String result = System.getenv(pPropName);
        if (project.hasProperty(pPropName)) {
            result = (String) project.property(pPropName);
        }
        if (result == null) {
            throw new GradleException("Required system or project property not found: " + pPropName);
        }
        return result;
    }



    private boolean isPropertyPresent(@Nonnull final String pPropName)
    {
        return System.getenv(pPropName) != null || project.hasProperty(pPropName);
    }



    public boolean java6Configured()
    {
        return isPropertyPresent(JAVA6_VM_EXEC_PROPNAME) && isPropertyPresent(JAVA6_JAVADOC_EXEC_PROPNAME)
            && isPropertyPresent(JAVA6_COMPILER_EXEC_PROPNAME);
    }



    public boolean java7Configured()
    {
        return isPropertyPresent(JAVA7_VM_EXEC_PROPNAME) && isPropertyPresent(JAVA7_JAVADOC_EXEC_PROPNAME)
            && isPropertyPresent(JAVA7_COMPILER_EXEC_PROPNAME);
    }



    public boolean isOlderSupportedJava(@Nonnull final JavaVersion pJavaLevel)
    {
        return pJavaLevel.isJava6() || pJavaLevel.isJava7();
    }



    private void assertKnownJavaLevel(@Nonnull final JavaVersion pJavaLevel)
    {
        if (!isOlderSupportedJava(pJavaLevel)) {
            throw new GradleException("Unsupported Java level " + pJavaLevel);
        }
    }



    @Nonnull
    public String getCompilerExecutable(@Nonnull final JavaVersion pJavaLevel)
    {
        assertKnownJavaLevel(pJavaLevel);
        return getPropertyValue(
            pJavaLevel == JavaVersion.VERSION_1_6 ? JAVA6_COMPILER_EXEC_PROPNAME : JAVA7_COMPILER_EXEC_PROPNAME);
    }



    @Nonnull
    public String getJavadocExecutable(@Nonnull final JavaVersion pJavaLevel)
    {
        assertKnownJavaLevel(pJavaLevel);
        return getPropertyValue(
            pJavaLevel == JavaVersion.VERSION_1_6 ? JAVA6_JAVADOC_EXEC_PROPNAME : JAVA7_JAVADOC_EXEC_PROPNAME);
    }



    @Nonnull
    public String getJvmExecutable(@Nonnull final JavaVersion pJavaLevel)
    {
        assertKnownJavaLevel(pJavaLevel);
        return getPropertyValue(
            pJavaLevel == JavaVersion.VERSION_1_6 ? JAVA6_VM_EXEC_PROPNAME : JAVA7_VM_EXEC_PROPNAME);
    }



    public void analyzeJavaLevels()
    {
        final JavaVersion currentJava = Jvm.current().getJavaVersion();
        if (currentJava != JavaVersion.VERSION_1_8) {
            if (currentJava.isJava9Compatible()) {
                project.getLogger().warn(
                    "This project must be built with Java 8. You are using a newer version of Java (" + currentJava
                        + "), which will lead to undefined build results.");
            }
            else {
                throw new GradleException("Outdated Java version " + currentJava + ". Use at least Java 8.");
            }
        }
        if (!java6Configured()) {
            project.getLogger().warn(
                "WARNING: The properties for Java 6 support are not configured as system properties or"
                    + " in your gradle.properties. Artifacts for Java 6 support will not be created.");
        }
        if (!java7Configured()) {
            project.getLogger().warn(
                "WARNING: The properties for Java 7 support are not configured as system properties or"
                    + " in your gradle.properties. Artifacts for Java 7 support will not be created.");
        }
    }
}
