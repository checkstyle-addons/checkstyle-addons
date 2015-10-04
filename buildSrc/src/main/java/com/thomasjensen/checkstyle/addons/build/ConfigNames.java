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
 * The configurations supported by the name factory.
 *
 * @author Thomas Jensen
 */
public enum ConfigNames
    implements NameWithVersion
{
    /** <code>mainCompile</code> / <code>main{0}Compile</code> */
    mainCompile("main{0}Compile"),

    /** <code>mainRuntime</code> / <code>main{0}Runtime</code> */
    mainRuntime("main{0}Runtime"),

    /** <code>testCompile</code> / <code>test{0}Compile</code> */
    testCompile("test{0}Compile"),

    /** <code>testRuntime</code> / <code>test{0}Runtime</code> */
    testRuntime("test{0}Runtime"),

    /** <code>provided</code> / <code>provided{0}</code> */
    provided("provided{0}");

    //

    private final MessageFormat nameWithVersionTemplate;



    private ConfigNames(final String pNameWithVersionTemplate)
    {
        nameWithVersionTemplate = new MessageFormat(pNameWithVersionTemplate);
    }



    @Override
    public String getNameWithoutVersion()
    {
        return name();
    }



    @Override
    public String getNameWithVersion(final String pVersionParam)
    {
        return nameWithVersionTemplate.format(new String[]{pVersionParam});
    }



    @Override
    public boolean useVersionForDefault()
    {
        return false;
    }

}
