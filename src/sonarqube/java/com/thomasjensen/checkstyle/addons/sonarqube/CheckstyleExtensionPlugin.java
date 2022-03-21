package com.thomasjensen.checkstyle.addons.sonarqube;
/*
 * Checkstyle-Addons - Additional Checkstyle checks
 * Copyright (c) 2015-2022, the Checkstyle Addons contributors
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

import java.util.Collections;

import org.sonar.api.Plugin;


/**
 * The Checkstyle Addons SonarQube plugin main class.
 */
public final class CheckstyleExtensionPlugin
    implements Plugin
{
    @Override
    public void define(final Context pContext)
    {
        pContext.addExtensions(Collections.singleton(RulesExtension.class));
    }
}
