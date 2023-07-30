package com.thomasjensen.checkstyle.addons.build;
/*
 * Checkstyle-Addons - Additional Checkstyle checks
 * Copyright (c) 2015-2023, the Checkstyle Addons contributors
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

import java.util.Date;
import javax.annotation.Nonnull;

import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;


public class BuildConfigExtension
{
    private final Property<String> authorName;

    private final Property<Date> buildTimestamp;

    private final Property<String> checksPackage;

    private final Property<String> gitHash;

    private final Property<String> github;

    private final Property<String> issueTrackerUrl;

    private final Provider<String> licenseUrl;

    private final Property<String> longName;

    private final Property<String> orgName;

    private final Property<String> orgUrl;

    private final Property<String> sqPackage;

    private final Property<String> sqPluginKey;

    private final Property<String> website;



    public BuildConfigExtension(@Nonnull final Project pProject)
    {
        final ObjectFactory objectFactory = pProject.getObjects();
        authorName = objectFactory.property(String.class);
        buildTimestamp = objectFactory.property(Date.class);
        checksPackage = objectFactory.property(String.class);
        gitHash = objectFactory.property(String.class);
        github = objectFactory.property(String.class);
        issueTrackerUrl = objectFactory.property(String.class);
        licenseUrl = github.map(gh -> buildLicenseUrl(pProject, gh));
        longName = objectFactory.property(String.class);
        orgName = objectFactory.property(String.class);
        orgUrl = objectFactory.property(String.class);
        sqPackage = objectFactory.property(String.class);
        sqPluginKey = objectFactory.property(String.class);
        website = objectFactory.property(String.class);
    }



    @Nonnull
    public Property<String> getAuthorName()
    {
        return authorName;
    }



    @Nonnull
    public Property<Date> getBuildTimestamp()
    {
        return buildTimestamp;
    }



    @Nonnull
    public Property<String> getChecksPackage()
    {
        return checksPackage;
    }



    @Nonnull
    public Property<String> getGitHash()
    {
        return gitHash;
    }



    @Nonnull
    public Property<String> getGithub()
    {
        return github;
    }



    @Nonnull
    public Property<String> getIssueTrackerUrl()
    {
        return issueTrackerUrl;
    }



    @Nonnull
    public Provider<String> getLicenseUrl()
    {
        return licenseUrl;
    }



    @Nonnull
    public Property<String> getLongName()
    {
        return longName;
    }



    @Nonnull
    public Property<String> getOrgName()
    {
        return orgName;
    }



    @Nonnull
    public Property<String> getOrgUrl()
    {
        return orgUrl;
    }



    @Nonnull
    public Property<String> getSqPackage()
    {
        return sqPackage;
    }



    @Nonnull
    public Property<String> getSqPluginKey()
    {
        return sqPluginKey;
    }



    @Nonnull
    public Property<String> getWebsite()
    {
        return website;
    }



    @Nonnull
    private String buildLicenseUrl(@Nonnull final Project pProject, @Nonnull final String pGitHub)
    {
        final String version = pProject.getVersion().toString();
        boolean isRelease = version.indexOf('-') < 0;
        String versionStr = isRelease ? ("v" + version) : "master";
        return "https://raw.githubusercontent.com/" + pGitHub + "/" + versionStr + "/LICENSE";
    }
}
