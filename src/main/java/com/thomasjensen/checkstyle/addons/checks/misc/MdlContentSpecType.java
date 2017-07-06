package com.thomasjensen.checkstyle.addons.checks.misc;
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



/**
 * The <code>type</code> values which are possible in the allow and deny lists of the <i>directories.json</i>
 * configuration file used by the {@link ModuleDirectoryLayoutCheck}.
 */
public enum MdlContentSpecType
{
    /** A comma-separated list of file extensions, excluding leading dots. */
    FileExtensions,

    /**
     * one of the MDL paths listed in <i>directories.json</i>. Can only be used in deny lists. Denies everything that is
     * contained in the allow list of the referenced MDL path.
     */
    FromPath,

    /** the simple name of a file, for example <code>file.txt</code> */
    SimpleName,

    /**
     * a regular expression which is applied to the specific path. Note that slashes or backslashes may be present in
     * that path depending on the current platform.
     */
    SpecificPathRegex,

    /**
     * the simple name of a folder which, if present at the top of the specific path, constitutes a match. Folders which
     * are specified as top-level folders may not occur again further down on the path. For example, if
     * <code>META-INF</code> was specified as top-level folder, a path like <code>META-INF/foo/META-INF/file.txt</code>
     * would be illegal.
     */
    TopLevelFolder,

    /**
     * the simple name of a folder which, if present anywhere on the specific path, constitutes a match. For example,
     * <code>doc-files</code> would match all files contained somewhere in a <code>doc-files</code> folder.
     */
    SimpleFolder;
}
