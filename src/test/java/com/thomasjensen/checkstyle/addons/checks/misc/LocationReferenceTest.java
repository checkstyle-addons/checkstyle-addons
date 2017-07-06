package com.thomasjensen.checkstyle.addons.checks.misc;
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
import org.junit.Test;

import com.thomasjensen.checkstyle.addons.BaseCheckTestSupport;


/**
 * Unit tests for {@link LocationReferenceCheck}.
 */
public class LocationReferenceTest
    extends BaseCheckTestSupport
{
    public LocationReferenceTest()
    {
        setCheckShortname(LocationReferenceCheck.class);
    }



    @Test
    public void testMethodName()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(LocationReferenceCheck.class);
        checkConfig.addAttribute("methodCalls", "checkedCall1");
        checkConfig.addAttribute("variableNames", "checkedVar1, checkedVar1a");

        final String[] expected = {//
            "163:22: Value must reference the current method name, which is '<init>'", //
            "169:22: Value must reference the current method name, which is '<init>'", //
            "176:22: Value must reference the current method name, which is '<clinit>'", //
        };
        verify(checkConfig, getPath("misc/InputLocationReference.java"), expected);
    }



    @Test
    public void testClassObject()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(LocationReferenceCheck.class);
        checkConfig.addAttribute("methodCalls", "checkedCall2");
        checkConfig.addAttribute("variableNames", "checkedVar2, checkedVar2a, checkedVar2b");
        checkConfig.addAttribute("location", "classobject");

        final String[] expected = {//
            "25:39: Value must reference the simple class object of the current type, which is 'InputLocationReference"
                + ".class'", //
            "28:22: Value must reference the simple class object of the current type, which is "
                + "'InputLocationReference.class'", //
            "157:48: Value must reference the simple class object of the current type, which is "
                + "'InputLocationReference.class'", //
            "165:22: Value must reference the simple class object of the current type, which is "
                + "'InputLocationReference.class'", //
            "171:22: Value must reference the simple class object of the current type, which is "
                + "'InputLocationReference.class'", //
            "178:22: Value must reference the simple class object of the current type, which is "
                + "'InputLocationReference.class'", //
        };
        verify(checkConfig, getPath("misc/InputLocationReference.java"), expected);
    }



    @Test
    public void testSimpleClass()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(LocationReferenceCheck.class);
        checkConfig.addAttribute("methodCalls", "checkedCall3, checkedCall3a");
        checkConfig.addAttribute("variableNames", "checkedVar3, checkedVar3a, checkedVar3b, checkedVar3c");
        checkConfig.addAttribute("location", "simpleclass");

        final String[] expected = {//
            "36:37: Value must reference the simple name of the current type, which is 'InputLocationReference'", //
            "40:23: Value must reference the simple name of the current type, which is 'InputLocationReference'", //
            "158:48: Value must reference the simple name of the current type, which is 'InputLocationReference'", //
        };
        verify(checkConfig, getPath("misc/InputLocationReference.java"), expected);
    }



    @Test
    public void testFullClass()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(LocationReferenceCheck.class);
        checkConfig.addAttribute("methodCalls", "checkedCall4, checkedCall4a");
        checkConfig.addAttribute("variableNames", "checkedVar4, checkedVar4a, checkedVar4b, checkedVar4c");
        checkConfig.addAttribute("location", "fullclass");

        final String[] expected = {//
            "50:37: Value must reference the fully qualified name of the current type, which is 'com.foo"
                + ".InputLocationReference'", //
            "54:23: Value must reference the fully qualified name of the current type, which is 'com.foo"
                + ".InputLocationReference'", //
        };
        verify(checkConfig, getPath("misc/InputLocationReference.java"), expected);
    }



    @Test
    public void testUnconfigured1()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(LocationReferenceCheck.class);
        verify(checkConfig, getPath("misc/InputLocationReference.java"), new String[0]);
    }



    @Test
    public void testUnconfigured2()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(LocationReferenceCheck.class);
        checkConfig.addAttribute("methodCalls", "");
        checkConfig.addAttribute("variableNames", "");
        verify(checkConfig, getPath("misc/InputLocationReference.java"), new String[0]);
    }



    @Test
    public void testInnerMethodName()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(LocationReferenceCheck.class);
        checkConfig.addAttribute("methodCalls", "checkedCall5");
        checkConfig.addAttribute("variableNames", "checkedVar5, checkedVar5a");

        final String[] expected = {//
            "71:53: Value must reference the current method name, which is 'innerMethod'", //
            "73:38: Value must reference the current method name, which is 'innerMethod'", //
        };
        verify(checkConfig, getPath("misc/InputLocationReference.java"), expected);
    }



    @Test
    public void testInnerSimpleClass()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(LocationReferenceCheck.class);
        checkConfig.addAttribute("methodCalls", "checkedCall6");
        checkConfig.addAttribute("variableNames", "checkedVar6, checkedVar6a");
        checkConfig.addAttribute("location", "simpleclass");

        final String[] expected = {//
            "79:53: Value must reference the simple name of the current type, which is 'Inner4'", //
            "81:38: Value must reference the simple name of the current type, which is 'Inner4'", //
        };
        verify(checkConfig, getPath("misc/InputLocationReference.java"), expected);
    }



    @Test
    public void testInnerFullClass()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(LocationReferenceCheck.class);
        checkConfig.addAttribute("methodCalls", "checkedCall7");
        checkConfig.addAttribute("variableNames", "checkedVar7, checkedVar7a");
        checkConfig.addAttribute("location", "fullclass");

        final String[] expected = {//
            "87:53: Value must reference the fully qualified name of the current type, which is 'com.foo"
                + ".InputLocationReference.Inner1.Inner2.Inner3.Inner4'", //
            "89:38: Value must reference the fully qualified name of the current type, which is 'com.foo"
                + ".InputLocationReference.Inner1.Inner2.Inner3.Inner4'", //
        };
        verify(checkConfig, getPath("misc/InputLocationReference.java"), expected);
    }



    @Test
    public void testInnerClassObject()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(LocationReferenceCheck.class);
        checkConfig.addAttribute("methodCalls", "checkedCall8");
        checkConfig.addAttribute("variableNames", "checkedVar8, checkedVar8a");
        checkConfig.addAttribute("location", "classobject");

        final String[] expected = {//
            "95:60: Value must reference the simple class object of the current type, which is 'Inner4.class'", //
            "97:38: Value must reference the simple class object of the current type, which is 'Inner4.class'", //
        };
        verify(checkConfig, getPath("misc/InputLocationReference.java"), expected);
    }



    @Test
    public void testAnonymousClassMethod()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(LocationReferenceCheck.class);
        checkConfig.addAttribute("methodCalls", "checkedCall9");
        checkConfig.addAttribute("variableNames", "checkedVar9, checkedVar9a");
        checkConfig.addAttribute("location", "method");

        final String[] expected = {//
            "113:45: Value must reference the current method name, which is 'compare'", //
            "115:30: Value must reference the current method name, which is 'compare'", //
        };
        verify(checkConfig, getPath("misc/InputLocationReference.java"), expected);
    }



    @Test
    public void testAnonymousSimpleClass()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(LocationReferenceCheck.class);
        checkConfig.addAttribute("methodCalls", "checkedCall10");
        checkConfig.addAttribute("variableNames", "checkedVar10, checkedVar10a");
        checkConfig.addAttribute("location", "simpleclass");

        final String[] expected = {//
            "125:46: Value must reference the simple name of the current type, which is 'InputLocationReference'", //
            "127:31: Value must reference the simple name of the current type, which is 'InputLocationReference'", //
        };
        verify(checkConfig, getPath("misc/InputLocationReference.java"), expected);
    }



    @Test
    public void testArgPositions1()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(LocationReferenceCheck.class);
        checkConfig.addAttribute("methodCalls", "checkedCall11");
        checkConfig.addAttribute("argumentPosition", "-1");

        final String[] expected = {//
            "139:57: Value must reference the current method name, which is 'argPositionsMinus1'", //
        };
        verify(checkConfig, getPath("misc/InputLocationReference.java"), expected);
    }



    @Test
    public void testArgPositions2()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(LocationReferenceCheck.class);
        checkConfig.addAttribute("methodCalls", "checkedCall12");
        checkConfig.addAttribute("argumentPosition", "-2");

        final String[] expected = {//
            "145:51: Value must reference the current method name, which is 'argPositionsMinus2'", //
        };
        verify(checkConfig, getPath("misc/InputLocationReference.java"), expected);
    }



    @Test
    public void testArgPositions2a()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(LocationReferenceCheck.class);
        checkConfig.addAttribute("methodCalls", "checkedCall12");
        checkConfig.addAttribute("argumentPosition", "2");   // in a 4-element list, this must be the same as -2

        final String[] expected = {//
            "145:51: Value must reference the current method name, which is 'argPositionsMinus2'", //
        };
        verify(checkConfig, getPath("misc/InputLocationReference.java"), expected);
    }



    @Test
    public void testArgPositions3()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(LocationReferenceCheck.class);
        checkConfig.addAttribute("methodCalls", "checkedCall13");
        if (isJava6()) {
            checkConfig.addAttribute("argumentPosition", "1");
        }
        else {
            checkConfig.addAttribute("argumentPosition", "+1");
        }

        final String[] expected = {//
            "151:44: Value must reference the current method name, which is 'argPositionsPlus1'", //
        };
        verify(checkConfig, getPath("misc/InputLocationReference.java"), expected);
    }



    @Test
    public void testConstructor1()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(LocationReferenceCheck.class);
        checkConfig.addAttribute("methodCalls", "this");

        final String[] expected = {//
            "189:14: Value must reference the current method name, which is '<init>'", //
        };
        verify(checkConfig, getPath("misc/InputLocationReference.java"), expected);
    }



    @Test
    public void testConstructor2()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(LocationReferenceCheck.class);
        checkConfig.addAttribute("methodCalls", "this");
        checkConfig.addAttribute("location", "classobject");

        final String[] expected = {//
            "194:14: Value must reference the simple class object of the current type, which is "
                + "'InputLocationReference.class'", //
        };
        verify(checkConfig, getPath("misc/InputLocationReference.java"), expected);
    }



    @Test
    public void testQualifiedCall()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(LocationReferenceCheck.class);
        checkConfig.addAttribute("methodCalls", "Inner5.checkedCall15");
        checkConfig.addAttribute("location", "classobject");

        final String[] expected = {//
            "204:66: Value must reference the simple class object of the current type, which is "
                + "'InputLocationReference.class'", //
        };
        verify(checkConfig, getPath("misc/InputLocationReference.java"), expected);
    }



    @Test
    public void testFieldOnly()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(LocationReferenceCheck.class);
        checkConfig.addAttribute("variableNames", "field4");
        checkConfig.addAttribute("location", "classobject");

        verify(checkConfig, getPath("misc/InputLocationReference.java"), new String[0]);
    }
}
