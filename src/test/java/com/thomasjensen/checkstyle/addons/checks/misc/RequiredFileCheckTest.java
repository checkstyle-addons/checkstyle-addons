package com.thomasjensen.checkstyle.addons.checks.misc;
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
import java.io.IOException;
import java.util.Set;

import com.thomasjensen.checkstyle.addons.BaseCheckTestSupport;
import org.junit.Test;


/**
 * Unit test of {@link RequiredFileCheck}.
 *
 * @author Thomas Jensen
 */
public class RequiredFileCheckTest
    extends BaseCheckTestSupport
{
    @Test
    public void testReadAllDirs()
        throws IOException
    {
        RequiredFileCheck check = new RequiredFileCheck();
        check.setBaseDir(getPath("misc/RequiredFile"));
        Set<File> dirs = check.readAllDirs(new File(getPath("misc/RequiredFile")));
        System.out.println(dirs);
    }
}
