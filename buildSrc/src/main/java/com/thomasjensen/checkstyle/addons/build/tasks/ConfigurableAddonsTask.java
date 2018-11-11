package com.thomasjensen.checkstyle.addons.build.tasks;
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

import javax.annotation.Nonnull;

import com.thomasjensen.checkstyle.addons.build.DependencyConfig;


/**
 * Defines common methods for all Checkstyle Addons Gradle tasks.
 */
public interface ConfigurableAddonsTask
{
    /**
     * Configure this task instance for a given dependency configuration.
     *
     * @param pDepConfig the dependency configuration for which to configure
     */
    void configureFor(@Nonnull DependencyConfig pDepConfig);
}
