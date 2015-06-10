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
import java.io.IOException;
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
}
