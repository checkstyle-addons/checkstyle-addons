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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;


/**
 * Utility class.
 *
 * @author Thomas Jensen
 */
public final class Util
{
    /** A pattern that never matches (and does so efficiently) */
    public static final Pattern NEVER_MATCH = Pattern.compile("^(?!x)x");



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
     * Calls getCanonicalFile() on the given File; if that doesn't work, call getAbsoluteFile() on it.
     *
     * @param pFile a file
     * @return the canonical representation, or the absolute representation
     */
    @Nonnull
    public static File canonize(@Nonnull final File pFile)
    {
        File result = null;
        try {
            result = pFile.getCanonicalFile();
        }
        catch (IOException e) {
            result = pFile.getAbsoluteFile();
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
     * token which are located on the same line as the given AST are considered.
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
