package com.thomasjensen.checkstyle.addons.util;
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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;


/**
 * The public API of the Checkstyle tool changes frequently, and many releases of Checkstyle are incompatible with their
 * predecessors. This class uses reflection in order to allow the Checkstyle Addons code to work with all dependency
 * configurations. It simply provides a stable interface with workarounds for all supported Checkstyle versions.
 */
public class CheckstyleApiFixer
{
    private final AbstractCheck check;

    private final File currentFileNameMockFile;



    /**
     * Constructor.
     *
     * @param pCheck the check for which this instance of the API fixer shall run
     */
    public CheckstyleApiFixer(@Nonnull final AbstractCheck pCheck)
    {
        this(pCheck, null);
    }



    /**
     * Constructor.
     *
     * @param pCheck the check for which this instance of the API fixer shall run
     * @param pCurrentFileNameMockFile file to use as result of {@link #getCurrentFileName()} in unit tests
     */
    public CheckstyleApiFixer(@Nonnull final AbstractCheck pCheck, @Nullable final String pCurrentFileNameMockFile)
    {
        check = pCheck;
        currentFileNameMockFile = pCurrentFileNameMockFile != null ? new File(pCurrentFileNameMockFile) : null;
    }



    /**
     * Wrapper for <code>FileContents.getFileName()</code> (Checkstyle issue <a target="_blank"
     * href="https://github.com/checkstyle/checkstyle/issues/1205">#1205</a>).
     *
     * @return the currently analyzed file as returned by Checkstyle
     *
     * @throws UnsupportedOperationException no known variant of <code>FileContents.getFileName()</code> could be found
     */
    @CheckForNull
    public File getCurrentFileName()
    {
        if (currentFileNameMockFile != null) {
            return currentFileNameMockFile;
        }

        // the remainder of this method is a workaround for Checkstyle issue #1205
        // https://github.com/checkstyle/checkstyle/issues/1205
        final FileContents fileContents = check.getFileContents();
        Method getFilename = null;
        try {
            getFilename = fileContents.getClass().getMethod("getFileName");
        }
        catch (NoSuchMethodException e) {
            try {
                getFilename = fileContents.getClass().getMethod("getFilename");
            }
            catch (NoSuchMethodException e1) {
                throw new UnsupportedOperationException("FileContents.getFilename()", e1);
            }
        }

        String filename = null;
        if (getFilename != null) {
            try {
                filename = (String) getFilename.invoke(fileContents);
            }
            catch (IllegalAccessException | InvocationTargetException e) {
                throw new UnsupportedOperationException("FileContents.getFilename()", e);
            }
        }

        File result = filename != null ? new File(filename) : null;
        return result;
    }



    /**
     * Returns the name of a token for a given ID.
     *
     * @param pTokenId the ID of the token name to get
     * @return a token name as returned by Checkstyle
     *
     * @throws UnsupportedOperationException no known variant of <code>getTokenName()</code> could be found
     */
    @Nonnull
    public String getTokenName(final int pTokenId)
    {
        final List<String> searchClasses = Arrays.asList("com.puppycrawl.tools.checkstyle.utils.TokenUtil",
            "com.puppycrawl.tools.checkstyle.utils.TokenUtils", TokenTypes.class.getName(),
            "com.puppycrawl.tools.checkstyle.Utils");
        Method getTokenName = null;

        for (final String className : searchClasses) {
            try {
                final Class<?> utilsClass = Class.forName(className);
                getTokenName = utilsClass.getMethod("getTokenName", int.class);
                break;
            }
            catch (ClassNotFoundException | NoSuchMethodException e) {
                // ignore
            }
        }
        if (getTokenName == null) {
            throw new UnsupportedOperationException("getTokenName() - method not found");
        }

        String result = null;
        try {
            result = (String) getTokenName.invoke(null, pTokenId);
        }
        catch (IllegalAccessException | InvocationTargetException e) {
            throw new UnsupportedOperationException(getTokenName.getDeclaringClass().getName() + ".getTokenName()", e);
        }
        return result;
    }
}
