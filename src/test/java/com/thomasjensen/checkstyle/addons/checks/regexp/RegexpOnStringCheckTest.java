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
 *
 * @author Thomas Jensen
 */
public class RegexpOnStringCheckTest
    extends BaseCheckTestSupport
{
    public RegexpOnStringCheckTest()
    {
        setCheckShortname(RegexpOnStringCheck.class);
    }



    @Test
    public void testMethodName()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(RegexpOnStringCheck.class);
        checkConfig.addAttribute("regexp", "oo");

        final String[] expected = {//
            "10:25: String literal \"foo\" matches illegal pattern 'oo'.", //
            "12:38: String literal \"foo\" matches illegal pattern 'oo'.", //
            "14:28: String literal \"foo\" matches illegal pattern 'oo'.", //
            "16:42: String literal \"foo\" matches illegal pattern 'oo'.", //
            "20:13: String literal \"foo\" matches illegal pattern 'oo'.", //
            "21:20: String literal \"foo\" matches illegal pattern 'oo'.", //
            "27:23: String literal \"foo\" matches illegal pattern 'oo'.", //
            "30:20: String literal \"foo\" matches illegal pattern 'oo'.", //
            "31:17: String literal \"foo\" matches illegal pattern 'oo'.", //
            "36:38: String literal \"foo\" matches illegal pattern 'oo'.", //
            "40:20: String literal \"foo\" matches illegal pattern 'oo'.", //
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
            "14:35: String literal \"literal1\" matches illegal pattern '^literal1$'.", //
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
            "45:28: String literal \"\" matches illegal pattern '^$'.", //
        };
        verify(checkConfig, getPath("regexp/InputRegexpOnString.java"), expected);
    }
}
