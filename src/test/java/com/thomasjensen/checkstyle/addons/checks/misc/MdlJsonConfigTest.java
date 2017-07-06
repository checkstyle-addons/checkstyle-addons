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

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thomasjensen.checkstyle.addons.util.Util;
import org.junit.Assert;
import org.junit.Test;


/**
 * Unit tests of {@link MdlJsonConfig}.
 */
public final class MdlJsonConfigTest
{
    @Test
    public void deserializeExampleConfig()
        throws IOException, ConfigValidationException
    {
        InputStream is = null;
        byte[] fileContents = null;
        try {
            is = getClass().getResourceAsStream("ModuleDirectoryLayout/directories.json");
            fileContents = Util.readBytes(is);
        }
        finally {
            Util.closeQuietly(is);
        }

        String json = new String(fileContents, Util.UTF8);
        MdlJsonConfig cfg = new ObjectMapper().readValue(json, MdlJsonConfig.class);

        cfg.validate();  // should be ok

        Assert.assertNotNull(cfg);
        Assert.assertEquals("A sunny day config file.", cfg.getComment());
        Assert.assertNotNull(cfg.getSettings());
        Assert.assertNotNull(cfg.getStructure());

        Assert.assertEquals(1, cfg.getSettings().getFormatVersion());
        Assert.assertEquals("", cfg.getSettings().getModuleRegex());
        Assert.assertFalse(cfg.getSettings().isAllowNestedSrcFolder());

        Assert.assertEquals(5, cfg.getStructure().size());
        Assert.assertEquals("a nice MDL path comment", cfg.getStructure().get("src/main/webapp").getComment());
    }



    @Test
    public void testSpecElementToString()
    {
        MdlJsonConfig.SpecElement se = new MdlJsonConfig.SpecElement(MdlContentSpecType.SimpleName, "foo.txt");
        Assert.assertEquals("{SimpleName, \"foo.txt\"}", se.toString());

        se = new MdlJsonConfig.SpecElement();
        Assert.assertEquals("{null, null}", se.toString());
    }
}
