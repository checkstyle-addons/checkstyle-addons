package com.thomasjensen.checkstyle.addons.build.tasks;
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

import java.util.Map;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.TaskAction;

import com.thomasjensen.checkstyle.addons.build.DependencyConfig;
import com.thomasjensen.checkstyle.addons.build.DependencyConfigs;


/**
 * Simple task which prints information about Checkstyle Addons' dependency configs.
 */
public class PrintDepConfigsTask
    extends DefaultTask
{
    private final DependencyConfigs depConfigs;

    private final Logger log;



    @Inject
    public PrintDepConfigsTask(@Nonnull final DependencyConfigs pDepConfigs)
    {
        depConfigs = pDepConfigs;
        setDescription("Print the Checkstyle Addons dependency configurations");
        log = getProject().getLogger();
    }



    @TaskAction
    public void printDepConfigs()
    {
        log.info("-----------------------------------------------------------------------------------------------");
        log.lifecycle("Dependency Configurations:");
        log.lifecycle("--------------------------");
        log.lifecycle("Default Checkstyle version: " + depConfigs.getDefault().getCheckstyleBaseVersion());
        log.lifecycle("Active dependency configurations:");
        for (Map.Entry<String, DependencyConfig> entry : depConfigs.getAll().entrySet()) {
            DependencyConfig depConfig = entry.getValue();
            log.lifecycle("  - " + entry.getKey() + ": Checkstyle " + depConfig.getCheckstyleBaseVersion()
                + ", Java " + depConfig.getJavaLevel()
                + ", compatible: " + depConfig.getCompatibleCheckstyleVersions());
        }

        log.info("-----------------------------------------------------------------------------------------------");
        log.info("Full contents:");
        for (final Map.Entry<String, DependencyConfig> entry : depConfigs.getAll().entrySet()) {
            log.info("  - " + entry.getKey() + ":\t" + entry.getValue());
        }
        log.info("-----------------------------------------------------------------------------------------------");
    }
}
