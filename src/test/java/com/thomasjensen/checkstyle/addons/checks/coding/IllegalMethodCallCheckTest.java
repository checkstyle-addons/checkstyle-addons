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

import java.io.IOException;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.thomasjensen.checkstyle.addons.BaseCheckTestSupport;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * Unit test of {@link IllegalMethodCallCheck}.
 */
public class IllegalMethodCallCheckTest
    extends BaseCheckTestSupport
{
    private static String sInputFilePath = null;



    public IllegalMethodCallCheckTest()
    {
        setCheckShortname(IllegalMethodCallCheck.class);
    }



    @BeforeClass
    public static void setUp()
        throws IOException
    {
        sInputFilePath = getPath("coding/InputIllegalMethodCall.java");
    }



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
            "40:16: Illegal method call: forName()",
            "41:23: Illegal method call: forName()",
        };
        verify(checkConfig, sInputFilePath, expected);
    }



    @Test
    public void testUnconfigured1()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(IllegalMethodCallCheck.class);
        final String[] expected = {};
        verify(checkConfig, sInputFilePath, expected);
    }



    @Test
    public void testUnconfigured2()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(IllegalMethodCallCheck.class);
        checkConfig.addAttribute("illegalMethodNames", "");
        final String[] expected = {};
        verify(checkConfig, sInputFilePath, expected);
    }



    @Test
    public void testExclusions1()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(IllegalMethodCallCheck.class);
        checkConfig.addAttribute("illegalMethodNames", "forName");
        checkConfig.addAttribute("excludedQualifiers", "Inner1");
        final String[] expected = {
            "6:49: Illegal method call: forName()",
            "9:15: Illegal method call: forName()",
            "10:31: Illegal method call: forName()",
            "16:31: Illegal method call: forName()",
            "18:30: Illegal method call: forName()",
            "19:25: Illegal method call: forName()",
            "20:9: Illegal method call: forName()",
            "23:21: Illegal method call: forName()",
            "41:23: Illegal method call: forName()",
        };
        verify(checkConfig, sInputFilePath, expected);
    }



    @Test
    public void testExclusions2()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(IllegalMethodCallCheck.class);
        checkConfig.addAttribute("illegalMethodNames", "forName");
        checkConfig.addAttribute("excludedQualifiers", "Inner1, Inner1.Inner2");
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
        verify(checkConfig, sInputFilePath, expected);
    }



    @Test
    public void testTypeArgument1()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(IllegalMethodCallCheck.class);
        checkConfig.addAttribute("illegalMethodNames", "method1");
        final String[] expected = {
            "48:29: Illegal method call: method1()",
            "49:24: Illegal method call: method1()",
        };
        verify(checkConfig, sInputFilePath, expected);
    }



    @Test
    public void testTypeArgument2Exclusion()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(IllegalMethodCallCheck.class);
        checkConfig.addAttribute("illegalMethodNames", "method1");
        checkConfig.addAttribute("excludedQualifiers", "inner1");
        final String[] expected = {};
        verify(checkConfig, sInputFilePath, expected);
    }
}

