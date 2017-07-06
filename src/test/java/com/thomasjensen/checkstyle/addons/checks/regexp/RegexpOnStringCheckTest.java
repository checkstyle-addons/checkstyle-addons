package com.thomasjensen.checkstyle.addons.checks.regexp;
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
 * Unit test of {@link RegexpOnStringCheck}.
 */
public class RegexpOnStringCheckTest
    extends BaseCheckTestSupport
{
    public RegexpOnStringCheckTest()
    {
        setCheckShortname(RegexpOnStringCheck.class);
    }



    @Test
    public void testSingleLiteralSubstring()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(RegexpOnStringCheck.class);
        checkConfig.addAttribute("regexp", "oo");

        final String[] expected = {//
            "10:25: String \"foo\" matches illegal pattern 'oo'.", //
            "12:38: String \"foo\" matches illegal pattern 'oo'.", //
            "14:28: String \"foo\" matches illegal pattern 'oo'.", //
            "16:42: String \"foo\" matches illegal pattern 'oo'.", //
            "20:13: String \"foo\" matches illegal pattern 'oo'.", //
            "21:20: String \"foo\" matches illegal pattern 'oo'.", //
            "27:23: String \"foo\" matches illegal pattern 'oo'.", //
            "30:20: String \"foobar\" matches illegal pattern 'oo'.", //
            "31:17: String \"foo\" matches illegal pattern 'oo'.", //
            "36:38: String \"foo\" matches illegal pattern 'oo'.", //
            "40:20: String \"foo\" matches illegal pattern 'oo'.", //
            "83:27: String \"foo\" matches illegal pattern 'oo'.", //
        };
        verify(checkConfig, getPath("regexp/InputRegexpOnString.java"), expected);
    }



    @Test
    public void testCompleteLiteral()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(RegexpOnStringCheck.class);
        checkConfig.addAttribute("regexp", "^literal1$");

        final String[] expected = {//
            "14:35: String \"literal1\" matches illegal pattern '^literal1$'.", //
        };
        verify(checkConfig, getPath("regexp/InputRegexpOnString.java"), expected);
    }



    @Test
    public void testCompleteLiteralSegmented()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(RegexpOnStringCheck.class);
        checkConfig.addAttribute("regexp", "^literal3$");

        final String[] expected = {//
            "79:26: String \"literal3\" matches illegal pattern '^literal3$'.", //
        };
        verify(checkConfig, getPath("regexp/InputRegexpOnString.java"), expected);
    }



    @Test
    public void testEmptyString()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(RegexpOnStringCheck.class);
        checkConfig.addAttribute("regexp", "^$");

        final String[] expected = {//
            "46:28: String \"\" matches illegal pattern '^$'.", //
        };
        verify(checkConfig, getPath("regexp/InputRegexpOnString.java"), expected);
    }



    @Test
    public void testConcat()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(RegexpOnStringCheck.class);
        checkConfig.addAttribute("regexp", "abcde");

        final String[] expected = {//
            "41:21: String \"abcdefghi\" matches illegal pattern 'abcde'.", //
            "50:23: String \"abcdefghi\" matches illegal pattern 'abcde'.", //
            "53:20: String \"abcdefghi\" matches illegal pattern 'abcde'.", //
            "54:17: String \"abcdef\" matches illegal pattern 'abcde'.", //
            "55:13: String \"abcdef\" matches illegal pattern 'abcde'.", //
            "60:25: String \"abcdef\" matches illegal pattern 'abcde'.", //
            "62:38: String \"abcdefghi\" matches illegal pattern 'abcde'.", //
            "64:28: String \"abcdefghi\" matches illegal pattern 'abcde'.", //
            "66:42: String \"abcdef\" matches illegal pattern 'abcde'.", //
            "83:53: String \"abcdef\" matches illegal pattern 'abcde'.", //
            "85:26: String \"abcdef\" matches illegal pattern 'abcde'.", //
            "87:26: String \"abcdef\" matches illegal pattern 'abcde'.", //
            "89:28: String \"abcdef\" matches illegal pattern 'abcde'.", //
            "91:26: String \"abcdef\" matches illegal pattern 'abcde'.", //
            "93:27: String \"abcdef\" matches illegal pattern 'abcde'.", //
        };
        verify(checkConfig, getPath("regexp/InputRegexpOnString.java"), expected);
    }



    @Test
    public void testOverlap()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(RegexpOnStringCheck.class);
        checkConfig.addAttribute("regexp", "zzz");

        final String[] expected = {//
            "68:25: String \"aaazzz\" matches illegal pattern 'zzz'.", //
        };
        verify(checkConfig, getPath("regexp/InputRegexpOnString.java"), expected);
    }



    @Test
    public void testConcat2()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(RegexpOnStringCheck.class);
        checkConfig.addAttribute("regexp", "defghi");

        final String[] expected = {//
            "41:21: String \"abcdefghi\" matches illegal pattern 'defghi'.", //
            "50:23: String \"abcdefghi\" matches illegal pattern 'defghi'.", //
            "53:20: String \"abcdefghi\" matches illegal pattern 'defghi'.", //
            "62:38: String \"abcdefghi\" matches illegal pattern 'defghi'.", //
            "64:28: String \"abcdefghi\" matches illegal pattern 'defghi'.", //
        };
        verify(checkConfig, getPath("regexp/InputRegexpOnString.java"), expected);
    }



    @Test
    public void testVeryLongString()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(RegexpOnStringCheck.class);
        checkConfig.addAttribute("regexp", "eee");

        final String[] expected = {//
            "71:33: String \"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa...\" "
                + "matches illegal pattern 'eee'.", //
        };
        verify(checkConfig, getPath("regexp/InputRegexpOnString.java"), expected);
    }
}
