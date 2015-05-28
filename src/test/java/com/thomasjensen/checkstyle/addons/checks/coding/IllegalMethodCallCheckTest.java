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
 * Unit test of {@link IllegalMethodCallCheck}.
 *
 * @author Thomas Jensen
 */
public class IllegalMethodCallCheckTest
    extends BaseCheckTestSupport
{
    @Test
    public void testDefault()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(IllegalMethodCallCheck.class);
        checkConfig.addAttribute("illegalMethodNames", "doesNotOccur, forName");
        final String[] expected = {
            "6:49: Illegal method call: forName()",
            "9:15: Illegal method call: forName()",
            "10:31: Illegal method call: forName()",
            "16:31: Illegal method call: forName()",
            "18:30: Illegal method call: forName()",
            "19:25: Illegal method call: forName()",
            "20:9: Illegal method call: forName()",
            "23:21: Illegal method call: forName()",
        };
        verify(checkConfig, getPath("coding/InputIllegalMethodCall.java"), expected);
    }



    @Test
    public void testUnconfigured1()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(IllegalMethodCallCheck.class);
        final String[] expected = {};
        verify(checkConfig, getPath("coding/InputIllegalMethodCall.java"), expected);
    }



    @Test
    public void testUnconfigured2()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(IllegalMethodCallCheck.class);
        checkConfig.addAttribute("illegalMethodNames", "");
        final String[] expected = {};
        verify(checkConfig, getPath("coding/InputIllegalMethodCall.java"), expected);
    }



    @Test
    public void testRequiredTokens()
    {
        int[] tokens = new IllegalMethodCallCheck().getRequiredTokens();
        TestCase.assertNotNull(tokens);
        TestCase.assertEquals(1, tokens.length);
    }
}

