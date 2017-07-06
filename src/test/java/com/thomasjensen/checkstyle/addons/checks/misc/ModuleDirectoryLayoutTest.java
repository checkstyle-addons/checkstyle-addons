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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.thomasjensen.checkstyle.addons.BaseFileSetCheckTestSupport;
import com.thomasjensen.checkstyle.addons.util.Util;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * Unit tests of {@link ModuleDirectoryLayoutCheck}.
 */
public class ModuleDirectoryLayoutTest
    extends BaseFileSetCheckTestSupport
{
    private DefaultConfiguration mCheckConfig;



    public ModuleDirectoryLayoutTest()
    {
        setCheckShortname(ModuleDirectoryLayoutCheck.class);
    }



    @Before
    public void setUp()
    {
        mCheckConfig = createCheckConfig(ModuleDirectoryLayoutCheck.class);
    }



    @Test
    @SuppressFBWarnings(value = "DMI_HARDCODED_ABSOLUTE_FILENAME", justification = "These are fictional absolute paths")
    public void testDecomposePath1()
        throws IOException
    {
        ModuleDirectoryLayoutCheck check = new ModuleDirectoryLayoutCheck();
        check.setBaseDir("D:/Projects/project1");
        check.setConfigFile(getPath("misc/ModuleDirectoryLayout/directories-multi.json"));

        final String path = "D:/Projects/project1/group/module1/src/test/resources/foo/bar/Filename.tar.gz";
        DecomposedPath dcp = check.decomposePath(check.getMdlConfig(), Util.canonize(new File(path)).getPath());

        Assert.assertNotNull(dcp);
        Assert.assertEquals("group" + File.separator + "module1", dcp.getModulePath());
        Assert.assertEquals("src/test/resources", dcp.getMdlPath());
        Assert.assertEquals("foo" + File.separator + "bar" + File.separator + "Filename.tar.gz", dcp.getSpecificPath());
        Assert.assertEquals("Filename.tar.gz", dcp.getSimpleFilename());

        Set<String> expectedExtensions = new HashSet<String>();
        expectedExtensions.add("tar.gz");
        expectedExtensions.add("gz");
        Assert.assertEquals(expectedExtensions, dcp.getFileExtensions());

        List<String> expectedSpecificFolders = new ArrayList<String>();
        expectedSpecificFolders.add("foo");
        expectedSpecificFolders.add("bar");
        Assert.assertEquals(expectedSpecificFolders, dcp.getSpecificFolders());
    }



    @Test
    @SuppressFBWarnings(value = "DMI_HARDCODED_ABSOLUTE_FILENAME", justification = "These are fictional absolute paths")
    public void testDecomposePathNoExtension()
        throws IOException
    {
        ModuleDirectoryLayoutCheck check = new ModuleDirectoryLayoutCheck();
        check.setBaseDir("D:/Projects/project1");
        check.setConfigFile(getPath("misc/ModuleDirectoryLayout/directories-multi.json"));

        final String path = "D:/Projects/project1/group/module1/src/test/resources/foo/bar/file";
        DecomposedPath dcp = check.decomposePath(check.getMdlConfig(), Util.canonize(new File(path)).getPath());

        Assert.assertNotNull(dcp);
        Assert.assertEquals("group" + File.separator + "module1", dcp.getModulePath());
        Assert.assertEquals("src/test/resources", dcp.getMdlPath());
        Assert.assertEquals("foo" + File.separator + "bar" + File.separator + "file", dcp.getSpecificPath());
        Assert.assertEquals("file", dcp.getSimpleFilename());

        Assert.assertEquals(Collections.emptySet(), dcp.getFileExtensions());

        List<String> expectedSpecificFolders = new ArrayList<String>();
        expectedSpecificFolders.add("foo");
        expectedSpecificFolders.add("bar");
        Assert.assertEquals(expectedSpecificFolders, dcp.getSpecificFolders());
    }



    @Test
    @SuppressFBWarnings(value = "DMI_HARDCODED_ABSOLUTE_FILENAME", justification = "These are fictional absolute paths")
    public void testDecomposePathExtensionLeadingDot()
        throws IOException
    {
        ModuleDirectoryLayoutCheck check = new ModuleDirectoryLayoutCheck();
        check.setBaseDir("D:/Projects/project1");
        check.setConfigFile(getPath("misc/ModuleDirectoryLayout/directories-multi.json"));

        final String path = "D:/Projects/project1/group/module1/src/test/resources/foo/bar/.gitignore";
        DecomposedPath dcp = check.decomposePath(check.getMdlConfig(), Util.canonize(new File(path)).getPath());

        Assert.assertNotNull(dcp);
        Assert.assertEquals("group" + File.separator + "module1", dcp.getModulePath());
        Assert.assertEquals("src/test/resources", dcp.getMdlPath());
        Assert.assertEquals("foo" + File.separator + "bar" + File.separator + ".gitignore", dcp.getSpecificPath());
        Assert.assertEquals(".gitignore", dcp.getSimpleFilename());

        Set<String> expectedExtensions = new HashSet<String>();
        expectedExtensions.add("gitignore");
        Assert.assertEquals(expectedExtensions, dcp.getFileExtensions());

        List<String> expectedSpecificFolders = new ArrayList<String>();
        expectedSpecificFolders.add("foo");
        expectedSpecificFolders.add("bar");
        Assert.assertEquals(expectedSpecificFolders, dcp.getSpecificFolders());
    }



    @Test
    @SuppressFBWarnings(value = "DMI_HARDCODED_ABSOLUTE_FILENAME", justification = "These are fictional absolute paths")
    public void testDecomposePathExtensionLeadingDoubleDot()
        throws IOException
    {
        ModuleDirectoryLayoutCheck check = new ModuleDirectoryLayoutCheck();
        check.setBaseDir("D:/Projects/project1");
        check.setConfigFile(getPath("misc/ModuleDirectoryLayout/directories-multi.json"));

        final String path = "D:/Projects/project1/group/module1/src/test/resources/foo/bar/file..txt";
        DecomposedPath dcp = check.decomposePath(check.getMdlConfig(), Util.canonize(new File(path)).getPath());

        Assert.assertNotNull(dcp);
        Assert.assertEquals("group" + File.separator + "module1", dcp.getModulePath());
        Assert.assertEquals("src/test/resources", dcp.getMdlPath());
        Assert.assertEquals("foo" + File.separator + "bar" + File.separator + "file..txt", dcp.getSpecificPath());
        Assert.assertEquals("file..txt", dcp.getSimpleFilename());

        Set<String> expectedExtensions = new HashSet<String>();
        expectedExtensions.add(".txt");  // extra dot!
        Assert.assertEquals(expectedExtensions, dcp.getFileExtensions());

        List<String> expectedSpecificFolders = new ArrayList<String>();
        expectedSpecificFolders.add("foo");
        expectedSpecificFolders.add("bar");
        Assert.assertEquals(expectedSpecificFolders, dcp.getSpecificFolders());
    }



    @Test
    @SuppressFBWarnings(value = "DMI_HARDCODED_ABSOLUTE_FILENAME", justification = "These are fictional absolute paths")
    public void testDecomposePathExtensionEmbeddedDoubleDot()
        throws IOException
    {
        ModuleDirectoryLayoutCheck check = new ModuleDirectoryLayoutCheck();
        check.setBaseDir("D:/Projects/project1");
        check.setConfigFile(getPath("misc/ModuleDirectoryLayout/directories-multi.json"));

        final String path = "D:/Projects/project1/group/module1/src/test/resources/foo/bar/file.tar..gz";
        DecomposedPath dcp = check.decomposePath(check.getMdlConfig(), Util.canonize(new File(path)).getPath());

        Assert.assertNotNull(dcp);
        Assert.assertEquals("group" + File.separator + "module1", dcp.getModulePath());
        Assert.assertEquals("src/test/resources", dcp.getMdlPath());
        Assert.assertEquals("foo" + File.separator + "bar" + File.separator + "file.tar..gz", dcp.getSpecificPath());
        Assert.assertEquals("file.tar..gz", dcp.getSimpleFilename());

        Set<String> expectedExtensions = new HashSet<String>();
        expectedExtensions.add("tar..gz");
        expectedExtensions.add(".gz");
        expectedExtensions.add("gz");
        Assert.assertEquals(expectedExtensions, dcp.getFileExtensions());

        List<String> expectedSpecificFolders = new ArrayList<String>();
        expectedSpecificFolders.add("foo");
        expectedSpecificFolders.add("bar");
        Assert.assertEquals(expectedSpecificFolders, dcp.getSpecificFolders());
    }



    @Test
    @SuppressFBWarnings(value = "DMI_HARDCODED_ABSOLUTE_FILENAME", justification = "These are fictional absolute paths")
    public void testDecomposePathWrongBasedir()
        throws IOException
    {
        ModuleDirectoryLayoutCheck check = new ModuleDirectoryLayoutCheck();
        check.setBaseDir("D:/wrong/path");
        check.setConfigFile(getPath("misc/ModuleDirectoryLayout/directories-multi.json"));

        final String path = "D:/Projects/project1/group/module1/src/test/resources/foo/bar/Filename.tar.gz";
        DecomposedPath dcp = check.decomposePath(check.getMdlConfig(), Util.canonize(new File(path)).getPath());

        Assert.assertNull(dcp);
    }



    @Test
    @SuppressFBWarnings(value = "DMI_HARDCODED_ABSOLUTE_FILENAME", justification = "These are fictional absolute paths")
    public void testDecomposePathFileInModuleRoot()
        throws IOException
    {
        ModuleDirectoryLayoutCheck check = new ModuleDirectoryLayoutCheck();
        check.setBaseDir("D:/Projects/project1");
        check.setConfigFile(getPath("misc/ModuleDirectoryLayout/directories-multi.json"));

        final String path = "D:/Projects/project1/group/module1/file.txt";
        DecomposedPath dcp = check.decomposePath(check.getMdlConfig(), Util.canonize(new File(path)).getPath());

        Assert.assertNull(dcp);
    }



    @Test
    public void testDecomposePathUnknownModule()
        throws Exception
    {
        mCheckConfig.addAttribute("configFile", getPath("misc/ModuleDirectoryLayout/directories-multi.json"));

        // moduleRegex in the JSON file does not match our single-module scenario, so the module cannot be determined
        final String[] expected = {"0: Module association could not be determined for file: " + Util.standardizeSlashes(
            "src/test/resources/com/thomasjensen/checkstyle/addons/checks/misc/ModuleDirectoryLayout/"
                + "InputModuleDirectoryLayout-empty.txt") + " - moduleRegex: ^.+?[\\\\/]module\\d+"};

        final String filepath = getPath("misc/ModuleDirectoryLayout/InputModuleDirectoryLayout-empty.txt");
        verify(mCheckConfig, filepath, expected);
    }



    @Test
    public void testSunnyDay()
        throws Exception
    {
        mCheckConfig.addAttribute("configFile", getPath("misc/ModuleDirectoryLayout/directories.json"));

        final String filepath = getPath("misc/ModuleDirectoryLayout/InputModuleDirectoryLayout-empty.txt");
        verify(mCheckConfig, filepath, new String[0]);
    }



    @Test
    public void testFileInBaseDir()
        throws Exception
    {
        mCheckConfig.addAttribute("baseDir", getPath("misc/ModuleDirectoryLayout/default"));
        mCheckConfig.addAttribute("configFile", new File(
            "src/main/resources/com/thomasjensen/checkstyle/addons/checks/misc/ModuleDirectoryLayout-default.json")
            .getCanonicalPath());

        final String filepath = getPath("misc/ModuleDirectoryLayout/default/file.txt");
        verify(mCheckConfig, filepath, new String[0]);
    }



    @Test
    public void testNestedSrcFolder()
        throws Exception
    {
        mCheckConfig.addAttribute("configFile", getPath("misc/ModuleDirectoryLayout/directories.json"));

        // moduleRegex in the JSON file does not match our single-module scenario, so the module cannot be determined
        final String[] expected = {"0: 'src' may not be used as package name or subfolder: " + Util.standardizeSlashes(
            "com/thomasjensen/checkstyle/addons/checks/misc/ModuleDirectoryLayout/scenario1/src/NestedSrcFolder.txt")};

        final String filepath = getPath("misc/ModuleDirectoryLayout/scenario1/src/NestedSrcFolder.txt");
        verify(mCheckConfig, filepath, expected);
    }



    @Test
    public void testMdlNotAllowedInModule()
        throws Exception
    {
        mCheckConfig.addAttribute("baseDir", getPath("misc/ModuleDirectoryLayout/scenario2"));
        mCheckConfig.addAttribute("configFile", getPath("misc/ModuleDirectoryLayout/directories-scenario2.json"));

        // because we configured it to be valid only in 'module-x'
        final String[] expected = {"0: Source folder 'src/main/webapp' is not allowed in module 'module1'"};

        final String filepath = getPath("misc/ModuleDirectoryLayout/scenario2/module1/src/main/webapp/file.txt");
        verify(mCheckConfig, filepath, expected);
    }



    @Test
    public void testMdlNotAllowedInModule2()
        throws Exception
    {
        mCheckConfig.addAttribute("baseDir", getPath("misc/ModuleDirectoryLayout/scenario3"));
        mCheckConfig.addAttribute("configFile", getPath("misc/ModuleDirectoryLayout/directories-scenario3.json"));

        // Scenario 3 defines no modules, so the module restriction placed upon src/main/webapp does not hold
        final String filepath = getPath("misc/ModuleDirectoryLayout/scenario3/src/main/webapp/file.txt");
        verify(mCheckConfig, filepath, new String[0]);
    }



    @Test
    public void testUnknownMdlPath()
        throws Exception
    {
        mCheckConfig.addAttribute("baseDir", getPath("misc/ModuleDirectoryLayout/default"));
        // no configFile property -> use built-in default

        final String[] expected = {"0: File resides in a non-standard source folder: " + Util.standardizeSlashes(
            "src/main/whitespace/file.ws")};

        final String filepath = getPath("misc/ModuleDirectoryLayout/default/src/main/whitespace/file.ws");
        verify(mCheckConfig, filepath, expected);
    }



    @Test
    public void testIllegalContent1()
        throws Exception
    {
        mCheckConfig.addAttribute("baseDir", getPath("misc/ModuleDirectoryLayout/default"));
        mCheckConfig.addAttribute("configFile", new File(
            "src/main/resources/com/thomasjensen/checkstyle/addons/checks/misc/ModuleDirectoryLayout-default.json")
            .getCanonicalPath());

        final String[] expected = {"0: File is not accepted content of src/main/java: " + Util.standardizeSlashes(
            "a/b/illegal.txt")};

        final String filepath = getPath("misc/ModuleDirectoryLayout/default/src/main/java/a/b/illegal.txt");
        verify(mCheckConfig, filepath, expected);
    }



    @Test
    public void testIllegalContent2()
        throws Exception
    {
        mCheckConfig.addAttribute("baseDir", getPath("misc/ModuleDirectoryLayout/default"));
        mCheckConfig.addAttribute("configFile", new File(
            "src/main/resources/com/thomasjensen/checkstyle/addons/checks/misc/ModuleDirectoryLayout-default.json")
            .getCanonicalPath());

        // .java is ok as a file extension, but the containing META-INF folder is on the deny list
        final String[] expected = {"0: File is not accepted content of src/main/java: " + Util.standardizeSlashes(
            "a/META-INF/A.java")};

        final String filepath = getPath("misc/ModuleDirectoryLayout/default/src/main/java/a/META-INF/A.java");
        verify(mCheckConfig, filepath, expected);
    }



    @Test
    public void testIllegalContent3()
        throws Exception
    {
        mCheckConfig.addAttribute("baseDir", getPath("misc/ModuleDirectoryLayout/default"));
        mCheckConfig.addAttribute("configFile", new File(
            "src/main/resources/com/thomasjensen/checkstyle/addons/checks/misc/ModuleDirectoryLayout-default.json")
            .getCanonicalPath());

        final String[] expected = {"0: File is not accepted content of src/main/resources: " + Util.standardizeSlashes(
            "META-INF/META-INF/file.txt")};

        final String filepath = getPath(
            "misc/ModuleDirectoryLayout/default/src/main/resources/META-INF/META-INF/file.txt");
        verify(mCheckConfig, filepath, expected);
    }



    @Test
    public void testGoodContent1()
        throws Exception
    {
        mCheckConfig.addAttribute("baseDir", getPath("misc/ModuleDirectoryLayout/default"));
        mCheckConfig.addAttribute("configFile", new File(
            "src/main/resources/com/thomasjensen/checkstyle/addons/checks/misc/ModuleDirectoryLayout-default.json")
            .getCanonicalPath());

        final String filepath = getPath("misc/ModuleDirectoryLayout/default/src/main/resources/META-INF/file.txt");
        verify(mCheckConfig, filepath, new String[0]);
    }



    @Test
    public void testIllegalContent4()
        throws Exception
    {
        mCheckConfig.addAttribute("baseDir", getPath("misc/ModuleDirectoryLayout/default"));
        mCheckConfig.addAttribute("configFile", new File(
            "src/main/resources/com/thomasjensen/checkstyle/addons/checks/misc/ModuleDirectoryLayout-default.json")
            .getCanonicalPath());

        final String[] expected = {"0: File is not accepted content of src/main/resources: " + Util.standardizeSlashes(
            "a/META-INF/file.txt")};

        final String filepath = getPath("misc/ModuleDirectoryLayout/default/src/main/resources/a/META-INF/file.txt");
        verify(mCheckConfig, filepath, expected);
    }



    @Test
    public void testGoodContent2()
        throws Exception
    {
        mCheckConfig.addAttribute("baseDir", getPath("misc/ModuleDirectoryLayout/default"));
        mCheckConfig.addAttribute("configFile", getPath("misc/ModuleDirectoryLayout/directories-scenario4.json"));

        // Two top-level folders defined in whitelist, our file is in one of them
        final String filepath = getPath("misc/ModuleDirectoryLayout/default/src/main/resources/META-INF/file.txt");
        verify(mCheckConfig, filepath, new String[0]);
    }



    @Test
    public void testGoodContent2a()
        throws Exception
    {
        mCheckConfig.addAttribute("baseDir", getPath("misc/ModuleDirectoryLayout/default"));
        mCheckConfig.addAttribute("configFile", getPath("misc/ModuleDirectoryLayout/directories-scenario4.json"));

        // Two top-level folders defined in whitelist, our file is in one of them
        final String filepath = getPath("misc/ModuleDirectoryLayout/default/src/main/resources/a/file.txt");
        verify(mCheckConfig, filepath, new String[0]);
    }



    @Test
    public void testGoodContent2b()
        throws Exception
    {
        mCheckConfig.addAttribute("baseDir", getPath("misc/ModuleDirectoryLayout/default"));
        mCheckConfig.addAttribute("configFile", getPath("misc/ModuleDirectoryLayout/directories-scenario6.json"));

        final String filepath = getPath("misc/ModuleDirectoryLayout/default/src/main/resources/a/file.txt");
        verify(mCheckConfig, filepath, new String[0]);
    }



    @Test
    public void testGoodContent3()
        throws Exception
    {
        mCheckConfig.addAttribute("baseDir", getPath("misc/ModuleDirectoryLayout/default"));
        mCheckConfig.addAttribute("configFile", new File(
            "src/main/resources/com/thomasjensen/checkstyle/addons/checks/misc/ModuleDirectoryLayout-default.json")
            .getCanonicalPath());

        final String filepath = getPath("misc/ModuleDirectoryLayout/default/src/main/java/a/b/file.java");
        verify(mCheckConfig, filepath, new String[0]);
    }



    @Test
    public void testBrokenConfigs()
        throws Exception
    {
        for (int i = 1; i <= 18; i++) {
            setUp();
            mCheckConfig.addAttribute("baseDir", getPath("misc/ModuleDirectoryLayout/default"));
            mCheckConfig.addAttribute("configFile",
                getPath("misc/ModuleDirectoryLayout/directories-broken" + i + ".json"));
            mCheckConfig.addAttribute("failQuietly", "true");  // flag must not have any effect!

            Throwable expectedException = null;
            try {
                final String filepath = getPath("misc/ModuleDirectoryLayout/default/src/main/java/a/b/illegal.txt");
                verify(mCheckConfig, filepath, new String[0]);
                Assert.fail("expected exception was not thrown");
            }
            catch (IllegalArgumentException e) {
                // expected - Versions of Checkstyle prior to 6.12 give the expected exception directly
                expectedException = e;
            }
            catch (CheckstyleException e) {
                // expected - Checkstyle 6.12 and later wrap the exception in a CheckstyleException
                expectedException = e.getCause();
            }
            Assert.assertTrue(expectedException instanceof IllegalArgumentException);
            Assert.assertTrue(expectedException.getMessage().contains(//
                "Could not read or parse the module directory layout configFile") //
                || expectedException.getMessage().contains(//
                "Module directory layout configFile contains invalid configuration"));
        }
    }



    @Test
    public void testDenyBySpecificPathRegex()
        throws Exception
    {
        mCheckConfig.addAttribute("baseDir", getPath("misc/ModuleDirectoryLayout/default"));
        mCheckConfig.addAttribute("configFile", getPath("misc/ModuleDirectoryLayout/directories-scenario5.json"));

        final String[] expected = {"0: File is not accepted content of src/main/java: " + Util.standardizeSlashes(
            "a/b/illegal.java")};

        final String filepath = getPath("misc/ModuleDirectoryLayout/default/src/main/java/a/b/illegal.java");
        verify(mCheckConfig, filepath, expected);
    }



    @Test
    public void testConfigFileNotFound()
        throws Exception
    {
        mCheckConfig.addAttribute("baseDir", getPath("misc/ModuleDirectoryLayout/default"));
        mCheckConfig.addAttribute("configFile", "non/existent/file.json");

        Throwable expectedException = null;
        try {
            final String filepath = getPath("misc/ModuleDirectoryLayout/default/src/main/java/a/b/illegal.txt");
            verify(mCheckConfig, filepath, new String[0]);
            Assert.fail("expected exception was not thrown");
        }
        catch (IllegalArgumentException e) {
            // expected - Versions of Checkstyle prior to 6.12 give the expected exception directly
            expectedException = e;
        }
        catch (CheckstyleException e) {
            // expected - Checkstyle 6.12 and later wrap the exception in a CheckstyleException
            expectedException = e.getCause();
        }
        Assert.assertTrue(expectedException instanceof IllegalArgumentException);
        Assert.assertTrue(expectedException.getCause() instanceof FileNotFoundException);
        Assert.assertTrue(expectedException.getMessage().contains("Config file not found for"));
    }



    @Test
    public void testConfigFileNotFoundFailQuietly()
        throws Exception
    {
        mCheckConfig.addAttribute("baseDir", getPath("misc/ModuleDirectoryLayout/default"));
        mCheckConfig.addAttribute("configFile", "non/existent/file.json");
        mCheckConfig.addAttribute("failQuietly", "true");

        final String filepath = getPath("misc/ModuleDirectoryLayout/default/src/main/java/a/b/illegal.txt");
        // The check should be disabled because the json could not be found.
        verify(mCheckConfig, filepath, new String[0]);
    }



    @Test
    public void testCutSlashes()
    {
        ModuleDirectoryLayoutCheck check = new ModuleDirectoryLayoutCheck();

        Assert.assertEquals("", check.cutSlashes(""));
        Assert.assertEquals(" ", check.cutSlashes(" "));
        Assert.assertEquals("", check.cutSlashes("/"));
        Assert.assertEquals("", check.cutSlashes("\\\\"));
        Assert.assertEquals("foo", check.cutSlashes("/foo"));
        Assert.assertEquals("foo\\bar", check.cutSlashes("\\foo\\bar"));
        Assert.assertEquals("bar", check.cutSlashes("bar/"));
        Assert.assertEquals("bar/foo", check.cutSlashes("bar/foo/"));
    }



    @Test(expected = NullPointerException.class)
    public void testCutSlashesNPE()
    {
        new ModuleDirectoryLayoutCheck().cutSlashes(null);
    }



    /**
     * Check that even when submodules are specified, files may always be in the module root and baseDir.
     *
     * @throws Exception test failed
     */
    @Test
    public void testRootFilesWhenModuleRegexGivenOk()
        throws Exception
    {
        mCheckConfig.addAttribute("baseDir", getPath("misc/ModuleDirectoryLayout/scenario7"));
        mCheckConfig.addAttribute("configFile", getPath("misc/ModuleDirectoryLayout/directories-scenario7.json"));

        final File[] filesToCheck = new File[]{//
            new File(getPath("misc/ModuleDirectoryLayout/scenario7/module/src/file.txt")), //
            new File(getPath("misc/ModuleDirectoryLayout/scenario7/module/moduleRootFile.txt")), //
            new File(getPath("misc/ModuleDirectoryLayout/scenario7/rootFile.txt"))};
        verify(createChecker(mCheckConfig), filesToCheck, "unused", new String[0]);
    }



    /**
     * Tests an interesting corner case where one MDL path occurs as the first part of the name of a file in a
     * different MDL path. This "scenario8" is modeled after an actual production bug.
     *
     * @throws Exception test failed
     */
    @Test
    public void testOneFilenameStartsWithMdlPathName()
        throws Exception
    {
        mCheckConfig.addAttribute("baseDir", getPath("misc/ModuleDirectoryLayout/scenario8"));
        mCheckConfig.addAttribute("configFile", getPath("misc/ModuleDirectoryLayout/directories-scenario8.json"));

        final File[] filesToCheck = new File[]{//
            new File(getPath("misc/ModuleDirectoryLayout/scenario8/module1/a/allowed.txt")), //
            new File(getPath("misc/ModuleDirectoryLayout/scenario8/module1/a/module1-allowed.txt")), //
            new File(getPath("misc/ModuleDirectoryLayout/scenario8/module1/allowed.txt")), //
            new File(getPath("misc/ModuleDirectoryLayout/scenario8/module1/module1-allowed.txt"))};

        verify(createChecker(mCheckConfig), filesToCheck, "unused", new String[0]);
    }



    @Test
    public void testExcludeRegex()
        throws Exception
    {
        mCheckConfig.addAttribute("baseDir", getPath("misc/ModuleDirectoryLayout/default"));
        mCheckConfig.addAttribute("configFile", new File(
            "src/main/resources/com/thomasjensen/checkstyle/addons/checks/misc/ModuleDirectoryLayout-default.json")
            .getCanonicalPath());

        final String filepath = getPath("misc/ModuleDirectoryLayout/default/.idea/csi-007/ignore_this.txt");
        verify(mCheckConfig, filepath, new String[0]);
    }
}
