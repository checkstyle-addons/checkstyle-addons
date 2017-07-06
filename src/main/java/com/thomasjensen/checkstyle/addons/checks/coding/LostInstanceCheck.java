package com.thomasjensen.checkstyle.addons.checks.coding;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.thomasjensen.checkstyle.addons.checks.AbstractAddonsCheck;
import com.thomasjensen.checkstyle.addons.checks.BinaryName;


/**
 * Checks that object instances created explicitly with <code>new</code> are actually used for something. Just being
 * assigned to a variable or passed as a parameter is enough. A full data flow analysis is not performed.
 * <p><a href="http://checkstyle-addons.thomasjensen.com/latest/checks/coding.html#LostInstance"
 * target="_blank">Documentation</a></p>
 */
public class LostInstanceCheck
    extends AbstractAddonsCheck
{
    /**
     * List of tokens that, when occurring as a parent token of LITERAL_NEW, indicate that LITERAL_NEW does not stand
     * alone.
     */
    private static final int[] GOOD_PARENTS =
        new int[]{TokenTypes.ASSIGN, TokenTypes.ANNOTATION_MEMBER_VALUE_PAIR, TokenTypes.ARRAY_INIT, TokenTypes.ELIST,
            TokenTypes.FOR_EACH_CLAUSE, TokenTypes.LITERAL_RETURN, TokenTypes.LITERAL_THROW};

    /**
     * List of tokens that, when occurring as a parent token of LITERAL_NEW, indicate that LITERAL_NEW stands alone.
     */
    private static final int[] BAD_PARENTS =
        new int[]{TokenTypes.SLIST, TokenTypes.LITERAL_IF, TokenTypes.LITERAL_ELSE, TokenTypes.LITERAL_FOR,
            TokenTypes.LITERAL_DO, TokenTypes.LITERAL_WHILE};



    @Override
    public void init()
    {
        super.init();
        Arrays.sort(GOOD_PARENTS);
        Arrays.sort(BAD_PARENTS);
    }



    @Override
    public Set<Integer> getRelevantTokens()
    {
        return Collections.singleton(Integer.valueOf(TokenTypes.LITERAL_NEW));
    }



    @Override
    public void visitToken(@Nullable final BinaryName pBinaryClassName, @Nonnull final DetailAST pAst)
    {
        boolean isLost = false;

        if (!isBeingDereferenced(pAst)) {
            for (DetailAST a = pAst.getParent(); a.getType() != TokenTypes.EOF; a = a.getParent()) {
                if (a.getType() == TokenTypes.ELIST) {
                    final int parentType = a.getParent().getType();
                    if (parentType == TokenTypes.FOR_INIT || parentType == TokenTypes.FOR_ITERATOR) {
                        isLost = true;
                    }
                    break;
                }
                if (Arrays.binarySearch(GOOD_PARENTS, a.getType()) >= 0) {
                    break;
                }
                if (Arrays.binarySearch(BAD_PARENTS, a.getType()) >= 0) {
                    isLost = true;
                    break;
                }
            }
        }

        if (isLost) {
            log(pAst, "lost.instance");
        }
    }



    /**
     * Determine if the instance created with <code>new</code> ist followed by a dot. If so, it is being used for
     * something, so its existence is not considered useless.
     *
     * @param pLiteralNew the current LITERAL_NEW token found by the visitor
     * @return <code>true</code> if the instance is being dereferenced
     */
    private boolean isBeingDereferenced(final DetailAST pLiteralNew)
    {
        boolean result = false;

        final DetailAST parent = pLiteralNew.getParent();
        if (parent.getType() == TokenTypes.DOT && (pLiteralNew.getNextSibling() != null
            || parent.getParent().getType() == TokenTypes.DOT)) {
            result = true;
        }
        return result;
    }
}
