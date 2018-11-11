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

import java.text.MessageFormat;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;


/**
 * All tasks for which we create dependency configuration specific variants in Checkstyle Addons, along with factory
 * methods for concrete task names.
 */
public enum TaskNames
{
    /** <code>assemble</code> / <code>assemble{0}</code> */
    assemble(true, "assemble{0}"),

    /** <code>compileJava</code> / <code>compileMain{0}</code> */
    compileJava(false, "compileMain{0}"),

    /** <code>compileSonarqubeJava</code> / <code>compileSonarqube{0}</code> */
    compileSonarqubeJava(false, "compileSonarqube{0}"),

    /** <code>compileTestJava</code> / <code>compileTest{0}</code> */
    compileTestJava(false, "compileTest{0}"),

    /** <code>generatePomProperties</code> / <code>generatePomProperties{0}</code> */
    generatePomProperties(true, "generatePomProperties{0}"),

    /** <code>generatePom</code> / <code>generatePom{0}</code> */
    generatePom(false, "generatePom{0}"),

    /** <code>fatjar</code> / <code>fatjar{0}</code> */
    fatJar(true, "fatjar{0}"),

    /** <code>jar</code> / <code>jar{0}</code> */
    jar(true, "jar{0}"),

    /** <code>jarSources</code> / <code>jarSources{0}</code> */
    jarSources(true, "jarSources{0}"),

    /** <code>jarJavadoc</code> / <code>jarJavadoc{0}</code> */
    jarJavadoc(true, "jarJavadoc{0}"),

    /** <code>jarEclipse</code> / <code>jarEclipse{0}</code> */
    jarEclipse(true, "jarEclipse{0}"),

    /** <code>jarSonarqube</code> / <code>jarSonarqube{0}</code> */
    jarSonarqube(true, "jarSonarqube{0}"),

    /** <code>javadoc</code> / <code>javadoc{0}</code> */
    javadoc(false, "javadoc{0}"),

    /** <code>classes</code> / <code>main{0}Classes</code> */
    mainClasses(false, "main{0}Classes"),    // should be called 'classes', but can't because of Groovy

    /** <code>sonarqubeClasses</code> / <code>sonarqube{0}Classes</code> */
    sonarqubeClasses(false, "sonarqube{0}Classes"),

    /** <code>testClasses</code> / <code>test{0}Classes</code> */
    testClasses(false, "test{0}Classes"),

    /** <code>test</code> / <code>test{0}</code> */
    test(false, "test{0}"),

    /** <code>xtest</code> / <code>xtest{0}against{1}</code> */
    xtest(true, "xtest{0}against{1}");

    //

    private static final Pattern DOT_PATTERN = Pattern.compile(Pattern.quote("."));

    private final boolean mUseVersionForDefault;

    private final MessageFormat nameWithVersionTemplate;



    private TaskNames(final boolean pUseVersionForDefault, final String pNameWithVersionTemplate)
    {
        mUseVersionForDefault = pUseVersionForDefault;
        nameWithVersionTemplate = new MessageFormat(pNameWithVersionTemplate);
    }



    /**
     * Getter.
     *
     * @return the name of the entity without any dependency configuration name or Checkstyle version added to it,
     * usually the Enum constant name
     */
    private String getNameWithoutVersion()
    {
        if (this == mainClasses) {
            return "classes";
        }
        return name();
    }



    /**
     * Determine the name of the entity including a Checkstyle version number.
     *
     * @param pVersionParam the dependency configuration name
     * @return the name of the entity including a dependency configuration name, built from its MessageFormat template
     */
    private String getNameWithVersion(final String pVersionParam)
    {
        final int paramNum = nameWithVersionTemplate.getFormats().length;
        if (paramNum == 1) {
            return nameWithVersionTemplate.format(new String[]{pVersionParam});
        }
        throw new UnsupportedOperationException(
            "Task '" + name() + "' requires " + paramNum + " parameters, but 1 was passed");
    }



    /**
     * Equivalent of {@link #getNameWithVersion(String)} with two parameters. Works only for enum constants that have
     * two parameters in their message format, for example {@link #xtest}.
     *
     * @param pVersionParam1 contents for the first placeholder in the message format
     * @param pVersionParam2 contents for the second placeholder in the message format
     * @return the specific task name
     */
    private String getNameWithVersion(final String pVersionParam1, final String pVersionParam2)
    {
        final int paramNum = nameWithVersionTemplate.getFormats().length;
        if (paramNum == 2) {
            return nameWithVersionTemplate.format(new String[]{pVersionParam1, pVersionParam2});
        }
        throw new UnsupportedOperationException(
            "Task '" + name() + "' requires " + paramNum + " parameters, but 2 were passed");
    }



    /**
     * Flag that tells us how names are expected to be created when the {@link NameFactory} is called with the default
     * Checkstyle version. If <code>true</code>, the name will be resolved by calling {@link #getNameWithoutVersion} .
     * If <code>false</code>, it will be resolved by calling {@link #getNameWithVersion}.
     *
     * @return flag
     */
    private boolean useVersionForDefault()
    {
        return mUseVersionForDefault;
    }



    /**
     * Turn any String into a String that can be used in a Gradle entity name by replacing dots with underscores
     * and capitalizing the first character.
     *
     * @param pRawString the raw String, for example {@code "6.4.1"} or {@code "java7"}
     * @return the Gradle-compatible String, for example {@code "6_4_1"} or {@code "Java7"}
     */
    private String gradlify(final String pRawString)
    {
        final String g = DOT_PATTERN.matcher(pRawString).replaceAll("_");
        return Character.toUpperCase(g.charAt(0)) + g.substring(1);
    }



    /**
     * Generate the version-specific name of the given object via the object's name rule.
     *
     * @param pDepConfig the dependency configuration for which the task is intended
     * @return the task name
     */
    @Nonnull
    public String getName(@Nonnull final DependencyConfig pDepConfig)
    {
        if (pDepConfig.isDefaultConfig() && !useVersionForDefault()) {
            return getNameWithoutVersion();
        }
        else {
            return getNameWithVersion(gradlify(pDepConfig.getName()));
        }
    }



    /**
     * Specialized variant of {@link #getName} used for <code>xtest</code> task names.
     *
     * @param pDepConfig the dependency configuration for which the task is intended
     * @param pVersionAgainst the runtime Checkstyle version against which the task shall run
     * @return the task name
     */
    public String getName(@Nonnull final DependencyConfig pDepConfig, @Nonnull final String pVersionAgainst)
    {
        return getNameWithVersion(gradlify(pDepConfig.getCheckstyleBaseVersion()), gradlify(pVersionAgainst));
    }
}
