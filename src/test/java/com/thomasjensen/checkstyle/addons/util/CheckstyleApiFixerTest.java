package com.thomasjensen.checkstyle.addons.util;
/*
 * Checkstyle-Addons - Additional Checkstyle checks
 * Copyright (c) 2015-2018, Thomas Jensen and the Checkstyle Addons contributors
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

import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import org.junit.Assert;
import org.junit.Test;

import com.thomasjensen.checkstyle.addons.checks.regexp.RegexpOnStringCheck;


/**
 * Some unit tests for {@link CheckstyleApiFixer}, as far as they are necessary in addition to the others.
 */
public class CheckstyleApiFixerTest
{
    @Test
    public void getTokenName_DOT_ok()
    {
        final CheckstyleApiFixer underTest = new CheckstyleApiFixer(new RegexpOnStringCheck());
        final String tokenName = underTest.getTokenName(TokenTypes.DOT);
        Assert.assertEquals("DOT", tokenName);
    }
}
