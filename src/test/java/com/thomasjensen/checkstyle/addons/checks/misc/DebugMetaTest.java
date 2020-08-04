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

import java.io.File;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.thomasjensen.checkstyle.addons.BaseCheckTestSupport;


/**
 * Unit test of {@link DebugMetaCheck}.
 */
public class DebugMetaTest
    extends BaseCheckTestSupport
{
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();



    public DebugMetaTest()
    {
        setCheckShortname(DebugMetaCheck.class);
    }



    @Test
    public void testSunnyDayConstantsInt()
        throws Exception
    {
        final File outFile = tempFolder.newFile("debug-out.log");
        final DefaultConfiguration checkConfig = createCheckConfig(DebugMetaCheck.class);
        checkConfig.addAttribute("outputFile", outFile.getAbsolutePath());
        checkConfig.addAttribute("append", "true");

        verify(checkConfig, getPath("misc/InputPropertyCatalog1.java"), new String[0]);

        Assert.assertTrue(outFile.canRead());
        Assert.assertTrue(outFile.length() > 0);
    }
}
