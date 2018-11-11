package com.thomasjensen.checkstyle.addons.build;
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


/**
 * List of extra properties used in the Gradle project along with their descriptions.
 */
public enum ExtProp
{
    /** String, name of the Gradle publication which is used by default */
    DefaultPublication("defaultPublication"),

    /** String, name of the author, to be used in manifests and such */
    AuthorName("authorName"),

    /** String, the long name of this piece of software, to be used in manifests and such */
    LongName("longName"),

    /** String, the descriptive name of our GitHub organization */
    OrgName("orgName"),

    /** String, the URL of our GitHub organization */
    OrgUrl("orgUrl"),

    /** String, the ID of our GitHub repo (currently {@code "checkstyle-addons/checkstyle-addons"}) */
    Github("github"),

    /** String, the URL of our issue tracker */
    IssueTrackerUrl("issueTrackerUrl"),

    /** {@link java.util.Date Date}, the build timestamp */
    BuildTimestamp("buildTimestamp"),

    /** String, the key which identifies our SonarQube plugin to SonarQube */
    SqPluginKey("sqPluginKey"),

    /** String, the package of our SonarQube plugin classes, as a relative path with no trailing slash */
    SqPackage("sqPackage"),

    /** String, the top level package of our checks, as a relative path with no trailing slash */
    ChecksPackage("checksPackage"),

    /** String, the URL of our website */
    Website("website"),

    /** Grgit, a handle on the local clone of our git repo */
    GitRepo("gitRepo"),

    /** String, hash of the HEAD of the local git repo */
    GitHash("gitHash"),

    /** {@link DependencyConfigs}, the dependency configurations */
    DepConfigs("depConfigs"),

    /** {@link DependencyConfig}, the default dependency config to be used in the IDE */
    VersionDefaults("versionDefaults"),

    /** the configuration for test tasks */
    TestConfigClosure("testConfigClosure");

    //

    private final String propertyName;



    private ExtProp(final String pPropertyName)
    {
        propertyName = pPropertyName;
    }



    @Nonnull
    public String getPropertyName()
    {
        return propertyName;
    }
}
