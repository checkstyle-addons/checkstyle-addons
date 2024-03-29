package com.thomasjensen.checkstyle.addons.checks.regexp;
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

import java.io.File;
import java.io.IOException;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import com.thomasjensen.checkstyle.addons.BaseCheckTestSupport;
import com.thomasjensen.checkstyle.addons.BaseFileSetCheckTestSupport;
import com.thomasjensen.checkstyle.addons.CsVersionInfo;
import com.thomasjensen.checkstyle.addons.Helpers;


/**
 * Unit test of {@link RegexpOnFilenameOrgCheck}.
 */
public class RegexpOnFilenameOrgCheckTest
    extends BaseFileSetCheckTestSupport
{
    private static final String REAL_EXT = "txt";

    private static final String SIMPLE_FILENAME = "InputRegexpOnFilename." + REAL_EXT;

    private DefaultConfiguration mCheckConfig;



    public RegexpOnFilenameOrgCheckTest()
    {
        setCheckShortname(RegexpOnFilenameOrgCheck.class);
    }



    @Before
    public void setUp()
    {
        mCheckConfig = createCheckConfig(RegexpOnFilenameOrgCheck.class);
    }



    protected static String getPath(final String pSimpleFilename)
        throws IOException
    {
        return BaseCheckTestSupport.getPath("regexp/" + pSimpleFilename);
    }



    @Test
    public void testSelectByExtension_Include1()
        throws Exception
    {
        final String filepath = getPath(SIMPLE_FILENAME);
        final String regexp = "no_match";
        Helpers.addConfigProperty(mCheckConfig, "fileExtensions", "java, " + REAL_EXT);
        Helpers.addConfigProperty(mCheckConfig, "regexp", regexp);
        Helpers.addConfigProperty(mCheckConfig, "mode", "required");
        final String[] expected =
            {"1: Filename '" + SIMPLE_FILENAME + "' does not contain required pattern '" + regexp + "'."};
        verify(mCheckConfig, filepath, expected);
    }



    @Test
    public void testSelectByExtension_Include2()
        throws Exception
    {
        final String filepath = getPath(SIMPLE_FILENAME);
        final String regexp = "no_match";
        Helpers.addConfigProperty(mCheckConfig, "fileExtensions", REAL_EXT);
        Helpers.addConfigProperty(mCheckConfig, "regexp", regexp);
        Helpers.addConfigProperty(mCheckConfig, "mode", "required");
        final String[] expected =
            {"1: Filename '" + SIMPLE_FILENAME + "' does not contain required pattern '" + regexp + "'."};
        verify(mCheckConfig, filepath, expected);
    }



    @Test
    public void testSelectByExtension_Exclude()
        throws Exception
    {
        final String filepath = getPath(SIMPLE_FILENAME);
        final String regexp = "no_match";
        Helpers.addConfigProperty(mCheckConfig, "fileExtensions", "noMatch");
        Helpers.addConfigProperty(mCheckConfig, "regexp", regexp);
        Helpers.addConfigProperty(mCheckConfig, "simple", "false");
        final String[] expected = {};
        verify(mCheckConfig, filepath, expected);
    }



    @Test
    public void testSelectByRegexp_Include1()
        throws Exception
    {
        final String filepath = getPath(SIMPLE_FILENAME);
        final String regexp = "no_match";
        Helpers.addConfigProperty(mCheckConfig, "fileExtensions", REAL_EXT);
        Helpers.addConfigProperty(mCheckConfig, "selection", SIMPLE_FILENAME + "$");
        Helpers.addConfigProperty(mCheckConfig, "regexp", regexp);
        Helpers.addConfigProperty(mCheckConfig, "mode", "required");
        final String[] expected =
            {"1: Filename '" + SIMPLE_FILENAME + "' does not contain required pattern '" + regexp + "'."};
        verify(mCheckConfig, filepath, expected);
    }



    @Test
    public void testSelectByRegexp_Include2()
        throws Exception
    {
        final String filepath = getPath(SIMPLE_FILENAME);
        final String regexp = "no_match";
        Helpers.addConfigProperty(mCheckConfig, "regexp", regexp);
        Helpers.addConfigProperty(mCheckConfig, "selection", SIMPLE_FILENAME + "$");
        Helpers.addConfigProperty(mCheckConfig, "mode", "required");
        final String[] expected =
            {"1: Filename '" + SIMPLE_FILENAME + "' does not contain required pattern '" + regexp + "'."};
        verify(mCheckConfig, filepath, expected);
    }



    @Test
    public void testSelectByRegexp_Exclude()
        throws Exception
    {
        final String filepath = getPath(SIMPLE_FILENAME);
        final String regexp = "no_match";
        Helpers.addConfigProperty(mCheckConfig, "regexp", regexp);
        Helpers.addConfigProperty(mCheckConfig, "selection", "^no_match");
        Helpers.addConfigProperty(mCheckConfig, "mode", "required");
        final String[] expected = {};
        verify(mCheckConfig, filepath, expected);
    }



    @Test
    public void testNoRegexpsGiven_Ok()
        throws Exception
    {
        final String filepath = getPath(SIMPLE_FILENAME);
        final String[] expected = {};
        verify(mCheckConfig, filepath, expected);
    }



    @Test
    public void testIllegal()
        throws Exception
    {
        final String filepath = getPath(SIMPLE_FILENAME);
        final String slash = "[\\\\/]";
        final String regexp = slash + getClass().getPackage().getName().replace(".", slash) + slash;
        Helpers.addConfigProperty(mCheckConfig, "regexp", regexp);
        Helpers.addConfigProperty(mCheckConfig, "simple", "false");
        Helpers.addConfigProperty(mCheckConfig, "mode", "illegal");
        final String[] expected = {"1: Filename '" + filepath + "' contains illegal pattern '" + regexp + "'."};
        verify(mCheckConfig, filepath, expected);
    }



    @Test
    public void testRequired()
        throws Exception
    {
        final String filepath = getPath(SIMPLE_FILENAME);
        final String slash = "[\\\\/]";
        final String regexp = slash + getClass().getPackage().getName().replace(".", slash) + slash;
        Helpers.addConfigProperty(mCheckConfig, "regexp", regexp);
        Helpers.addConfigProperty(mCheckConfig, "simple", "false");
        Helpers.addConfigProperty(mCheckConfig, "mode", "required");
        final String[] expected = {};
        verify(mCheckConfig, filepath, expected);
    }



    @Test
    public void testIllegal_Not()
        throws Exception
    {
        final String filepath = getPath(SIMPLE_FILENAME);
        final String regexp = "no_match";
        Helpers.addConfigProperty(mCheckConfig, "regexp", regexp);
        Helpers.addConfigProperty(mCheckConfig, "simple", "false");
        Helpers.addConfigProperty(mCheckConfig, "mode", "illegal");
        final String[] expected = {};
        verify(mCheckConfig, filepath, expected);
    }



    @Test
    public void testRequired_Not()
        throws Exception
    {
        final String filepath = getPath(SIMPLE_FILENAME);
        final String regexp = "no_match";
        Helpers.addConfigProperty(mCheckConfig, "regexp", regexp);
        Helpers.addConfigProperty(mCheckConfig, "simple", "false");
        Helpers.addConfigProperty(mCheckConfig, "mode", "required");
        final String[] expected =
            {"1: Filename '" + filepath + "' does not contain required pattern '" + regexp + "'."};
        verify(mCheckConfig, filepath, expected);
    }



    @Test
    public void testIllegalSimple()
        throws Exception
    {
        final String filepath = getPath(SIMPLE_FILENAME);
        final String regexp = "^" + SIMPLE_FILENAME + "$";
        Helpers.addConfigProperty(mCheckConfig, "regexp", regexp);
        final String[] expected = {"1: Filename '" + SIMPLE_FILENAME + "' contains illegal pattern '" + regexp + "'."};
        verify(mCheckConfig, filepath, expected);
    }



    @Test
    public void testRequiredSimple()
        throws Exception
    {
        final String filepath = getPath(SIMPLE_FILENAME);
        final String regexp = "^" + SIMPLE_FILENAME + "$";
        Helpers.addConfigProperty(mCheckConfig, "regexp", regexp);
        Helpers.addConfigProperty(mCheckConfig, "mode", "required");
        final String[] expected = {};
        verify(mCheckConfig, filepath, expected);
    }



    @Test
    public void testBrokenRegexp()
        throws Exception
    {
        final String filepath = getPath(SIMPLE_FILENAME);
        final String illegal = "*$"; // incorrect syntax
        Helpers.addConfigProperty(mCheckConfig, "regexp", illegal);
        Helpers.addConfigProperty(mCheckConfig, "simple", "false");
        try {
            verify(mCheckConfig, filepath, new String[]{});
            Assert.fail("CheckstyleException was not thrown");
        }
        catch (CheckstyleException expected) {
            // expected
        }
    }



    @Test
    public void testBrokenModeParam()
        throws Exception
    {
        final String filepath = getPath(SIMPLE_FILENAME);
        Helpers.addConfigProperty(mCheckConfig, "mode", "unknownMode");
        try {
            verify(mCheckConfig, filepath, new String[]{});
            Assert.fail("CheckstyleException was not thrown");
        }
        catch (CheckstyleException expected) {
            // expected
        }
    }



    @Test
    public void testTrailingSpace()
        throws Exception
    {
        Assume.assumeThat(CsVersionInfo.currentCsVersion(), CsVersionInfo.isLessThan("8.10"));
        File tempFile = null;
        try {
            tempFile = File.createTempFile("addons-test-", ".txt ");
            final String[] expected =
                {"1: Filename '" + tempFile.getName() + "' contains illegal pattern '" + "^(?:\\s+.*|.*?\\s+)$" + "'."};
            verify(mCheckConfig, tempFile.getAbsolutePath(), expected);
        }
        finally {
            if (tempFile != null) {
                Assert.assertTrue("Could not delete temp file: " + tempFile.getAbsolutePath(), tempFile.delete());
            }
        }
    }



    @Test
    public void testLeadingSpace()
        throws Exception
    {
        File tempFile = null;
        try {
            tempFile = File.createTempFile(" addons-test-", ".txt");
            final String[] expected =
                {"1: Filename '" + tempFile.getName() + "' contains illegal pattern '" + "^(?:\\s+.*|.*?\\s+)$" + "'."};
            verify(mCheckConfig, tempFile.getAbsolutePath(), expected);
        }
        finally {
            if (tempFile != null) {
                Assert.assertTrue("Could not delete temp file: " + tempFile.getAbsolutePath(), tempFile.delete());
            }
        }
    }



    @Test
    public void testNoSpaces()
        throws Exception
    {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("addons-test-", ".txt");
            final String[] expected = {};
            verify(mCheckConfig, tempFile.getAbsolutePath(), expected);
        }
        finally {
            if (tempFile != null) {
                Assert.assertTrue("Could not delete temp file: " + tempFile.getAbsolutePath(), tempFile.delete());
            }
        }
    }



    @Test
    public void testNullEmptyRegexParams_Ok()
    {
        RegexpOnFilenameOrgCheck check = new RegexpOnFilenameOrgCheck();
        check.setSelection(null);
        check.setSelection("");
        check.setRegexp(null);
        check.setRegexp("");
    }



    @Test
    public void testOptionValues()
    {
        Assert.assertSame(RegexpOnFilenameOrgOption.REQUIRED, RegexpOnFilenameOrgOption.valueOf("REQUIRED"));
        Assert.assertSame(RegexpOnFilenameOrgOption.ILLEGAL, RegexpOnFilenameOrgOption.valueOf("ILLEGAL"));
        Assert.assertSame(RegexpOnFilenameOrgOption.REQUIRED, RegexpOnFilenameOrgOption.valueOfIgnoreCase("required"));
        Assert.assertSame(RegexpOnFilenameOrgOption.ILLEGAL, RegexpOnFilenameOrgOption.valueOfIgnoreCase("illegal"));
    }
}
