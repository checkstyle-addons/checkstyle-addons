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

import java.util.Collections;
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
 *
 * @author Thomas Jensen
 */
public class RegexpOnStringCheck
    extends AbstractAddonsCheck
{
    private static final Set<Integer> TOKEN_TYPES = Collections.singleton(Integer.valueOf(TokenTypes.EXPR));

    private static final int MAX_OUPUT_STRING_LEN = 64;

    /** the given regexp */
    private Pattern regexp = Util.NEVER_MATCH;



    @Override
    public Set<Integer> getRelevantTokens()
    {
        return TOKEN_TYPES;
    }



    @Override
    protected void visitToken(@Nullable final BinaryName pBinaryClassName, @Nonnull final DetailAST pAst)
    {
        final DetailAST ast = pAst.getNumberOfChildren() == 1 ? pAst.getFirstChild() : null;
        if (ast != null && isStringExpression(ast)) {
            final String text = getConcatenation(ast);
            if (matchesWithoutQuotes(text)) {
                DetailAST hilite = findLiteralToHighlight(ast, true);
                if (hilite == null) {
                    hilite = findLiteralToHighlight(ast, false);
                }
                log(hilite, "regexp.string", clipText(text), regexp);
            }
        }
    }



    private String clipText(@Nonnull final String pText)
    {
        final String snipMark = "...";
        if (pText.length() - 2 > MAX_OUPUT_STRING_LEN) {  // pText includes quotes
            return pText.substring(0, MAX_OUPUT_STRING_LEN + 1 - snipMark.length()) + snipMark + "\"";
        }
        return pText;
    }



    private boolean matchesWithoutQuotes(@Nonnull final String pText)
    {
        return regexp.matcher(pText.substring(1, pText.length() - 1)).find();
    }



    private boolean isStringExpression(@Nonnull final DetailAST pAst)
    {
        if (pAst.getType() == TokenTypes.STRING_LITERAL) {
            return true;
        }
        if (pAst.getType() == TokenTypes.PLUS) {
            return isStringExpression(pAst.getFirstChild()) && isStringExpression(pAst.getLastChild());
        }
        return false;
    }



    @CheckForNull
    private String getConcatenation(@Nonnull final DetailAST pAst)
    {
        String result = null;
        if (pAst.getType() == TokenTypes.PLUS) {
            String left = getConcatenation(pAst.getFirstChild());
            String right = getConcatenation(pAst.getLastChild());
            if (left != null && right != null) {
                result = left.substring(0, left.length() - 1) + right.substring(1);    // without middle quotes
            }
        }
        else if (pAst.getType() == TokenTypes.STRING_LITERAL) {
            result = pAst.getText();
        }
        return result;
    }



    @CheckForNull
    private DetailAST findLiteralToHighlight(@Nonnull final DetailAST pAst, final boolean pUseMatcher)
    {
        DetailAST result = null;
        if (pAst.getType() == TokenTypes.STRING_LITERAL) {
            if (!pUseMatcher || matchesWithoutQuotes(pAst.getText())) {
                result = pAst;
            }
            else {
                result = null;
            }
        }
        else { // PLUS
            result = findLiteralToHighlight(pAst.getFirstChild(), pUseMatcher);
            if (result == null) {
                result = findLiteralToHighlight(pAst.getLastChild(), pUseMatcher);
            }
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
