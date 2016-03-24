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

import java.text.MessageFormat;


/**
 * The tasks supported by the name factory.
 *
 * @author Thomas Jensen
 */
public enum TaskNames
    implements NameWithVersion
{
    /** <code>assemble</code> / <code>assemble{0}</code> */
    assemble(true, "assemble{0}"),

    /** <code>compileJava</code> / <code>compileMain{0}Java</code> */
    compileJava(false, "compileMain{0}Java"),

    /** <code>compileTestJava</code> / <code>compileTest{0}Java</code> */
    compileTestJava(false, "compileTest{0}Java"),

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
    mainClasses(false, "main{0}Classes"),     // should be called 'classes', but can't because of Groovy

    /** <code>testClasses</code> / <code>test{0}Classes</code> */
    testClasses(false, "test{0}Classes"),

    /** <code>test</code> / <code>test{0}</code> */
    test(false, "test{0}"),

    /** <code>xtest</code> / <code>xtest{0}against{1}</code> */
    xtest(true, "xtest{0}against{1}");

    //

    private final boolean mUseVersionForDefault;

    private final MessageFormat nameWithVersionTemplate;



    private TaskNames(final boolean pUseVersionForDefault, final String pNameWithVersionTemplate)
    {
        mUseVersionForDefault = pUseVersionForDefault;
        nameWithVersionTemplate = new MessageFormat(pNameWithVersionTemplate);
    }



    @Override
    public String getNameWithoutVersion()
    {
        if (this == mainClasses) {
            return "classes";
        }
        return name();
    }



    @Override
    public String getNameWithVersion(final String pVersionParam)
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
     * @param pVersionParam1 contents for the first placeholder in the message format
     * @param pVersionParam2 contents for the second placeholder in the message format
     * @return the specific task name
     */
    public String getNameWithVersion(final String pVersionParam1, final String pVersionParam2)
    {
        final int paramNum = nameWithVersionTemplate.getFormats().length;
        if (paramNum == 2) {
            return nameWithVersionTemplate.format(new String[]{pVersionParam1, pVersionParam2});
        }
        throw new UnsupportedOperationException(
            "Task '" + name() + "' requires " + paramNum + " parameters, but 2 were passed");
    }



    @Override
    public boolean useVersionForDefault()
    {
        return mUseVersionForDefault;
    }
}
