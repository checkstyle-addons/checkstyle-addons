package com.thomasjensen.checkstyle.addons.checks;
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
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.thomasjensen.checkstyle.addons.util.Util;


/**
 * Checks deriving from this type are aware of method calls occurring anywhere in the source, and get explicitly
 * notified when such calls are encountered. The full qualifier of the call is provided.
 */
public abstract class AbstractMethodCallCheck
    extends AbstractAddonsCheck
{
    private static final Set<Integer> TOKEN_TYPES = Collections.unmodifiableSet(new HashSet<Integer>(Arrays.asList(
        Integer.valueOf(TokenTypes.METHOD_CALL), Integer.valueOf(TokenTypes.METHOD_DEF), Integer.valueOf(
            TokenTypes.CTOR_CALL), Integer.valueOf(TokenTypes.SUPER_CTOR_CALL), Integer.valueOf(TokenTypes.CTOR_DEF),
        Integer.valueOf(TokenTypes.STATIC_INIT), Integer.valueOf(TokenTypes.INSTANCE_INIT))));

    private final Deque<String> currentMethodName = new LinkedList<String>();



    @Override
    public Set<Integer> getRelevantTokens()
    {
        return TOKEN_TYPES;
    }



    /**
     * Determines if the check is configured to do anything at all.
     *
     * @return <code>true</code> if processing should be performed
     */
    protected abstract boolean isCheckActive();



    /**
     * Filter method which determines if the given method call is considered relevant.
     *
     * @param pQualifier the <em>qualifier</em> of the method call (for a call like <code>Foo.Bar.call()</code>, the
     * qualifier is <code>Foo.Bar</code>)
     * @param pMethodName the simple name of the called method
     * @return indication of whether the call is relevant (<code>true</code>) or not (<code>false</code>)
     */
    protected abstract boolean isRelevantCall(@Nullable String pQualifier, @Nonnull String pMethodName);



    /**
     * Visitor method called when a relevant method call is encountered. This method is called in addition to the other
     * visitor methods.
     *
     * @param pMethodName the simple name of the called method
     * @param pMethodCallAst AST of the call, useful for logging issues ( this is a METHOD_DEF, CTOR_CALL, or a
     * SUPER_CTOR_CALL token)
     */
    protected abstract void visitMethodCall(@Nonnull String pMethodName, @Nonnull DetailAST pMethodCallAst);



    /**
     * The current method name. <code>"&lt;init&gt;"</code> is used for constructors and instance initializers, and
     * <code>"&lt;clinit&gt;"</code> is used for static initializers.
     *
     * @return the method name, or <code>null</code> if we are not inside a method, constructor, or initializer
     */
    @CheckForNull
    public String getCurrentMethodName()
    {
        return currentMethodName.peek();
    }



    @Override
    public void beginTree(final DetailAST pRootAst)
    {
        super.beginTree(pRootAst);
        currentMethodName.clear();
    }



    @Override
    protected void visitToken(@Nullable final BinaryName pBinaryClassName, @Nonnull final DetailAST pAst)
    {
        if (isCheckActive()) {
            /*
             * something is called
             */
            if (pAst.getType() == TokenTypes.METHOD_CALL) {
                visitCall(pAst, false);
            }
            else if (pAst.getType() == TokenTypes.CTOR_CALL || pAst.getType() == TokenTypes.SUPER_CTOR_CALL) {
                visitCall(pAst, true);  // "this" or "super"
            }

            /*
             * tracking of current method name
             */
            else if (pAst.getType() == TokenTypes.METHOD_DEF) {
                currentMethodName.push(pAst.findFirstToken(TokenTypes.IDENT).getText());
            }
            else if (pAst.getType() == TokenTypes.CTOR_DEF || pAst.getType() == TokenTypes.INSTANCE_INIT) {
                currentMethodName.push("<init>");
            }
            else if (pAst.getType() == TokenTypes.STATIC_INIT) {
                currentMethodName.push("<clinit>");
            }
        }
    }



    private void visitCall(@Nonnull final DetailAST pAst, final boolean pKeyword)
    {
        // pAst is a METHOD_CALL, CTOR_CALL, or SUPER_CTOR_CALL
        final String calledMethodName = pKeyword ? pAst.getText() : findCalledMethodName(pAst).getText();
        final String qualifier = pKeyword ? null : extractQualifier(pAst, calledMethodName);

        if (isRelevantCall(qualifier, calledMethodName)) {
            visitMethodCall(calledMethodName, pAst);
        }
    }



    @Override
    protected void leaveToken(@Nullable final BinaryName pBinaryClassName, @Nonnull final DetailAST pAst)
    {
        /*
         * tracking of current method name
         */
        if (isCheckActive()) {
            if (pAst.getType() == TokenTypes.METHOD_DEF || pAst.getType() == TokenTypes.CTOR_DEF
                || pAst.getType() == TokenTypes.STATIC_INIT || pAst.getType() == TokenTypes.INSTANCE_INIT) {
                currentMethodName.pop();
            }
        }
    }



    @CheckForNull
    private String extractQualifier(@Nonnull final DetailAST pAst, @Nonnull final String pMethodName)
    {
        String result = null;
        final String fullCall = Util.getFullIdent(pAst);
        if (fullCall != null && fullCall.length() > pMethodName.length() + 1) {
            int sepDotPos = fullCall.length() - pMethodName.length() - 1;
            if (fullCall.charAt(sepDotPos) == '.') {
                result = fullCall.substring(0, sepDotPos);
            }
        }
        return result;
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
            throw new IllegalStateException("Unexpected token type: " + getApiFixer().getTokenName(
                firstChild.getType()));
        }
    }
}
