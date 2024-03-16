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
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.gradle.api.file.CopySpec;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.javadoc.Javadoc;

import com.thomasjensen.checkstyle.addons.build.DependencyConfig;


public class SiteCopyJavadocConfigAction
    extends AbstractTaskConfigAction<Copy>
{
    public SiteCopyJavadocConfigAction()
    {
        super();
    }



    @Override
    protected void configureTaskFor(@Nonnull Copy pCopyTask, @Nullable DependencyConfig pUnused)
    {
        Javadoc javadocTask = (Javadoc) project.getTasks().getByName("javadoc");
        pCopyTask.dependsOn(javadocTask);
        pCopyTask.setDescription("Copy Javadoc to site directory");
        pCopyTask.setGroup(SiteTask.SITE_GROUP);
        pCopyTask.setDestinationDir(new File(project.getLayout().getBuildDirectory().getAsFile().get(), "site"));
        pCopyTask.setDuplicatesStrategy(DuplicatesStrategy.EXCLUDE);

        final String website = buildUtil.getBuildConfig().getWebsite().get();
        pCopyTask.into("v" + project.getVersion() + "/apidocs", (CopySpec copySpec) -> {
            copySpec.from(project.fileTree(Objects.requireNonNull(javadocTask.getDestinationDir())));
            copySpec.filter((String line) -> {
                if (!line.contains(website + "latest/checks/")) {
                    return line;
                }
                return line.replace(website + "latest/checks/", website + "v" + project.getVersion() + "/checks/");
            });
        });
        pCopyTask.into("v" + project.getVersion() + "/apidocs/resources", copySpec ->
            copySpec.from(project.fileTree(new File(javadocTask.getDestinationDir(), "resources"))));
        pCopyTask.into("latest/apidocs", copySpec ->
            copySpec.from(project.fileTree(Objects.requireNonNull(javadocTask.getDestinationDir()))));
    }
}
