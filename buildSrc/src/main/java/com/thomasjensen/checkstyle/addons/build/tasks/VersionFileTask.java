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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import com.thomasjensen.checkstyle.addons.build.BuildConfigExtension;
import com.thomasjensen.checkstyle.addons.build.BuildUtil;


public class VersionFileTask
    extends DefaultTask
{
    public static final String TASK_NAME = "versionFile";

    private final Property<File> versionFile = getProject().getObjects().property(File.class);

    private BuildUtil buildUtil = null;



    public VersionFileTask()
    {
        buildUtil = new BuildUtil(getProject());
    }



    public static void konfigure(@Nonnull final VersionFileTask pTask)
    {
        final Project project = pTask.getProject();
        pTask.setDescription("Writes the project version to a file to make it available at runtime.");
        pTask.getVersionFile().set(new File(pTask.getTemporaryDir(), "version.properties"));
        pTask.getInputs().property("version", project.getVersion());
    }



    @TaskAction
    public void writeVersionFile()
    {
        final BuildConfigExtension buildConfig = buildUtil.getBuildConfig();
        List<String> lines = new ArrayList<>();
        lines.add("# " + buildConfig.getLongName().get() + " version information");
        lines.add("# " + buildConfig.getBuildTimestamp().get());
        lines.add("version=" + getInputs().getProperties().get("version"));

        try {
            Files.write(getVersionFile().get().toPath(), lines, StandardCharsets.US_ASCII);
        }
        catch (IOException e) {
            throw new GradleException("Failed to create file: " + getVersionFile().get().getAbsolutePath(), e);
        }
    }



    @Nonnull
    @OutputFile
    public Property<File> getVersionFile()
    {
        return versionFile;
    }
}
