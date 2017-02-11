package com.thomasjensen.checkstyle.addons.build;
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

import javax.annotation.Nonnull;


/**
 * List of extra properties used in the Gradle project along with their descriptions.
 *
 * @author Thomas Jensen
 */
public enum ExtProp
{
    /** List of Strings, names of Gradle configurations which should be bundled with our artifacts */
    BundledConfigs("bundledConfigurations"),

    /** String, name of the Gradle publication which is used by default */
    DefaultPublication("defaultPublication"),

    /** String, name of the Gradle project property giving the absolute path to the javac executable for Java&nbsp;6 */
    Jdk6PropName("jdk6PropName"),

    /** String, name of the Gradle project property giving the absolute path to the javadoc executable for Java 6 */
    Javadoc6PropName("javadoc6PropName"),

    /** String, name of the Gradle project property giving the absolute path to the javac executable for Java&nbsp;7 */
    Jdk7PropName("jdk7PropName"),

    /** String, name of the Gradle project property giving the absolute path to the javadoc executable for Java 7 */
    Javadoc7PropName("javadoc7PropName"),

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

    /** {@link NameFactory}, an instance of a name factory */
    NameFactory("nameFactory");

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
