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

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import org.junit.Assert;
import org.junit.Test;


/**
 * Unit tests of {@link Util}.
 *
 * @author Thomas Jensen
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

        f = new File("./\u0000");
        c = Util.canonize(f);
        Assert.assertTrue(c.isAbsolute());
        Assert.assertTrue(c.getPath().endsWith("." + File.separator + '\u0000'));
    }
}
