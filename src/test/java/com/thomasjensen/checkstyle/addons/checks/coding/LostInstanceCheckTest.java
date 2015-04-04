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
import junit.framework.TestCase;
import org.junit.Test;


/**
 * Unit test of {@link LostInstanceCheck}.
 *
 * @author Thomas Jensen
 */
public class LostInstanceCheckTest
    extends BaseCheckTestSupport
{
    @Test
    public void testDefault()
        throws Exception
    {
        final DefaultConfiguration checkConfig =
            createCheckConfig(LostInstanceCheck.class);
        final String[] expected = {
            "10:9: Instance created here is not used for anything.",
            "36:22: Instance created here is not used for anything.",
            "37:35: Instance created here is not used for anything.",
            "41:13: Instance created here is not used for anything.",
            "43:13: Instance created here is not used for anything.",
            "46:13: Instance created here is not used for anything.",
            "50:13: Instance created here is not used for anything.",
            "52:13: Instance created here is not used for anything.",
            "55:13: Instance created here is not used for anything.",
            "57:13: Instance created here is not used for anything.",
            "60:30: Instance created here is not used for anything.",
            "63:14: Instance created here is not used for anything.",
            "66:35: Instance created here is not used for anything.",
            "73:12: Instance created here is not used for anything.",
            "76:53: Instance created here is not used for anything.",
        };
        verify(checkConfig, getPath("coding/InputLostInstance.java"), expected);
    }



    @Test
    public void testRequiredTokens()
    {
        int[] tokens = new LostInstanceCheck().getRequiredTokens();
        TestCase.assertNotNull(tokens);
        TestCase.assertEquals(1, tokens.length);
    }
}

