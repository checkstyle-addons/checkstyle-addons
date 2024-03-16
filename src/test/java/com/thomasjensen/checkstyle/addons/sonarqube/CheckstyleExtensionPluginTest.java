package com.thomasjensen.checkstyle.addons.sonarqube;
/*
 * Checkstyle-Addons - Additional Checkstyle checks
 * Copyright (c) 2015-2024, the Checkstyle Addons contributors
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

import org.junit.Test;
import org.mockito.Mockito;
import org.sonar.api.Plugin;


/**
 * Very basic unit test of our {@link CheckstyleExtensionPlugin}.
 */
public class CheckstyleExtensionPluginTest
{
    @Test
    public void testPluginDefinition()
    {
        Plugin.Context mockCtx = Mockito.mock(Plugin.Context.class);
        CheckstyleExtensionPlugin underTest = new CheckstyleExtensionPlugin();
        underTest.define(mockCtx);
    }
}
