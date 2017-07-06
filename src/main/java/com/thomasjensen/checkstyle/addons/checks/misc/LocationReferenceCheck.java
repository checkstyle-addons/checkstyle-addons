package com.thomasjensen.checkstyle.addons.checks.misc;
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
import java.util.Locale;
import java.util.Set;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.thomasjensen.checkstyle.addons.checks.AbstractMethodCallCheck;
import com.thomasjensen.checkstyle.addons.checks.BinaryName;
import com.thomasjensen.checkstyle.addons.util.Util;


/**
 * This check compares specified method arguments to the names of the current method or class.
 * <p><a href="http://checkstyle-addons.thomasjensen.com/latest/checks/misc.html#LocationReference"
 * target="_blank">Documentation</a></p>
 */
public class LocationReferenceCheck
    extends AbstractMethodCallCheck
{
    private static final Set<Integer> TOKEN_TYPES = Collections.singleton(Integer.valueOf(TokenTypes.VARIABLE_DEF));

    private Set<String> methodCalls = Collections.emptySet();

    private Set<String> variableNames = Collections.emptySet();

    private LocationReferenceOption location = LocationReferenceOption.Method;

    private int argumentPosition = 0;



    @Override
    public Set<Integer> getRelevantTokens()
    {
        return Util.union(super.getRelevantTokens(), TOKEN_TYPES);
    }



    @Override
    protected boolean isCheckActive()
    {
        return !methodCalls.isEmpty() || !variableNames.isEmpty();
    }



    @Override
    protected boolean isRelevantCall(@Nullable final String pQualifier, @Nonnull final String pMethodName)
    {
        final String methodCall = (pQualifier != null ? (pQualifier + ".") : "") + pMethodName;
        return methodCalls.contains(methodCall);   // ignore variableNames here
    }



    @Override
    protected void visitMethodCall(@Nonnull final String pMethodName, @Nonnull final DetailAST pMethodCallAst)
    {
        // pMethodCallAst is a METHOD_CALL, CTOR_CALL, or SUPER_CTOR_CALL
        DetailAST argList = pMethodCallAst.findFirstToken(TokenTypes.ELIST);
        final DetailAST actualAst = findLocationArgument(argList);
        String actual = null;
        if (actualAst != null) {
            if (actualAst.getType() == TokenTypes.STRING_LITERAL) {
                actual = actualAst.getText().substring(1, actualAst.getText().length() - 1);  // no quotes
            }
            else {
                actual = Util.getFullIdent(actualAst);
            }
        }
        performCheck(actualAst, actual);
    }



    @CheckForNull
    private String getExpectedValue()
    {
        String expected = null;
        switch (location) {
            case Method:
                expected = getCurrentMethodName();
                break;

            case ClassObject: // fall through
            case SimpleClass:
                expected = getCurrentSimpleName();
                break;

            case FullClass:
                expected = getCurrentBinaryName().toString();
                expected = expected.replace('$', '.');
                break;

            default:
                throw new IllegalStateException("Unknown location reference: " + location);
        }
        return expected;
    }



    @CheckForNull
    private DetailAST findLocationArgument(@Nonnull final DetailAST pArgList) // ELIST
    {
        DetailAST result = null;
        final boolean backwards = argumentPosition < 0;
        int pos = backwards ? -1 : 0;
        for (DetailAST ast = backwards ? pArgList.getLastChild() : pArgList.getFirstChild(); ast != null;
            ast = backwards ? ast.getPreviousSibling() : ast.getNextSibling()) {
            if (ast.getType() == TokenTypes.COMMA) {
                continue;
            }
            if (pos == argumentPosition) {
                if (location == LocationReferenceOption.ClassObject) {
                    DetailAST a = ast.findFirstToken(TokenTypes.DOT);
                    if (a != null && a.getLastChild().getType() == TokenTypes.LITERAL_CLASS) {
                        result = a;
                    }
                }
                else {
                    DetailAST a = ast.getFirstChild();
                    if (a != null && a.getType() == TokenTypes.STRING_LITERAL) {
                        result = a;
                    }
                }
                break;
            }
            pos += backwards ? -1 : 1;
        }
        return result;
    }



    @Override
    protected void visitToken(@Nullable final BinaryName pBinaryClassName, @Nonnull final DetailAST pAst)
    {
        super.visitToken(pBinaryClassName, pAst);
        if (!variableNames.isEmpty() && pAst.getType() == TokenTypes.VARIABLE_DEF) {
            final String varName = pAst.findFirstToken(TokenTypes.IDENT).getText();
            final String varType = getTypeIdent(pAst);
            if (variableNames.contains(varName)) {
                DetailAST actualAst = null;
                String actual = null;
                final boolean expectString = location != LocationReferenceOption.ClassObject;
                if (expectString && String.class.getSimpleName().equals(varType)) {
                    actualAst = findStringLiteral(pAst);
                    if (actualAst != null) {
                        actual = actualAst.getText().substring(1, actualAst.getText().length() - 1);
                    }
                }
                else if (!expectString && Class.class.getSimpleName().equals(varType)) {
                    actualAst = findClassObject(pAst);
                    actual = actualAst.getFirstChild().getFirstChild().getText();
                }
                performCheck(actualAst, actual);
            }
        }
    }



    private void performCheck(@Nullable final DetailAST pActualAst, @Nullable final String pActual)
    {
        if (pActual != null) {
            final String expected = getExpectedValue();
            if (expected != null && !pActual.equals(expected) && pActualAst != null) {
                // there must be a clear expected *and* actual value, or we would not flag anything
                final DetailAST leftMostToken = Util.findLeftMostTokenInLine(pActualAst);
                log(leftMostToken, "locationreference.mismatch." + location.name().toLowerCase(Locale.ENGLISH),
                    expected);
            }
        }
    }



    @CheckForNull
    private DetailAST findClassObject(@Nonnull final DetailAST pAst)
    {
        DetailAST result = null;
        DetailAST ast = pAst.findFirstToken(TokenTypes.ASSIGN);
        if (ast != null) {
            final DetailAST expr = ast.findFirstToken(TokenTypes.EXPR);
            ast = expr.getFirstChild();  // DOT
            if (ast != null) {
                if (ast.getFirstChild().getType() == TokenTypes.IDENT
                    && ast.getLastChild().getType() == TokenTypes.LITERAL_CLASS) {
                    result = expr;
                }
            }
        }
        return result;
    }



    @CheckForNull
    private DetailAST findStringLiteral(@Nonnull final DetailAST pAst)
    {
        DetailAST result = null;
        DetailAST ast = pAst.findFirstToken(TokenTypes.ASSIGN);
        if (ast != null) {
            result = ast.findFirstToken(TokenTypes.EXPR).findFirstToken(TokenTypes.STRING_LITERAL);
        }
        return result;
    }



    @CheckForNull
    private String getTypeIdent(@Nonnull final DetailAST pAst)
    {
        String result = null;
        DetailAST ast = pAst.findFirstToken(TokenTypes.TYPE).findFirstToken(TokenTypes.IDENT);
        if (ast != null) {
            result = ast.getText();
        }
        return result;
    }



    /**
     * Setter.
     *
     * @param pMethodCalls list of qualified method calls to cover
     */
    public void setMethodCalls(final String... pMethodCalls)
    {
        final Set<String> newMethodCalls = new HashSet<String>();
        Collections.addAll(newMethodCalls, pMethodCalls);
        methodCalls = Collections.unmodifiableSet(newMethodCalls);
    }



    /**
     * Setter.
     *
     * @param pVariableNames list of variable names to cover
     */
    public void setVariableNames(final String... pVariableNames)
    {
        final Set<String> newVariableNames = new HashSet<String>();
        Collections.addAll(newVariableNames, pVariableNames);
        variableNames = Collections.unmodifiableSet(newVariableNames);
    }



    public void setLocation(final String pLocation)
    {
        location = LocationReferenceOption.valueOfIgnoreCase(pLocation);
    }



    public void setArgumentPosition(final int pArgumentPosition)
    {
        argumentPosition = pArgumentPosition;
    }
}
