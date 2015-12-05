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

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.puppycrawl.tools.checkstyle.api.AbstractFileSetCheck;
import com.thomasjensen.checkstyle.addons.util.Util;


/**
 * Checks for the presence of required files.
 *
 * @author Thomas Jensen
 */
public class RequiredFileCheck
    extends AbstractFileSetCheck
{
    private Set<String> dirs = new HashSet<String>();

    /*
     * --------------- Check properties: ---------------------------------------------------------------------------
     */

    private String simpleFilename = null;

    /** the base directory to be assumed for this check, usually the project's root directory */
    private File baseDir = Util.canonize(new File("."));

    private Set<String> directoryGlobs = Collections.emptySet();

    private boolean ignoreEmptyDirs = false;

    private boolean caseSensitive = true;



    @Override
    public void beginProcessing(final String pCharset)
    {
        super.beginProcessing(pCharset);
        if (ignoreEmptyDirs) {
            dirs.clear();
        }
        else {
            dirs = readAllDirs();
        }
    }



    private Set<String> readAllDirs()
    {
        // TODO read all matching dirs beneath baseDir
        return null;
    }



    @Override
    protected void processFiltered(final File pFile, final List<String> pList)
    {
        // TODO implement processFiltered()

    }



    @Override
    public void finishProcessing()
    {
        // TODO implement finishProcessing()
        super.finishProcessing();
    }



    public void setFile(final String pSimpleFilename)
    {
        simpleFilename = pSimpleFilename;
    }



    public void setBaseDir(final String pBaseDir)
    {
        baseDir = Util.canonize(new File(pBaseDir));
    }



    public void setCaseSensitive(final boolean pCaseSensitive)
    {
        caseSensitive = pCaseSensitive;
    }



    public void setDirectories(final String... pDirectoryGlobs)
    {
        final Set<String> newDirectoryGlobs = new HashSet<String>();
        Collections.addAll(newDirectoryGlobs, pDirectoryGlobs);
        directoryGlobs = Collections.unmodifiableSet(newDirectoryGlobs);
    }



    public void setIgnoreEmptyDirs(final boolean pIgnoreEmptyDirs)
    {
        ignoreEmptyDirs = pIgnoreEmptyDirs;
    }
}
