package com.thomasjensen.checkstyle.addons.build.tasks;
/*
 * Checkstyle-Addons - Additional Checkstyle checks
 * Copyright (c) 2015-2020, the Checkstyle Addons contributors
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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.tools.ant.filters.ReplaceTokens;
import org.gradle.api.Action;
import org.gradle.api.file.CopySpec;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.SourceSet;

import com.thomasjensen.checkstyle.addons.build.DependencyConfig;


public class SiteCopyDownloadGuideConfigAction
    extends AbstractTaskConfigAction<Copy>
{
    public SiteCopyDownloadGuideConfigAction()
    {
        super();
    }



    @Override
    protected void configureTaskFor(@Nonnull Copy pCopyTask, @Nullable DependencyConfig pUnused)
    {
        pCopyTask.setDescription("Copy download guide frontmatter stub to site directory");
        pCopyTask.setGroup(SiteTask.SITE_GROUP);
        pCopyTask.setDestinationDir(new File(project.getBuildDir(), "site"));

        final SourceSet mainSourceSet = buildUtil.getSourceSet(SourceSet.MAIN_SOURCE_SET_NAME);
        final File originalFile = new File(mainSourceSet.getResources().getSrcDirs().iterator().next(),
            "/download-guide.html");
        final Action<CopySpec> action = copySpec -> {
            copySpec.from(originalFile);
            copySpec.filter(SiteCopyAllChecksConfigAction.buildReplacements(project), ReplaceTokens.class);
        };
        pCopyTask.into("v" + project.getVersion(), action);
        pCopyTask.into("latest", action);
    }
}
