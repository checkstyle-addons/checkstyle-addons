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
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;


/**
 * Unit tests of {@link MdlJsonConfigValidator}.
 */
public final class MdlJsonConfigValidatorTest
{
    /**
     * Make sure that the default directories.json validates ok.
     *
     * @throws URISyntaxException bug
     */
    @Test
    public void testValidateDefaultConfig()
        throws URISyntaxException
    {
        final URL url = getClass().getResource("ModuleDirectoryLayout-default.json");
        Assert.assertNotNull("ModuleDirectoryLayout-default.json not found", url);
        final File configFile = new File(url.toURI());
        Assert.assertNotNull(configFile);

        MdlJsonConfigValidator.main(new String[]{configFile.getAbsolutePath()});
    }
}
