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
import javax.annotation.Nullable;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.thomasjensen.checkstyle.addons.checks.AbstractMethodCallCheck;


/**
 * Flags calls to methods with certain names. Occurrences are flagged based on the name alone; the type of the object
 * to which the method belongs is not taken into account.
 * <p><a href="http://checkstyle-addons.thomasjensen.com/latest/checks/coding.html#IllegalMethodCall"
 * target="_blank">Documentation</a></p>
 */
public class IllegalMethodCallCheck
    extends AbstractMethodCallCheck
{
    private Set<String> illegalMethodNames = null;

    private Set<String> excludedQualifiers = new HashSet<String>();



    @Override
    protected boolean isCheckActive()
    {
        return illegalMethodNames != null && !illegalMethodNames.isEmpty();
    }



    @Override
    protected boolean isRelevantCall(@Nullable final String pQualifier, @Nonnull final String pMethodName)
    {
        return illegalMethodNames.contains(pMethodName) && !excludedQualifiers.contains(pQualifier);
    }



    @Override
    protected void visitMethodCall(@Nonnull final String pMethodName, @Nonnull final DetailAST pMethodCallAst)
    {
        DetailAST highlight = pMethodCallAst;
        if (pMethodCallAst.getType() == TokenTypes.METHOD_CALL) {
            highlight = pMethodCallAst.findFirstToken(TokenTypes.IDENT);
            if (highlight == null) {
                highlight = pMethodCallAst.findFirstToken(TokenTypes.DOT).getLastChild();
            }
        }
        log(highlight, "illegal.method.call", pMethodName);
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



    /**
     * Setter.
     *
     * @param pExcludedQualifiers the list of excluded contexts
     */
    public void setExcludedQualifiers(final String... pExcludedQualifiers)
    {
        final Set<String> newExclusions = new HashSet<String>();
        Collections.addAll(newExclusions, pExcludedQualifiers);
        excludedQualifiers = newExclusions;
    }
}
