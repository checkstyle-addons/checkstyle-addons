package com.thomasjensen.checkstyle.addons.checks.regexp;
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
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import com.puppycrawl.tools.checkstyle.api.AbstractFileSetCheck;
import com.puppycrawl.tools.checkstyle.api.FileText;


/**
 * This check applies a given regular expression to the names of files.
 * <p><a href="http://checkstyle-addons.thomasjensen.com/latest/checks/regexp.html#RegexpOnFilenameOrg"
 * target="_blank">Documentation</a></p>
 */
public class RegexpOnFilenameOrgCheck
    extends AbstractFileSetCheck
{
    /**
     * regexp applied to the canonical file name in order to determine if the file is applicable for the check
     * (substring match)
     */
    private Pattern selection;

    /** mode of operation (required or illegal) */
    private RegexpOnFilenameOrgOption mode = RegexpOnFilenameOrgOption.ILLEGAL;

    /**
     * if <code>true</code>, only the simple name of the file will be checked against the regexp;<br> if
     * <code>false</code>, the entire canonical path will be checked
     */
    private boolean simple = true;

    /** the default regexp detects leading and trailing whitespace */
    private static final Pattern REGEXP_DEFAULT = Pattern.compile("^(?:\\s+.*|.*?\\s+)$");

    /** the given regexp */
    private Pattern regexp = REGEXP_DEFAULT;



    /**
     * Setter.
     *
     * @param pSelection the new value of {@link #selection}
     */
    public void setSelection(final String pSelection)
    {
        if (pSelection != null && pSelection.length() > 0) {
            selection = Pattern.compile(pSelection);
        }
    }



    public void setMode(final String pMode)
    {
        mode = RegexpOnFilenameOrgOption.valueOfIgnoreCase(pMode);
    }



    public void setSimple(final boolean pSimple)
    {
        simple = pSimple;
    }



    /**
     * Setter.
     *
     * @param pRegexp the new value of {@link #regexp}
     */
    public void setRegexp(final String pRegexp)
    {
        if (pRegexp != null && pRegexp.length() > 0) {
            regexp = Pattern.compile(pRegexp);
        }
    }



    @Override
    protected void processFiltered(final File pFile, final List<String> pLines)
    {
        String filePath = null;
        try {
            filePath = pFile.getCanonicalPath();
        }
        catch (IOException e) {
            filePath = pFile.getAbsolutePath();
        }

        boolean ok = true;
        if (selection == null || selection.matcher(filePath).find()) {
            if (simple) {
                filePath = pFile.getName();
            }
            ok = regexp.matcher(filePath).find() ^ (mode == RegexpOnFilenameOrgOption.ILLEGAL);
        }

        if (!ok) {
            final String msgKey = "regexp.filepath." + mode.toString().toLowerCase(Locale.ENGLISH);
            // Log the exact String that the regexp was applied to and the exact regexp that was
            // used. It is important to be accurate here in order to enable people to check results.
            log(0, msgKey, filePath, regexp.pattern());
        }
    }



    protected void processFiltered(final File pFile, final FileText pLines)
    {
        processFiltered(pFile, Collections.<String>emptyList());
    }
}
