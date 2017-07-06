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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.thomasjensen.checkstyle.addons.checks.AbstractAddonsCheck;
import com.thomasjensen.checkstyle.addons.checks.BinaryName;
import com.thomasjensen.checkstyle.addons.util.Util;


/**
 * This check applies a regular expression to String literals found in the source. <p><a
 * href="http://checkstyle-addons.thomasjensen.com/latest/checks/regexp.html#RegexpOnString"
 * target="_blank">Documentation</a></p>
 */
public class RegexpOnStringCheck
    extends AbstractAddonsCheck
{
    private static final Set<Integer> TOKEN_TYPES = Collections.singleton(Integer.valueOf(TokenTypes.EXPR));

    private static final int MAX_OUPUT_STRING_LEN = 64;

    /** the given regexp */
    private Pattern regexp = Util.NEVER_MATCH;



    /** An occurrence of a String found in a Java source file, together with its first AST. */
    private static class FoundString
    {
        private final StringBuilder sb;

        private final DetailAST ast;



        public FoundString(@Nonnull final String pString, @Nonnull final DetailAST pAst)
        {
            sb = new StringBuilder(pString);
            ast = pAst;
        }



        public void addString(@Nonnull final String pString)
        {
            sb.append(pString);
        }



        @Nonnull
        public String getString()
        {
            return sb.toString();
        }



        @Nonnull
        public DetailAST getAst()
        {
            return ast;
        }
    }



    @Override
    public Set<Integer> getRelevantTokens()
    {
        return TOKEN_TYPES;
    }



    @Override
    protected void visitToken(@Nullable final BinaryName pBinaryClassName, @Nonnull final DetailAST pAst)
    {
        final DetailAST ast = getExprAst(pAst);
        if (ast != null && containsStringLiteral(ast)) {
            final List<FoundString> foundStrings = findAllStrings(ast);
            for (final FoundString foundString : foundStrings) {
                final String text = foundString.getString();
                if (regexp.matcher(text).find()) {
                    log(foundString.getAst(), "regexp.string", clipText(text), regexp);
                }
            }
        }
    }



    /**
     * Find the "meaningful" child of an EXPR AST. This is usually the only child present, but it may be surrounded by
     * parentheses.
     *
     * @param pAst an EXPR AST
     * @return the meaningful child of the EXPR, or <code>null</code> if such could not be determined
     */
    @CheckForNull
    private DetailAST getExprAst(@Nonnull final DetailAST pAst)
    {
        DetailAST result = null;
        for (DetailAST a = pAst.getFirstChild(); a != null; a = a.getNextSibling()) {
            if (a.getType() != TokenTypes.LPAREN && a.getType() != TokenTypes.RPAREN) {
                result = a;
                break;
            }
        }
        return result;
    }



    private String clipText(@Nonnull final String pText)
    {
        final String snipMark = "...";
        if (pText.length() > MAX_OUPUT_STRING_LEN) {
            return pText.substring(0, MAX_OUPUT_STRING_LEN - snipMark.length()) + snipMark;
        }
        return pText;
    }



    private boolean containsStringLiteral(@Nonnull final DetailAST pAst)
    {
        if (pAst.getType() == TokenTypes.STRING_LITERAL) {
            return true;
        }
        if (pAst.getType() == TokenTypes.EXPR) {
            return false;
        }
        for (DetailAST a = pAst.getFirstChild(); a != null; a = a.getNextSibling()) {
            if (containsStringLiteral(a)) {
                return true;
            }
        }
        return false;
    }



    private void flatten(@Nonnull final DetailAST pAst, @Nonnull final List<DetailAST> pFlattenedList)
    {
        if (pAst.getType() == TokenTypes.PLUS) {
            boolean first = true;
            for (DetailAST a = pAst.getFirstChild(); a != null; a = a.getNextSibling()) {
                if (a.getType() != TokenTypes.LPAREN && a.getType() != TokenTypes.RPAREN) {
                    flatten(a, pFlattenedList);
                    if (first) {
                        pFlattenedList.add(pAst);
                        first = false;
                    }
                }
            }
        }
        else if (pAst.getType() == TokenTypes.EXPR) {
            return;
        }
        else {
            if (pAst.getNumberOfChildren() > 0) {
                for (DetailAST a = pAst.getFirstChild(); a != null; a = a.getNextSibling()) {
                    flatten(a, pFlattenedList);
                    if (a != pAst.getLastChild()) {
                        pFlattenedList.add(pAst);    // we don't care this gets added too often
                    }
                }
            }
            else {
                pFlattenedList.add(pAst);
            }
        }
    }



    @Nonnull
    private String removeQuotes(@Nonnull final String pString)
    {
        return pString.substring(1, pString.length() - 1);
    }



    @Nonnull
    private List<FoundString> findAllStrings(@Nonnull final DetailAST pAst)
    {
        final List<DetailAST> flattened = new ArrayList<DetailAST>();
        flatten(pAst, flattened);

        final List<FoundString> result = new ArrayList<FoundString>();
        FoundString current = null;
        for (final DetailAST a : flattened) {
            if (a.getType() == TokenTypes.STRING_LITERAL) {
                if (current != null) {
                    current.addString(removeQuotes(a.getText()));
                }
                else {
                    current = new FoundString(removeQuotes(a.getText()), a);
                }
            }
            else if (a.getType() != TokenTypes.PLUS) {
                if (current != null) {
                    result.add(current);
                    current = null;
                }
            }
        }
        if (current != null) {
            result.add(current);
        }
        return result;
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
}
