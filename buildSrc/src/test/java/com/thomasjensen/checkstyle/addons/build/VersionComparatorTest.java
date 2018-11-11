package com.thomasjensen.checkstyle.addons.build;
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

import org.junit.Assert;
import org.junit.Test;


/**
 * Unit tests of {@link VersionComparator}.
 */
public class VersionComparatorTest
{
    @Test
    public void testComp()
    {
        VersionComparator testee = new VersionComparator();

        Assert.assertTrue(testee.compare("6.1", "6.1.2") < 0);
        Assert.assertTrue(testee.compare("6.1.2", "6.1") > 0);
        Assert.assertTrue(testee.compare("6.2", "6.1.2") > 0);
        Assert.assertTrue(testee.compare("6.1.2", "6.2") < 0);
        Assert.assertTrue(testee.compare("6.1", "7.1") < 0);
        Assert.assertTrue(testee.compare("7.1", "6.1") > 0);
        Assert.assertTrue(testee.compare("6.1.3", "6.1.2") > 0);
        Assert.assertTrue(testee.compare("6.1.2", "6.1.3") < 0);
        Assert.assertTrue(testee.compare("6.1", "foo") < 0);
        Assert.assertTrue(testee.compare("foo", "6.1") > 0);
        Assert.assertTrue(testee.compare("foo", "FOO") > 0);
        Assert.assertTrue(testee.compare("FOO", "foo") < 0);
        Assert.assertTrue(testee.compare("6.1", null) < 0);
        Assert.assertTrue(testee.compare(null, "6.1") > 0);
        Assert.assertTrue(testee.compare(null, null) == 0);
        Assert.assertTrue(testee.compare("6.1.2", "6.1.2") == 0);
        Assert.assertTrue(testee.compare("6.1", "6.1") == 0);
        Assert.assertTrue(testee.compare("foo", "foo") == 0);
        Assert.assertTrue(testee.compare("6", "0815") > 0);  // lex. order b/c "6" does not match version pattern
        Assert.assertTrue(testee.compare("6.1", "6.x") < 0);
        Assert.assertTrue(testee.compare("6.x", "6.1") > 0);
        Assert.assertTrue(testee.compare("6.1.8", "6.1.x") < 0);
        Assert.assertTrue(testee.compare("6.1.x", "6.1.8") > 0);
    }
}
