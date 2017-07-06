package com.thomasjensen.checkstyle.addons.sonarqube;
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

import java.util.List;

import junit.framework.TestCase;
import org.junit.Test;


/**
 * Unit test of {@link CheckstyleExtensionPlugin}.
 */
public class CheckstyleExtensionPluginTest
{
    @Test
    public void testGetExtensions()
    {
        List<?> exts = new CheckstyleExtensionPlugin().getExtensions();
        TestCase.assertNotNull(exts);
        TestCase.assertEquals(1, exts.size());
    }
}
