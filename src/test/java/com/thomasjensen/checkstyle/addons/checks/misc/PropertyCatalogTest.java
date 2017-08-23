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

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import org.junit.Assert;
import org.junit.Test;

import com.thomasjensen.checkstyle.addons.BaseCheckTestSupport;
import com.thomasjensen.checkstyle.addons.checks.BinaryName;
import com.thomasjensen.checkstyle.addons.util.Util;


/**
 * Unit test of {@link PropertyCatalogCheck}.
 */
public class PropertyCatalogTest
    extends BaseCheckTestSupport
{
    public PropertyCatalogTest()
    {
        setCheckShortname(PropertyCatalogCheck.class);
    }



    @Test
    public void testPropertyFileTemplate()
        throws IOException
    {
        PropertyCatalogCheck check = new PropertyCatalogCheck(getPath("misc/InputPropertyCatalog1.java"));
        check.setPropertyFile("|{0}|{1}|{2}|{3}|{4}|{5}|{6}|{7}|{8}|{9}|{10}|{11}|{12}|");

        String s = check.buildPropertyFilePath(new BinaryName("com.foo", "Bar", "Inner"), 0, true);
        Assert.assertEquals("|com.foo.Bar$Inner|com/foo/Bar/Inner|com.foo.Bar|com/foo/Bar|../../..|com/foo|Bar|Inner|"
            + "src|test|resources|||", s);

        s = check.buildPropertyFilePath(new BinaryName("com.foo", "Bar", "Inner1", "Inner2"), 1, true);
        Assert.assertEquals(
            "|com.foo.Bar$Inner1$Inner2|com/foo/Bar/Inner1/Inner2|com.foo.Bar|com/foo/Bar|../../..|com/foo|Bar|Inner2|"
                + "src|test|resources|src/||", s);

        s = check.buildPropertyFilePath(new BinaryName("com.foo", "Bar"), 2, true);
        Assert.assertEquals("|com.foo.Bar|com/foo/Bar|com.foo.Bar|com/foo/Bar|../../..|com/foo|Bar|null|"
            + "src|test|resources|src/test/||", s);

        s = check.buildPropertyFilePath(new BinaryName(null, "Bar"), 3, true);
        s = s.replaceAll(Pattern.quote("\\"), "/");
        Assert.assertEquals("|Bar|Bar|Bar|Bar|..||Bar|null|src|test|resources|src/test/resources/|src/test"
            + "/resources/com/thomasjensen/checkstyle/addons/checks/misc|", s);
    }



    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testPropertyFileTemplateTooManyDirs()
        throws IOException
    {
        PropertyCatalogCheck check = new PropertyCatalogCheck(getPath("misc/InputPropertyCatalog1.java"));
        check.buildPropertyFilePath(new BinaryName(null, "Foo"), PropertyCatalogCheck.NUM_SUBDIRS + 1, true);
        Assert.fail("Expected exception was not thrown");
    }



    @Test
    public void testPropertyFileTemplateBasedir()
        throws IOException
    {
        PropertyCatalogCheck check = new PropertyCatalogCheck(getPath("misc/InputPropertyCatalog1.java"));
        check.setBaseDir("src");
        check.setPropertyFile("|{0}|{1}|{2}|{3}|{4}|{5}|{6}|{7}|{8}|{9}|{10}|{11}|{12}|");

        String s = check.buildPropertyFilePath(new BinaryName("com.foo", "Bar", "Inner"), 0, true);
        Assert.assertEquals("|com.foo.Bar$Inner|com/foo/Bar/Inner|com.foo.Bar|com/foo/Bar|../../..|com/foo|Bar|Inner|"
            + "test|resources|com|||", s);

        check.setBaseDir("src/test");   // forward slash
        s = check.buildPropertyFilePath(new BinaryName("com.foo", "Bar", "Inner"), 0, true);
        Assert.assertEquals("|com.foo.Bar$Inner|com/foo/Bar/Inner|com.foo.Bar|com/foo/Bar|../../..|com/foo|Bar|Inner|"
            + "resources|com|thomasjensen|||", s);

        check.setBaseDir("src\\test");   // backslash
        s = check.buildPropertyFilePath(new BinaryName("com.foo", "Bar", "Inner"), 0, true);
        Assert.assertEquals("|com.foo.Bar$Inner|com/foo/Bar/Inner|com.foo.Bar|com/foo/Bar|../../..|com/foo|Bar|Inner|"
            + "resources|com|thomasjensen|||", s);
    }



    @Test
    public void testPropertyFileTemplateParent()
        throws IOException
    {
        PropertyCatalogCheck check = new PropertyCatalogCheck(getPath("misc/InputPropertyCatalog1.java"));
        check.setPropertyFile("{8}");

        String s = check.buildPropertyFilePath(new BinaryName("com.foo", "Bar", "Inner"), 1, true);
        Assert.assertNotNull(s);
        Assert.assertTrue(s.length() > 0);
    }



    @Test
    public void testPropertyFileTemplateParentDynamic()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(PropertyCatalogCheck.class);
        checkConfig.addAttribute("selection", "foo\\.InputPropertyCatalog1");
        checkConfig.addAttribute("propertyFile",
            new File("{11}resources/com/thomasjensen/checkstyle/addons/checks/misc/{6}.properties").getCanonicalPath());
        // If {11} should be resolved as "src/test/", the file is found and we get no error.

        verify(checkConfig, getPath("misc/InputPropertyCatalog1.java"), new String[0]);
    }



    @Test
    public void testPropertyFileTemplateParentDynamicNonExistent()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(PropertyCatalogCheck.class);
        checkConfig.addAttribute("selection", "foo\\.InputPropertyCatalog1");
        checkConfig.addAttribute("propertyFile",
            new File("{11}resources/nonexistent/{6}.properties").getCanonicalPath());

        final String[] expected = {//
            "4:20: Could not load property file for catalog 'com.foo.InputPropertyCatalog1': " + new File(
                "{11}resources" + File.separator + "nonexistent" + File.separator + "InputPropertyCatalog1.properties")
                .getCanonicalPath() + ", where '{11}' was successively replaced with all leading fragments of "
                + "'src/test/resources/', including the empty String", //
        };
        verify(checkConfig, getPath("misc/InputPropertyCatalog1.java"), expected);
    }



    @Test
    public void testSunnyDayConstantsInt()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(PropertyCatalogCheck.class);
        checkConfig.addAttribute("selection", "foo\\.InputPropertyCatalog1");
        checkConfig.addAttribute("propertyFile", getPath("misc/{6}.properties"));

        verify(checkConfig, getPath("misc/InputPropertyCatalog1.java"), new String[0]);
    }



    @Test
    public void testSunnyDayConstantsString()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(PropertyCatalogCheck.class);
        checkConfig.addAttribute("selection", "foo\\.InputPropertyCatalog");
        checkConfig.addAttribute("propertyFile", getPath("misc/{6}.properties"));

        verify(checkConfig, getPath("misc/InputPropertyCatalog2.java"), new String[0]);
    }



    @Test
    public void testSunnyDayEnumConstants()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(PropertyCatalogCheck.class);
        checkConfig.addAttribute("selection", "foo\\.InputPropertyCatalog");
        checkConfig.addAttribute("propertyFile", getPath("misc/{6}.properties"));

        verify(checkConfig, getPath("misc/InputPropertyCatalog3.java"), new String[0]);
    }



    @Test
    public void testSunnyDayEnumConstantParamsIgnored()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(PropertyCatalogCheck.class);
        checkConfig.addAttribute("selection", "foo\\.InputPropertyCatalog");
        checkConfig.addAttribute("propertyFile", getPath("misc/InputPropertyCatalog3.properties"));

        verify(checkConfig, getPath("misc/InputPropertyCatalog4.java"), new String[0]);
    }



    @Test
    public void testEnumArgumentInt()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(PropertyCatalogCheck.class);
        checkConfig.addAttribute("selection", "foo\\.InputPropertyCatalog");
        checkConfig.addAttribute("propertyFile", getPath("misc/InputPropertyCatalog1.properties"));
        checkConfig.addAttribute("enumArgument", "true");

        verify(checkConfig, getPath("misc/InputPropertyCatalog4.java"), new String[0]);
    }



    @Test
    public void testEnumArgumentString()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(PropertyCatalogCheck.class);
        checkConfig.addAttribute("selection", "foo\\.InputPropertyCatalog");
        checkConfig.addAttribute("propertyFile", getPath("misc/InputPropertyCatalog2.properties"));
        checkConfig.addAttribute("enumArgument", "true");

        verify(checkConfig, getPath("misc/InputPropertyCatalog5.java"), new String[0]);
    }



    @Test
    public void testInterfaceConstants()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(PropertyCatalogCheck.class);
        checkConfig.addAttribute("selection", "foo\\.InputPropertyCatalog.$");
        checkConfig.addAttribute("propertyFile", getPath("misc/InputPropertyCatalog1.properties"));
        checkConfig.addAttribute("enumArgument", "true");  // ignored

        verify(checkConfig, getPath("misc/InputPropertyCatalog6.java"), new String[0]);
    }



    @Test
    public void testInnerClass()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(PropertyCatalogCheck.class);
        checkConfig.addAttribute("selection", "Catalog6\\$Foo");
        checkConfig.addAttribute("propertyFile", getPath("misc/InputPropertyCatalog1.properties"));

        verify(checkConfig, getPath("misc/InputPropertyCatalog6.java"), new String[0]);
    }



    @Test
    public void testPropertyFileNotFound()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(PropertyCatalogCheck.class);
        checkConfig.addAttribute("selection", "Catalog1$");
        checkConfig.addAttribute("propertyFile", "notfound");

        final String[] expected = {//
            "4:20: Could not load property file for catalog 'com.foo.InputPropertyCatalog1': " + new File("notfound")
                .getCanonicalPath(), //
        };
        verify(checkConfig, getPath("misc/InputPropertyCatalog1.java"), expected);
    }



    @Test
    public void testOrphaned1()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(PropertyCatalogCheck.class);
        checkConfig.addAttribute("selection", "Catalog6\\$Orphaned1");
        checkConfig.addAttribute("propertyFile", getPath("misc/InputPropertyCatalog1.properties"));

        final String[] expected = {//
            "21:25: Orphaned property '1' in file: " + new File(getPath("misc/InputPropertyCatalog1.properties"))
                .getCanonicalPath(), //
        };
        verify(checkConfig, getPath("misc/InputPropertyCatalog6.java"), expected);
    }



    @Test
    public void testOrphaned2()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(PropertyCatalogCheck.class);
        checkConfig.addAttribute("selection", "Catalog6\\$Orphaned2");
        checkConfig.addAttribute("propertyFile", getPath("misc/InputPropertyCatalog1.properties"));

        final String[] expected = {//
            "33:25: Orphaned properties [1, 2] in file: " + new File(getPath("misc/InputPropertyCatalog1.properties"))
                .getCanonicalPath(), //
        };
        verify(checkConfig, getPath("misc/InputPropertyCatalog6.java"), expected);
    }



    @Test
    public void testOrphansIgnored()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(PropertyCatalogCheck.class);
        checkConfig.addAttribute("selection", "Catalog6\\$Orphaned2");
        checkConfig.addAttribute("propertyFile", getPath("misc/InputPropertyCatalog1.properties"));
        checkConfig.addAttribute("reportOrphans", "false");

        verify(checkConfig, getPath("misc/InputPropertyCatalog6.java"), new String[0]);
    }



    @Test
    public void testMissing1()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(PropertyCatalogCheck.class);
        checkConfig.addAttribute("selection", "Catalog6$");
        checkConfig.addAttribute("propertyFile", getPath("misc/InputPropertyCatalog1-missing1.properties"));

        final String[] expected = {//
            "8:16: Catalog entry 'KEY2' refers to missing property '1' in file: " + new File(
                getPath("misc/InputPropertyCatalog1-missing1.properties")).getCanonicalPath(), //
        };
        verify(checkConfig, getPath("misc/InputPropertyCatalog6.java"), expected);
    }



    @Test
    public void testMissing2()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(PropertyCatalogCheck.class);
        checkConfig.addAttribute("selection", "Catalog6$");
        checkConfig.addAttribute("propertyFile", getPath("misc/InputPropertyCatalog1-missing2.properties"));

        final String expectedFilePath = new File(getPath("misc/InputPropertyCatalog1-missing2.properties"))
            .getCanonicalPath();
        final String[] expected = {//
            "8:16: Catalog entry 'KEY2' refers to missing property '1' in file: " + expectedFilePath,
            "29:16: Catalog entry 'KEY3' refers to missing property '2' in file: " + expectedFilePath, //
        };
        verify(checkConfig, getPath("misc/InputPropertyCatalog6.java"), expected);
    }



    @Test
    public void testDuplicateProperty()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(PropertyCatalogCheck.class);
        checkConfig.addAttribute("selection", "Catalog7Dup$");
        checkConfig.addAttribute("propertyFile", getPath("misc/InputPropertyCatalog1-missing1.properties"));

        final String[] expected = {"8:39: Catalog entry 'KEY3' refers to the same property as 'KEY2' on line 7"};
        verify(checkConfig, getPath("misc/InputPropertyCatalog7Dup.java"), expected);
    }



    @Test
    public void testDuplicatePropertySuppressed()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(PropertyCatalogCheck.class);
        checkConfig.addAttribute("selection", ".");
        checkConfig.addAttribute("propertyFile", getPath("misc/InputPropertyCatalog1-missing1.properties"));
        checkConfig.addAttribute("reportDuplicates", "false");

        verify(checkConfig, getPath("misc/InputPropertyCatalog7Dup.java"), new String[0]);
    }



    @Test
    public void testDuplicatePropertyDouble()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(PropertyCatalogCheck.class);
        checkConfig.addAttribute("selection", ".");
        checkConfig.addAttribute("propertyFile", getPath("misc/InputPropertyCatalog1-missing2.properties"));

        final String[] expected = {//
            "7:36: Catalog entry 'KEY2' refers to the same property as 'KEY1' on line 6",
            "8:39: Catalog entry 'KEY3' refers to the same property as 'KEY1' on line 6",  //
        };
        verify(checkConfig, getPath("misc/InputPropertyCatalog8Dup.java"), expected);
    }



    @Test
    public void testExclusionOfFields()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(PropertyCatalogCheck.class);
        checkConfig.addAttribute("selection", ".");
        checkConfig.addAttribute("propertyFile", getPath("misc/InputPropertyCatalog1.properties"));
        checkConfig.addAttribute("excludedFields", "(?:LOG|^EXCLUDE_.*)");

        verify(checkConfig, getPath("misc/InputPropertyCatalog9Excl.java"), new String[0]);
    }



    @Test
    public void testUnclearConstants()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(PropertyCatalogCheck.class);
        checkConfig.addAttribute("selection", ".");
        checkConfig.addAttribute("propertyFile", getPath("misc/InputPropertyCatalog1-missing2.properties"));

        final String[] expected =
            {"8:29: Keys in a property catalog must be simple literals of type String, int, long, or boolean.",
                "10:29: Keys in a property catalog must be simple literals of type String, int, long, or boolean.",
                "12:32: Keys in a property catalog must be simple literals of type String, int, long, or boolean.",
                "14:32: Keys in a property catalog must be simple literals of type String, int, long, or boolean."};
        verify(checkConfig, getPath("misc/InputPropertyCatalog10Unclear.java"), expected);
    }



    @Test
    public void testUnclearEnumParam()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(PropertyCatalogCheck.class);
        checkConfig.addAttribute("selection", ".");
        checkConfig.addAttribute("enumArgument", "true");
        checkConfig.addAttribute("propertyFile", getPath("misc/InputPropertyCatalog1-missing2.properties"));

        final String[] expected =
            {"8:5: Enum constant of the property catalog is not parameterized, or the first parameter of the "
                + "enum constant's constructor is not a simple literal of type String, int, long, or boolean.",
                "10:5: Enum constant of the property catalog is not parameterized, or the first parameter of the "
                    + "enum constant's constructor is not a simple literal of type String, int, long, or boolean.",
                "12:5: Enum constant of the property catalog is not parameterized, or the first parameter of the "
                    + "enum constant's constructor is not a simple literal of type String, int, long, or boolean.",
                "14:5: Enum constant of the property catalog is not parameterized, or the first parameter of the "
                    + "enum constant's constructor is not a simple literal of type String, int, long, or boolean.",
                "16:5: Enum constant of the property catalog is not parameterized, or the first parameter of the "
                    + "enum constant's constructor is not a simple literal of type String, int, long, or boolean."};
        verify(checkConfig, getPath("misc/InputPropertyCatalog11Unclear.java"), expected);
    }



    @Test
    public void testCaseSensitivity()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(PropertyCatalogCheck.class);
        checkConfig.addAttribute("selection", ".");
        checkConfig.addAttribute("propertyFile", getPath("misc/InputPropertyCatalog2.properties"));
        checkConfig.addAttribute("caseSensitive", "true");

        final String expectedFilePath = new File(getPath("misc/InputPropertyCatalog2.properties")).getCanonicalPath();
        final String[] expected = {//
            "4:20: Orphaned properties [one, two, zero] in file: " + expectedFilePath,
            "6:39: Catalog entry 'KEY1' refers to missing property 'ZERO' in file: " + expectedFilePath,
            "8:39: Catalog entry 'KEY2' refers to missing property 'oNe' in file: " + expectedFilePath,
            "10:39: Catalog entry 'KEY3' refers to missing property 'Two' in file: " + expectedFilePath, //
        };
        verify(checkConfig, getPath("misc/InputPropertyCatalog12Case.java"), expected);

        final DefaultConfiguration checkConfigNoCase = createCheckConfig(PropertyCatalogCheck.class);
        checkConfigNoCase.addAttribute("selection", ".");
        checkConfigNoCase.addAttribute("propertyFile", getPath("misc/InputPropertyCatalog2.properties"));
        checkConfigNoCase.addAttribute("caseSensitive", "false");
        verify(checkConfigNoCase, getPath("misc/InputPropertyCatalog12Case.java"), new String[0]);
    }



    @Test
    public void testCaseSensitivity2()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(PropertyCatalogCheck.class);
        checkConfig.addAttribute("selection", ".");
        checkConfig.addAttribute("propertyFile", getPath("misc/InputPropertyCatalog12.properties"));
        checkConfig.addAttribute("caseSensitive", "true");

        final String expectedFilePath = new File(getPath("misc/InputPropertyCatalog12.properties")).getCanonicalPath();
        final String[] expected = {//
            "6:20: Orphaned properties [Two, ZERO, oNe] in file: " + expectedFilePath,
            "8:39: Catalog entry 'KEY1' refers to missing property 'zero' in file: " + expectedFilePath,
            "9:39: Catalog entry 'KEY2' refers to missing property 'one' in file: " + expectedFilePath,
            "11:39: Catalog entry 'KEY3' refers to missing property 'two' in file: " + expectedFilePath,  //
        };
        verify(checkConfig, getPath("misc/InputPropertyCatalog2.java"), expected);

        final DefaultConfiguration checkConfigNoCase = createCheckConfig(PropertyCatalogCheck.class);
        checkConfigNoCase.addAttribute("selection", ".");
        checkConfigNoCase.addAttribute("propertyFile", getPath("misc/InputPropertyCatalog12.properties"));
        checkConfigNoCase.addAttribute("caseSensitive", "false");
        verify(checkConfigNoCase, getPath("misc/InputPropertyCatalog2.java"), new String[0]);
    }



    @Test
    public void testCaseSensitivity3Ok()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(PropertyCatalogCheck.class);
        checkConfig.addAttribute("selection", ".");
        checkConfig.addAttribute("propertyFile", getPath("misc/InputPropertyCatalog13.properties"));
        checkConfig.addAttribute("caseSensitive", "true");

        verify(checkConfig, getPath("misc/InputPropertyCatalog13Case.java"), new String[0]);
    }



    @Test
    public void testCaseSensitivity3Duplicate()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(PropertyCatalogCheck.class);
        checkConfig.addAttribute("selection", ".");
        checkConfig.addAttribute("propertyFile", getPath("misc/InputPropertyCatalog13.properties"));
        checkConfig.addAttribute("caseSensitive", "false");

        String[] expected = {"8:39: Catalog entry 'KEY2' refers to the same property as 'KEY1' on line 6"};
        verify(checkConfig, getPath("misc/InputPropertyCatalog13Case.java"), expected);
    }



    @Test
    public void testMissing14Missing()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(PropertyCatalogCheck.class);
        checkConfig.addAttribute("selection", ".");
        checkConfig.addAttribute("propertyFile", getPath("misc/InputPropertyCatalog14-missing.properties"));

        final String expectedFilePath = new File(getPath("misc/InputPropertyCatalog14-missing.properties"))
            .getCanonicalPath();
        final String[] expected = {"6:11: Property 'one' not found in file: " + expectedFilePath};
        verify(checkConfig, getPath("misc/InputPropertyCatalog14Missing.java"), expected);
    }



    @Test
    public void testMissing15EncodingUtf8()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(PropertyCatalogCheck.class);
        checkConfig.addAttribute("selection", ".");
        checkConfig.addAttribute("propertyFile", getPath("misc/InputPropertyCatalog15-UTF8.properties"));
        verify(checkConfig, getPath("misc/InputPropertyCatalog15Encoding.java"), new String[0]);
    }



    @Test
    public void testMissing15EncodingIso8859()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(PropertyCatalogCheck.class);
        checkConfig.addAttribute("selection", ".");
        checkConfig.addAttribute("propertyFile", getPath("misc/InputPropertyCatalog15-iso8859.properties"));
        checkConfig.addAttribute("propertyFileEncoding", "ISO-8859-1");
        verify(checkConfig, getPath("misc/InputPropertyCatalog15Encoding.java"), new String[0]);
    }



    /**
     * In an Enum, only the Enum constants can be used for keys. Additional regular constants defined in the Enum class
     * must be ignored.
     *
     * @throws Exception if it didn't work
     */
    @Test
    public void testMissing16EnumConstantsMixed()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(PropertyCatalogCheck.class);
        checkConfig.addAttribute("selection", ".");
        checkConfig.addAttribute("propertyFile", getPath("misc/InputPropertyCatalog3.properties"));

        verify(checkConfig, getPath("misc/InputPropertyCatalog16EnumConstantsMixed.java"), new String[0]);
    }



    /**
     * If the class is in the default package, there is not 'package' element in the AST, which must be not problem.
     *
     * @throws Exception test failed
     */
    @Test
    public void testMissing17DefaultPackage()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(PropertyCatalogCheck.class);
        checkConfig.addAttribute("selection", ".");
        checkConfig.addAttribute("propertyFile", getPath("misc/InputPropertyCatalog1.properties"));

        verify(checkConfig, getPath("misc/InputPropertyCatalog17DefaultPackage.java"), new String[0]);
    }



    @Test
    public void testMultipleSourceSets()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(PropertyCatalogCheck.class);
        final File baseDir = Util.canonize(
            new File(new File(getPath("misc/InputPropertyCatalog1.properties")).getParentFile(), "PropertyCatalog"));
        checkConfig.addAttribute("baseDir", baseDir.getAbsolutePath());
        checkConfig.addAttribute("selection", "Messages$");
        checkConfig.addAttribute("propertyFile", "{12}/../resources/{1}.properties");

        final File[] filesToCheck = new File[]{//
            new File(getPath("misc/PropertyCatalog/subsys/module/src/it/java/com/foo/FooMessages.java")),  //
            new File(getPath("misc/PropertyCatalog/subsys/module/src/main/java/com/foo/BarMessages.java")) //
        };
        verify(createChecker(checkConfig), filesToCheck, "doesNotMatter", new String[0]);
    }



    @Test
    public void testFileExclusion()
        throws Exception
    {
        final DefaultConfiguration checkConfig = createCheckConfig(PropertyCatalogCheck.class);
        checkConfig.addAttribute("selection", ".");
        checkConfig.addAttribute("fileExludes", "InputPropertyCatalog14Missing");
        checkConfig.addAttribute("propertyFile", getPath("misc/InputPropertyCatalog14-missing.properties"));

        final File[] filesToCheck = new File[]{//
            new File(getPath("misc/InputPropertyCatalog14.java")), //
            new File(getPath("misc/InputPropertyCatalog14Missing.java")),   // has the error, but suppressed
        };
        verify(createChecker(checkConfig), filesToCheck, "doesNotMatter", new String[0]);
    }
}
