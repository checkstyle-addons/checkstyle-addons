package com.thomasjensen.checkstyle.addons.checks.coding;
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

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.thomasjensen.checkstyle.addons.BaseCheckTestSupport;
import org.junit.Test;


/**
 * Unit test of {@link LostInstanceCheck}.
 */
public class LostInstanceCheckTest
    extends BaseCheckTestSupport
{
    public LostInstanceCheckTest()
    {
        setCheckShortname(LostInstanceCheck.class);
    }



    @Test
    public void testDefault()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(LostInstanceCheck.class);
        final String[] expected = {//
            "13:9: Instance created here is not used for anything.",
            "39:22: Instance created here is not used for anything.",
            "40:35: Instance created here is not used for anything.",
            "44:13: Instance created here is not used for anything.",
            "46:13: Instance created here is not used for anything.",
            "49:13: Instance created here is not used for anything.",
            "53:13: Instance created here is not used for anything.",
            "55:13: Instance created here is not used for anything.",
            "58:13: Instance created here is not used for anything.",
            "60:13: Instance created here is not used for anything.",
            "63:30: Instance created here is not used for anything.",
            "66:14: Instance created here is not used for anything.",
            "69:35: Instance created here is not used for anything.",
            "78:12: Instance created here is not used for anything.",
            "81:53: Instance created here is not used for anything.",
            "98:9: Instance created here is not used for anything.",
            "104:17: Instance created here is not used for anything." //
        };
        verify(checkConfig, getPath("coding/InputLostInstance.java"), expected);
    }
}

