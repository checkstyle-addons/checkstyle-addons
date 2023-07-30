package com.thomasjensen.checkstyle.addons;
/*
 * Checkstyle-Addons - Additional Checkstyle checks
 * Copyright (c) 2015-2023, the Checkstyle Addons contributors
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import org.junit.Assert;


/**
 * Some static helper methods for our tests.
 */
public final class Helpers
{
    private Helpers()
    {
        // utility class
    }


    public static <A> void callAstMethod(final Object pAst, final String pMethodName, final Class<A> pArgumentType,
            final A pArgument)
    {
        try {
            Method method = DetailAST.class.getMethod(pMethodName, pArgumentType);
            method.invoke(pAst, pArgument);
        }
        catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            Assert.fail("Failed to invoke DetailAST method " + pMethodName);
        }
    }


    public static void addConfigProperty(final DefaultConfiguration pCheckConfig, final String pPropertyName,
            final String pPropertyValue)
    {
        try {
            try {
                Method method = DefaultConfiguration.class.getMethod("addProperty", String.class, String.class);
                method.invoke(pCheckConfig, pPropertyName, pPropertyValue);
            }
            catch (NoSuchMethodException e) {
                try {
                    Method method = DefaultConfiguration.class.getMethod("addAttribute", String.class, String.class);
                    method.invoke(pCheckConfig, pPropertyName, pPropertyValue);
                }
                catch (NoSuchMethodException ex) {
                    Assert.fail("Failed to invoke DefaultConfiguration method addProperty()/addAttribute()");
                }
            }
        }
        catch (InvocationTargetException | IllegalAccessException e) {
            Assert.fail("Failed to invoke DefaultConfiguration method addProperty()/addAttribute(): " + e.getMessage());
        }
    }
}
