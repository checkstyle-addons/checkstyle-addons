package com.thomasjensen.checkstyle.addons.build.tasks;

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



    @Inject
    public PrintDepConfigsTask(@Nonnull final DependencyConfigs pDepConfigs)
    {
        depConfigs = pDepConfigs;
    }



    @TaskAction
    public void printDepConfigs()
    {
        final Logger log = getProject().getLogger();
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
