package com.thomasjensen.checkstyle.addons.util;
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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import org.junit.Assert;
import org.junit.Test;

import com.thomasjensen.checkstyle.addons.BaseCheckTestSupport;


/**
 * Unit tests of {@link Util}.
 */
public class UtilTest
{
    @Test
    public void testCloseQuietlyException()
    {
        Util.closeQuietly(new Closeable()
        {
            @Override
            public void close()
                throws IOException
            {
                throw new IOException("should be ignored");
            }
        });
    }



    @Test
    public void testGetFullIdentNull()
    {
        DetailAST ast = new DetailAST();
        ast.setLineNo(1);
        ast.setColumnNo(0);
        ast.setText("foo");
        Assert.assertNull(Util.getFullIdent(ast));
    }



    @Test
    public void testGetFirstIdentNull()
    {
        DetailAST ast = new DetailAST();
        ast.setLineNo(1);
        ast.setColumnNo(0);
        ast.setText("foo");
        Assert.assertNull(Util.getFirstIdent(ast));
    }



    @Test
    public void testCanonize()
    {
        File f = new File(".");
        File c = Util.canonize(f);
        Assert.assertTrue(c.isAbsolute());
        Assert.assertFalse(c.getPath().contains("/./") || c.getPath().contains("\\.\\"));

        if (!BaseCheckTestSupport.isJava6()) {
            f = new File("./\u0000");
            c = Util.canonize(f);
            Assert.assertTrue(c.isAbsolute());
            Assert.assertTrue(c.getPath().endsWith("." + File.separator + '\u0000'));
        }
    }



    @Test
    public void testCanonizeSlashes()
    {
        File f = new File(".");
        File c = Util.canonize(f);
        Assert.assertTrue(c.isAbsolute());
        Assert.assertTrue(c.getPath().contains(File.separator));

        final File parent = c.getParentFile();
        final String thisOne = c.getName();
        final String badSlash = File.separatorChar == '/' ? "\\" : "/";
        final File badCombined = new File(parent.getPath() + badSlash + thisOne);
        final File bc = Util.canonize(badCombined);

        Assert.assertTrue(bc.getPath().contains(File.separator));
        Assert.assertFalse(bc.getPath().contains(badSlash));
        Assert.assertEquals(c, bc);
    }



    @Test
    public void testUnion()
    {
        Set<String> set1 = new HashSet<>(Arrays.asList("a", "b", "c"));
        Set<String> set2 = new HashSet<>(Arrays.asList("c", "d", "e"));
        Set<String> union = Util.union(set1, set2);
        Assert.assertEquals(set1.size() + set2.size() - 1, union.size());
    }



    @Test
    public void testUnionNull()
    {
        Set<String> someSet = new HashSet<>(Arrays.asList("a", "b", "c"));
        Set<String> union = Util.union(someSet, null);
        Assert.assertEquals(someSet, union);

        union = Util.union(null, someSet);
        Assert.assertEquals(someSet, union);

        union = Util.union(null, null);
        Assert.assertNotNull(union);
        Assert.assertTrue(union.isEmpty());
    }



    @Test
    public void teststringEquals()
    {
        Assert.assertTrue(Util.stringEquals(null, null, true));
        Assert.assertTrue(Util.stringEquals(null, null, false));
        Assert.assertFalse(Util.stringEquals("foo", null, true));
        Assert.assertFalse(Util.stringEquals("foo", null, false));
        Assert.assertFalse(Util.stringEquals(null, "foo", true));
        Assert.assertFalse(Util.stringEquals(null, "foo", false));
        Assert.assertFalse(Util.stringEquals("bar", "foo", true));
        Assert.assertFalse(Util.stringEquals("bar", "foo", false));
        Assert.assertFalse(Util.stringEquals("FOO", "foo", true));
        Assert.assertTrue(Util.stringEquals("FOO", "foo", false));
    }
}
