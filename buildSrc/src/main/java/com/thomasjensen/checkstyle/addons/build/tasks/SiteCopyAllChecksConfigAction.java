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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.tools.ant.filters.ReplaceTokens;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.file.CopySpec;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.SourceSet;

import com.thomasjensen.checkstyle.addons.build.BuildUtil;
import com.thomasjensen.checkstyle.addons.build.DependencyConfig;


public class SiteCopyAllChecksConfigAction
    extends AbstractTaskConfigAction<Copy>
{
    public SiteCopyAllChecksConfigAction()
    {
        super();
    }



    @Override
    protected void configureTaskFor(@Nonnull Copy pCopyTask, @Nullable DependencyConfig pUnused)
    {
        pCopyTask.setDescription("Copy list of all checks to site directory");
        pCopyTask.setGroup(SiteTask.SITE_GROUP);
        pCopyTask.setDestinationDir(new File(project.getBuildDir(), "site"));

        final SourceSet mainSourceSet = buildUtil.getSourceSet(SourceSet.MAIN_SOURCE_SET_NAME);
        final String checksPackage = buildUtil.getBuildConfig().getChecksPackage().get();
        final File originalFile = new File(mainSourceSet.getResources().getSrcDirs().iterator().next(),
            "/" + checksPackage + "/all_checks.html");
        final Action<CopySpec> action = copySpec -> {
            copySpec.from(originalFile);
            copySpec.rename((String fileName) -> fileName.replace(originalFile.getName(), "index.html"));
            copySpec.filter(buildReplacements(project), ReplaceTokens.class);
        };
        pCopyTask.into("v" + project.getVersion() + "/checks", action);
        pCopyTask.into("latest/checks", action);
    }



    public static Map<String, Object> buildReplacements(@Nonnull final Project pProject)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ZZZZ");
        Date buildTimestamp = new BuildUtil(pProject).getBuildConfig().getBuildTimestamp().get();
        Map<String, String> replacements = new HashMap<>();
        replacements.put("buildTimestamp", sdf.format(buildTimestamp));
        replacements.put("version", pProject.getVersion().toString());
        Map<String, Object> result = new HashMap<>();
        result.put("tokens", replacements);
        return result;
    }
}
