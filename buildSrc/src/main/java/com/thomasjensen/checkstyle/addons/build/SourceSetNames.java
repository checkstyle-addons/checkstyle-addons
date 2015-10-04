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
 * The source sets supported by the name factory.
 *
 * @author Thomas Jensen
 */
public enum SourceSetNames
    implements NameWithVersion
{
    /** <code>main</code> / <code>main{0}</code> */
    main("main{0}"),

    /** <code>test</code> / <code>test{0}</code> */
    test("test{0}");

    //

    private final MessageFormat nameWithVersionTemplate;



    private SourceSetNames(final String pNameWithVersionTemplate)
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
