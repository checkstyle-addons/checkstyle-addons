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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;

import com.puppycrawl.tools.checkstyle.api.Check;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;


/**
 * Flags calls to methods with certain names. Occurrences are flagged based on the name alone; the type of the object to
 * which the method belongs is not taken into account.
 * <p/>
 * <a href="http://checkstyle-addons.thomasjensen.com/latest/checks/coding.html#IllegalMethodCall"
 * target="_blank">Documentation</a>
 *
 * @author Thomas Jensen
 */
public class IllegalMethodCallCheck
    extends Check
{
    private Set<String> illegalMethodNames = null;



    @Override
    public int[] getDefaultTokens()
    {
        return new int[]{TokenTypes.METHOD_CALL};
    }



    @Override
    public int[] getRequiredTokens()
    {
        return getDefaultTokens();
    }



    @Override
    public void visitToken(final DetailAST pAst)
    {
        if (illegalMethodNames != null && !illegalMethodNames.isEmpty()) {
            final DetailAST methodNameAst = findCalledMethodName(pAst);
            final String methodName = methodNameAst.getText();
            if (illegalMethodNames.contains(methodName)) {
                log(methodNameAst, "illegal.method.call", methodName);
            }
        }
    }



    @Nonnull
    private DetailAST findCalledMethodName(@Nonnull final DetailAST pAst)
    {
        final DetailAST firstChild = pAst.getFirstChild();
        if (firstChild.getType() == TokenTypes.IDENT) {
            return firstChild;
        }
        else if (firstChild.getType() == TokenTypes.DOT) {
            return firstChild.getLastChild();
        }
        else {
            throw new IllegalStateException("Unexpected token type: " + TokenTypes.getTokenName(firstChild.getType()));
        }
    }



    /**
     * Setter.
     *
     * @param pIllegalMethodNames the list of method names to flag
     */
    public void setIllegalMethodNames(final String... pIllegalMethodNames)
    {
        final Set<String> methodNames = new HashSet<String>();
        Collections.addAll(methodNames, pIllegalMethodNames);
        illegalMethodNames = methodNames;
    }
}
