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

import java.util.Collections;
import java.util.List;

import org.sonar.api.SonarPlugin;


/**
 * The Checkstyle Addons SonarQube plugin main class.
 *
 * @author Thomas Jensen
 */
public final class CheckstyleExtensionPlugin
    extends SonarPlugin
{
    @Override
    @SuppressWarnings("unchecked")
    public List<?> getExtensions()
    {
        return Collections.singletonList(CheckstyleExtensionRepository.class);
    }
}