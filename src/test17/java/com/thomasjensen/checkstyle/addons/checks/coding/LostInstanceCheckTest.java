package com.thomasjensen.checkstyle.addons.checks.coding;
/*
 * Checkstyle-Addons - Additional Checkstyle checks
 * Copyright (c) 2015-2024, the Checkstyle Addons contributors
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

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import org.junit.Test;

import com.thomasjensen.checkstyle.addons.BaseCheckTestSupport;


/**
 * Unit tests of {@link LostInstanceCheck} which require Java 17, or at least a Java level greater than 11.
 */
public class LostInstanceCheckTest
    extends BaseCheckTestSupport
{
    public LostInstanceCheckTest()
    {
        setCheckShortname(LostInstanceCheck.class);
    }



    @Test
    public void testIssue4() throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(LostInstanceCheck.class);
        final String[] expected = {};
        verify(checkConfig, getPath("coding/InputLostInstance17.java"), expected);
    }
}
