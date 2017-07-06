package com.thomasjensen.checkstyle.addons.util;
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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;


/**
 * Utility class.
 */
public final class Util
{
    /** A pattern that never matches (and does so efficiently) */
    public static final Pattern NEVER_MATCH = Pattern.compile("^(?!x)x");

    /** Constant for UTF-8 charset; can be replaced with StandardCharsets.UTF_8 after moving to Java 7 */
    public static final Charset UTF8 = Charset.forName("UTF-8");

    /** Size of the file I/O buffer in bytes */
    private static final int IO_BUFFER_SIZE_BYTES = 8000;



    private Util()
    {
        super();
    }



    /**
     * Determine the text of the first direct IDENT child node.
     *
     * @param pAst an AST
     * @return the first encountered IDENT text, or <code>null</code> if none was found
     */
    @CheckForNull
    public static String getFirstIdent(@Nonnull final DetailAST pAst)
    {
        String result = null;
        DetailAST ast = pAst.findFirstToken(TokenTypes.IDENT);
        if (ast != null) {
            result = ast.getText();
        }
        return result;
    }



    /**
     * Determine the full identifier of the current element on the AST. The identifier is built from DOT and IDENT
     * elements found directly below the specified element. Other elements encountered are ignored.
     *
     * @param pAst an AST
     * @return the full identifier constructed from either the first encountered IDENT or DOT; <code>null</code> if no
     * identifier could be constructed
     */
    @CheckForNull
    public static String getFullIdent(@Nonnull final DetailAST pAst)
    {
        String result = null;
        DetailAST ast = checkTokens(pAst.getFirstChild(), TokenTypes.DOT, TokenTypes.IDENT);
        if (ast != null) {
            StringBuilder sb = new StringBuilder();
            if (getFullIdentInternal(ast, sb)) {
                result = sb.toString();
            }
        }
        return result;
    }



    private static boolean getFullIdentInternal(@Nonnull final DetailAST pDotOrIdent, @Nonnull final StringBuilder pSb)
    {
        // pDotOrIdent was an IDENT
        if (pDotOrIdent.getType() == TokenTypes.IDENT) {
            pSb.append(pDotOrIdent.getText());
            return true;
        }

        // pDotOrIdent was a DOT
        DetailAST ast = checkTokens(pDotOrIdent.getFirstChild(), TokenTypes.DOT, TokenTypes.IDENT);
        if (ast != null) {
            getFullIdentInternal(ast, pSb);
            pSb.append('.');
            ast = checkTokens(ast.getNextSibling(), TokenTypes.IDENT);
            if (ast != null) {
                getFullIdentInternal(ast, pSb);
                return true;
            }
        }
        return false;
    }



    @CheckForNull
    private static DetailAST checkTokens(@Nonnull final DetailAST pAst, @Nonnull final int... pTokens)
    {
        for (DetailAST ast = pAst; ast != null; ast = ast.getNextSibling()) {
            for (int token : pTokens) {
                if (token == ast.getType()) {
                    return ast;
                }
            }
        }
        return null;
    }



    /**
     * Quietly close the given resource, ignoring any exceptions.
     *
     * @param pCloseable the resource to close
     */
    public static void closeQuietly(@Nullable final Closeable pCloseable)
    {
        if (pCloseable != null) {
            try {
                pCloseable.close();
            }
            catch (IOException e) {
                // ignore
            }
        }
    }



    /**
     * Read all bytes from an InputStream into a byte array. Can be replaced with <code>Files.readAllBytes()</code> once
     * the code is migrated to Java 7.
     *
     * @param pInputStream the input stream
     * @return the complete contents read from the input stream
     *
     * @throws IOException I/O error
     */
    public static byte[] readBytes(@Nonnull final InputStream pInputStream)
        throws IOException
    {
        ByteArrayOutputStream baos = null;
        BufferedInputStream bis = null;
        try {
            baos = new ByteArrayOutputStream(IO_BUFFER_SIZE_BYTES);
            bis = new BufferedInputStream(pInputStream, IO_BUFFER_SIZE_BYTES);
            byte[] buffer = new byte[IO_BUFFER_SIZE_BYTES];
            for (int bytesRead = bis.read(buffer); bytesRead > 0; bytesRead = bis.read(buffer)) {
                baos.write(buffer, 0, bytesRead);
            }
        }
        finally {
            closeQuietly(bis);
            closeQuietly(baos);
        }

        return baos.toByteArray();
    }



    /**
     * Calls getCanonicalFile() on the given File; if that doesn't work, call getAbsoluteFile() on it. Thus, the
     * resulting file is not guaranteed to exist. Separator characters are standardized to {@link File#separatorChar}.
     *
     * @param pFile a file
     * @return the canonical representation, or the absolute representation
     */
    @Nonnull
    public static File canonize(@Nonnull final File pFile)
    {
        String resultPath = null;
        try {
            resultPath = pFile.getCanonicalPath();
        }
        catch (IOException e) {
            resultPath = pFile.getAbsolutePath();
        }
        return new File(standardizeSlashes(resultPath));
    }



