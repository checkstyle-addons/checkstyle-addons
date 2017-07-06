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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

import net.jcip.annotations.Immutable;


/**
 * Encapsulates the components of a checked file path.
 */
@Immutable
public class DecomposedPath
{
    private final String modulePath;

    private final String mdlPath;

    private final String specificPath;

    private final String simpleFilename;

    private final Set<String> fileExtensions;

    private final List<String> specificFolders;



    /**
     * Constructor.
     *
     * @param pModulePath the module path, for example <code>subsystem1/module1</code>
     * @param pMdlPath the MDL path, for example <code>src/main/java</code>
     * @param pSpecificPath the specific path, for example <code>com/acme/Foo.java</code>
     * @param pSimpleFilename the simple file name, for example <code>Foo.java</code>
     * @param pFileExtensions the list of file extensions, for example <code>java</code>, but it could be a list if
     * multiple dots are present (e.g. <code>tar.gz, gz</code>)
     * @param pSpecificFolders the simple names of all folders on the specific path, for example <code>com, acme</code>
     */
    public DecomposedPath(@Nonnull final String pModulePath, @Nonnull final String pMdlPath,
        @Nonnull final String pSpecificPath, @Nonnull final String pSimpleFilename,
        @Nonnull final Set<String> pFileExtensions, @Nonnull final List<String> pSpecificFolders)
    {
        modulePath = pModulePath;
        mdlPath = pMdlPath;
        specificPath = pSpecificPath;
        simpleFilename = pSimpleFilename;
        fileExtensions = Collections.unmodifiableSet(new HashSet<String>(pFileExtensions));
        specificFolders = Collections.unmodifiableList(new ArrayList<String>(pSpecificFolders));
    }



    @Nonnull
    public String getModulePath()
    {
        return modulePath;
    }



    @Nonnull
    public String getMdlPath()
    {
        return mdlPath;
    }



    @Nonnull
    public String getSpecificPath()
    {
        return specificPath;
    }



    @Nonnull
    public String getSimpleFilename()
    {
        return simpleFilename;
    }



    @Nonnull
    public Set<String> getFileExtensions()
    {
        return fileExtensions;
    }



    @Nonnull
    public List<String> getSpecificFolders()
    {
        return specificFolders;
    }
}
