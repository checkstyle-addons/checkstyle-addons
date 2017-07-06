package com.thomasjensen.checkstyle.addons.checks;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.thomasjensen.checkstyle.addons.BaseCheckTestSupport;
import org.junit.Assert;
import org.junit.Test;


/**
 * Unit tests for the binary name handling features of {@link AbstractAddonsCheck}.
 */
public class BinaryNameTest
    extends BaseCheckTestSupport
{

    /**
     * Instantiable class for AbstractAddonsCheck.
     */
    public static class Check
        extends AbstractAddonsCheck
    {
        public static final List<BinaryName> FOUND_BINARY_NAMES = new ArrayList<BinaryName>();

        private final Set<String> foundPositions = new HashSet<String>();



        @Override
        public void beginTree(final DetailAST pRootAst)
        {
            super.beginTree(pRootAst);
            FOUND_BINARY_NAMES.clear();
            foundPositions.clear();

            // required tokens must contain our declared token
            boolean found = false;
            for (final int t : getRequiredTokens()) {
                if (t == TokenTypes.OBJBLOCK) {
                    found = true;
                    break;
                }
            }
            Assert.assertTrue(found);
        }



        @Override
        public Set<Integer> getRelevantTokens()
        {
            return Collections.singleton(TokenTypes.OBJBLOCK);
        }



        @Override
        protected void visitKnownType(@Nonnull final BinaryName pBinaryClassName, @Nonnull final DetailAST pAst)
        {
            FOUND_BINARY_NAMES.add(pBinaryClassName);

            DetailAST a = getClassDeclarationPosition(pBinaryClassName);
            String pos = a.getLineNo() + ":" + a.getColumnNo();
            Assert.assertFalse(foundPositions.contains(pos));
            foundPositions.add(pos);

            Assert.assertEquals("com.thomasjensen.checkstyle.addons.checks", getMyPackage());
        }
    }



    @Test
    public void testBinaryNames()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(Check.class);

        final List<BinaryName> expected = Arrays.asList(//
            new BinaryName("com.thomasjensen.checkstyle.addons.checks", "InputBinaryName"), //
            new BinaryName("com.thomasjensen.checkstyle.addons.checks", "InputBinaryName", "A"), // latin 'A'
            new BinaryName("com.thomasjensen.checkstyle.addons.checks", "InputBinaryName", "\u0391"), // greek 'A'
            new BinaryName("com.thomasjensen.checkstyle.addons.checks", "InputBinaryName", "\u0410"), // cyrillic 'A'
            new BinaryName("com.thomasjensen.checkstyle.addons.checks", "InputBinaryName", "$"), //
            new BinaryName("com.thomasjensen.checkstyle.addons.checks", "InputBinaryName", "$", "B$"), //
            new BinaryName("com.thomasjensen.checkstyle.addons.checks", "InputBinaryName", "$", "B$", "$B"), //
            new BinaryName("com.thomasjensen.checkstyle.addons.checks", "InputBinaryName", "$", "B$", "$B", "C"), //
            new BinaryName("com.thomasjensen.checkstyle.addons.checks", "InputBinaryName", "C$B"));

        verify(checkConfig, getPath("InputBinaryName.java"), new String[0]);

        Assert.assertEquals(expected, Check.FOUND_BINARY_NAMES);
    }



    @Test
    public void testBinaryNames2()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(Check.class);

        final List<BinaryName> expected = Arrays.asList(//
            new BinaryName("com.thomasjensen.checkstyle.addons.checks", "InputBinary$Name"), //
            new BinaryName("com.thomasjensen.checkstyle.addons.checks", "InputBinary$Name", "Foo$Bar"));

        verify(checkConfig, getPath("InputBinary$Name.java"), new String[0]);

        Assert.assertEquals(expected, Check.FOUND_BINARY_NAMES);
    }



    @Test
    public void testToString()
    {
        Assert.assertEquals("Foo", new BinaryName(null, "Foo").toString());
        Assert.assertEquals("com.Foo", new BinaryName("com", "Foo").toString());
        Assert.assertEquals("com.foo.Foo$Bar", new BinaryName("com.foo", "Foo", "Bar").toString());
        Assert.assertEquals("com.foo.A$$$B$C$D", new BinaryName("com.foo", "A", "$", "B", "C$D").toString());
    }



    @Test
    public void testIllegalNulls()
    {
        try {
            new BinaryName("com.foo", (Collection<String>) null);
            Assert.fail("Expected exception was not thrown");
        }
        catch (NullPointerException e) {
            // expected
        }

        try {
            new BinaryName("com.foo", (String) null, (String[]) null);
            Assert.fail("Expected exception was not thrown");
        }
        catch (IllegalArgumentException e) {
            // expected
        }

        try {
            new BinaryName("com.foo", Collections.<String>emptyList());
            Assert.fail("Expected exception was not thrown");
        }
        catch (IllegalArgumentException e) {
            // expected
        }
    }



    @Test
    public void testGetters()
    {
        BinaryName bn = new BinaryName("com.foo", "Foo", "Bar");
        Assert.assertEquals("Foo", bn.getOuterSimpleName());
        Assert.assertEquals("Bar", bn.getInnerSimpleName());
        Assert.assertEquals("com.foo.Foo", bn.getOuterFqcn());
        Assert.assertEquals("com.foo", bn.getPackage());

        bn = new BinaryName("com.foo", "Foo");
        Assert.assertEquals("Foo", bn.getOuterSimpleName());
        Assert.assertNull(bn.getInnerSimpleName());
        Assert.assertEquals("com.foo.Foo", bn.getOuterFqcn());
        Assert.assertEquals("com.foo", bn.getPackage());

        bn = new BinaryName(null, "Foo");
        Assert.assertEquals("Foo", bn.getOuterSimpleName());
        Assert.assertNull(bn.getInnerSimpleName());
        Assert.assertEquals("Foo", bn.getOuterFqcn());
        Assert.assertNull(bn.getPackage());
    }



    @Test
    public void testEquals()
    {
        Assert.assertNotEquals(new BinaryName("com", "Foo"), "someString");
        Assert.assertNotEquals(new BinaryName("com", "Foo"), null);
        Assert.assertNotEquals(new BinaryName("com", "Foo"), new BinaryName("com", "Bar"));
        Assert.assertNotEquals(new BinaryName("com", "Foo"), new BinaryName("net", "Foo"));
        Assert.assertNotEquals(new BinaryName("com", "Foo"), new BinaryName(null, "Foo"));
        Assert.assertNotEquals(new BinaryName(null, "Foo"), new BinaryName("com", "Foo"));
        Assert.assertNotEquals(new BinaryName(null, "Foo"), new BinaryName(null, "Bar"));

        BinaryName bn = new BinaryName("com", "Foo");
        Assert.assertEquals(bn, bn);
    }
}