    /**
     * Standardizes all slashes and backslashes in the given String to {@link File#separatorChar}.
     *
     * @param pPath a String
     * @return a new String which is <code>pPath</code> with standardized path separator characters
     */
    @Nonnull
    public static String standardizeSlashes(@Nonnull final String pPath)
    {
        final String goodSlash = File.separator;
        final String badSlash = File.separatorChar == '/' ? "\\" : "/";
        // The JDK also assumes there are only these two options.
        String result = pPath.replaceAll(Pattern.quote(badSlash), Matcher.quoteReplacement(goodSlash));
        return result;
    }



    /**
     * Search for a String in a collection.
     *
     * @param pIterable the list of String to be searched
     * @param pSearched the String to search for
     * @param pCaseSensitive if comparisons should be case sensitive
     * @return <code>true</code> if found, <code>false</code> otherwise
     */
    public static boolean containsString(@Nullable final Iterable<String> pIterable, @Nullable final String pSearched,
        final boolean pCaseSensitive)
    {
        boolean result = false;
        if (pSearched != null && pIterable != null) {
            for (String s : pIterable) {
                if (stringEquals(pSearched, s, pCaseSensitive)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }



    /**
     * Determine the equality of two Strings, optionally ignoring case.
     *
     * @param pStr1 the first String
     * @param pStr2 the second String
     * @param pCaseSensitive if the comparison should be case sensitive
     * @return <code>true</code> if the String are equal, <code>false</code> otherwise
     */
    public static boolean stringEquals(@Nullable final String pStr1, @Nullable final String pStr2,
        final boolean pCaseSensitive)
    {
        boolean result = false;
        if (pStr1 == null && pStr2 == null) {
            result = true;
        }
        else if (pStr1 != null) {
            if (pCaseSensitive) {
                result = pStr1.equals(pStr2);
            }
            else {
                result = pStr1.equalsIgnoreCase(pStr2);
            }
        }
        return result;
    }



    /**
     * Creates a new immutable {@link HashSet} which contains a union of the two given sets.
     *
     * @param pColl1 first set
     * @param pColl2 second set
     * @param <E> type of all contained elements
     * @return union set
     */
    @Nonnull
    public static <E> Set<E> union(@Nullable final Set<E> pColl1, @Nullable final Set<E> pColl2)
    {
        final Set<E> result = new HashSet<E>();
        if (pColl1 != null) {
            result.addAll(pColl1);
        }
        if (pColl2 != null) {
            result.addAll(pColl2);
        }
        return Collections.unmodifiableSet(result);
    }



    /**
     * Find the left-most token in the given AST. The left-most token is the token with the smallest column number. Only
     * tokens which are located on the same line as the given AST are considered.
     *
     * @param pAst the root of a subtree. This token is also considered for the result.
     * @return the left-most token
     */
    @Nonnull
    public static DetailAST findLeftMostTokenInLine(@Nonnull final DetailAST pAst)
    {
        return findLeftMostTokenInLineInternal(pAst, pAst.getLineNo(), pAst.getColumnNo());
    }



    @Nonnull
    private static DetailAST findLeftMostTokenInLineInternal(@Nonnull final DetailAST pAst, final int pLine,
        final int pColumn)
    {
        DetailAST result = pAst;
        int col = pColumn;
        for (DetailAST ast = pAst.getFirstChild(); ast != null; ast = ast.getNextSibling()) {
            if (ast.getLineNo() > pLine) {
                break;
            }
            int currentCol = ast.getColumnNo();
            if (currentCol < col) {
                col = currentCol;
                result = ast;
            }

            DetailAST subTree = findLeftMostTokenInLineInternal(ast, pLine, col);
            currentCol = subTree.getColumnNo();
            if (currentCol < col) {
                col = currentCol;
                result = subTree;
            }
        }
        return result;
    }



    /**
     * Variant of {@link Enum#valueOf} that ignores value case.
     *
     * @param pValue the String value
     * @param pEnumClass the class object of the enum type
     * @param <E> the enum type
     * @return the enum value
     *
     * @throws IllegalArgumentException the given String value does not match a valid enum value
     */
    @Nonnull
    public static <E extends Enum<E>> E valueOfIgnoreCase(@Nonnull final String pValue,
        @Nonnull final Class<E> pEnumClass)
    {
        for (E e : pEnumClass.getEnumConstants()) {
            if (e.name().equalsIgnoreCase(pValue)) {
                return e;
            }
        }
        throw new IllegalArgumentException("illegal value for " + pEnumClass.getSimpleName() + ": " + pValue);
    }
}
