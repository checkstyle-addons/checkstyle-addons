package com.thomasjensen.checkstyle.addons.checks.misc;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;


/**
 * Unit tests of {@link CatalogEntry}.
 */
public class CatalogEntryTest
{
    private static List<CatalogEntry> sTestData = new ArrayList<>();



    public static DetailAST createAstForLineCol(final int pLineNo, final int pColumnNo)
    {
        DetailAST ast = null;
        if (DetailAST.class.isInterface()) {
            ast = Mockito.mock(DetailAST.class);
            Mockito.when(ast.getLineNo()).thenReturn(pLineNo);
            Mockito.when(ast.getColumnNo()).thenReturn(pColumnNo);
        }
        else {
            try {
                ast = DetailAST.class.newInstance();
                ast.setLineNo(pLineNo);
                ast.setColumnNo(pColumnNo);
            }
            catch (ReflectiveOperationException e) {
                Assert.fail("unexpected error: " + e.getMessage());
            }
        }
        return ast;
    }



    @BeforeClass
    public static void createTestData()
    {
        for (int i = 10, j = 0; i <= 30; i++, j++) {
            DetailAST ast = createAstForLineCol(i, 0);
            String key = String.valueOf((char) ('z' - j));
            if ((j + 1) % 6 == 0) {
                i--;  // some should be on the same line
            }
            String constantName = String.valueOf((char) ('A' + j));
            sTestData.add(new CatalogEntry(constantName, key, ast));
        }
        Collections.shuffle(sTestData);
        sTestData = Collections.unmodifiableList(sTestData);

        Assert.assertEquals(25, sTestData.size());
    }



    /**
     * The natural ordering of CatalogEntry objects should be the line number of their ASTs, followed by the constant
     * name; in other words, the order of their appearance in the source file, and if two constants are defined on the
     * same line, their alphabetical order. The key does not matter.
     */
    @Test
    public void testOrder()
    {
        List<CatalogEntry> sorted = new ArrayList<>(sTestData);
        Collections.sort(sorted);

        Assert.assertEquals(sTestData.size(), sorted.size());

        int lastLine = -1;
        String lastConst = " ";
        for (CatalogEntry entry : sorted) {
            Assert.assertTrue(entry.getAst().getLineNo() >= lastLine);
            if (entry.getAst().getLineNo() == lastLine) {
                Assert.assertTrue(entry.getConstantName().compareTo(lastConst) > 0);
            }
            lastLine = entry.getAst().getLineNo();
            lastConst = entry.getConstantName();
        }
    }



    @Test
    public void testEqualsHashCodeEqual()
    {
        final int line = 1;
        final String constName = "A";

        DetailAST ast1 = createAstForLineCol(line, 0);
        CatalogEntry entry1 = new CatalogEntry(constName, "a", ast1);

        DetailAST ast2 = createAstForLineCol(line, 0);
        CatalogEntry entry2 = new CatalogEntry(constName, "b", ast2);

        Assert.assertEquals(entry1, entry2);
        Assert.assertEquals(entry1.hashCode(), entry2.hashCode());
    }



    @Test
    public void testEqualsHashCodeNotEqual()
    {
        final String constName = "A";

        DetailAST ast1 = createAstForLineCol(1, 0);
        CatalogEntry entry1 = new CatalogEntry(constName, "a", ast1);

        DetailAST ast2 = createAstForLineCol(2, 0);
        CatalogEntry entry2 = new CatalogEntry(constName, "b", ast2);

        Assert.assertNotEquals(entry1, entry2);
        Assert.assertNotEquals(entry1.hashCode(), entry2.hashCode());
    }



    @Test
    public void testEqualsHashCodeNotEqualCase()
    {
        final int line = 1;

        DetailAST ast1 = createAstForLineCol(line, 0);
        CatalogEntry entry1 = new CatalogEntry("A", "a", ast1);

        DetailAST ast2 = createAstForLineCol(line, 0);
        CatalogEntry entry2 = new CatalogEntry("a", "b", ast2);

        Assert.assertNotEquals(entry1, entry2);
        Assert.assertNotEquals(entry1.hashCode(), entry2.hashCode());
    }



    @Test
    public void testEqualsCornerCases()
    {
        DetailAST ast = createAstForLineCol(42, 9);
        CatalogEntry entry = new CatalogEntry("a", "b", ast);

        Assert.assertNotEquals(entry, "somethingElse");
        Assert.assertNotEquals(entry, null);
        Assert.assertEquals(entry, entry);
    }



    @Test
    public void testToString()
    {
        DetailAST ast = createAstForLineCol(42, 9);
        CatalogEntry entry = new CatalogEntry("a", "b", ast);

        Assert.assertEquals("CatalogEntry{42:9, key=\"b\", constantName=\"a\"}", entry.toString());
    }



    @Test
    public void testGetters()
    {
        DetailAST ast = createAstForLineCol(42, 9);
        CatalogEntry entry = new CatalogEntry("a", "b", ast);

        Assert.assertEquals("a", entry.getConstantName());
        Assert.assertEquals("b", entry.getKey());
        Assert.assertEquals(42, entry.getAst().getLineNo());
        Assert.assertEquals(9, entry.getAst().getColumnNo());
    }
}
