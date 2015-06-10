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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.puppycrawl.tools.checkstyle.api.Check;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.thomasjensen.checkstyle.addons.util.Util;


/**
 * Base class of all Checkstyle Addons checks.
 *
 * @author Thomas Jensen
 */
public abstract class AbstractAddonsCheck
    extends Check
{
    private static final Set<Integer> TOKENS = Collections.unmodifiableSet(new HashSet<Integer>(Arrays.asList(
        TokenTypes.PACKAGE_DEF, TokenTypes.ENUM_DEF, TokenTypes.CLASS_DEF, TokenTypes.INTERFACE_DEF,
        TokenTypes.ANNOTATION_DEF)));

    /** Flag used to cancel checking an input class (in case of errors in the check) */
    private boolean stopChecking = false;

    /** fully qualified name of the outer (or only) class */
    private String iOuterClassName = null;

    /** Map from binary class names to ASTs */
    private final Map<String, DetailAST> iClassDeclarationPositions = new HashMap<String, DetailAST>();

    /** CLASS_DEF/IDENTs as encountered */
    private final Deque<String> iClassDefStack = new LinkedList<String>();

    /** Encountered binary class names in the current Java file */
    private final Deque<String> iBinaryNameStack = new LinkedList<String>();

    /** Package that the currently checked class resides in */
    private String iMyPackage = null;



    protected AbstractAddonsCheck()
    {
        super();
    }



    @Override
    public final int[] getDefaultTokens()
    {
        final Set<Integer> tokens = new TreeSet<Integer>();
        tokens.addAll(TOKENS);
        tokens.addAll(getRelevantTokens());

        final int[] result = new int[tokens.size()];
        int i = 0;
        for (Integer token : tokens) {
            result[i++] = token.intValue();
        }
        return result;
    }



    /**
     * The tokens which this check is interested in. Will be added to the tokens of the base class.
     *
     * @return the relevant tokens
     */
    public abstract Set<Integer> getRelevantTokens();



    @Override
    public int[] getRequiredTokens()
    {
        return getDefaultTokens();
    }



    @Override
    public void beginTree(final DetailAST pRootAst)
    {
        super.beginTree(pRootAst);
        if (isCheckStopped()) {
            return;
        }

        iClassDeclarationPositions.clear();
        iBinaryNameStack.clear();
        iOuterClassName = null;
        iMyPackage = null;
        iClassDefStack.clear();
    }



    @Override
    public final void finishTree(final DetailAST pRootAst)
    {
        super.finishTree(pRootAst);
        if (isCheckStopped()) {
            return;
        }
        finishTree(iOuterClassName, pRootAst);
    }



    /**
     * Called after a tree is fully processed. Ideal place to report on information collected whilst processing a tree.
     *
     * @param pFqcn the fully qualified class name of the outer class
     * @param pRootAst the root of the tree
     */
    protected void finishTree(@Nonnull final String pFqcn, @Nonnull final DetailAST pRootAst)
    {
        // optionally filled in by subclass
    }



    /**
     * Called to process a token.
     * @param pBinaryClassName the currently active binary class name
     * @param pAst the token to process
     */
    protected void visitToken(@Nullable final String pBinaryClassName, @Nonnull final DetailAST pAst)
    {
        // optionally filled in by subclass
    }



    /**
     * Called after all the child nodes have been processed.
     * @param pBinaryClassName the currently active binary class name
     * @param pAst the token being completed
     */
    protected void leaveToken(@Nullable final String pBinaryClassName, @Nonnull final DetailAST pAst)
    {
        // optionally filled in by subclass
    }



    /**
     * Called after visiting a CLASS_DEF, INTERFACE_DEF, ANNOTATION_DEF, or ENUM_DEF token and successfully
     * determining the type's binary class name. This is useful in a Java source file with nested inner classes.
     * If the implementing check registers for any of the above tokens, the regular call to
     * {@link #visitToken(String, DetailAST)} will be performed in addition to (after) this one.
     * @param pBinaryClassName the binary class name of the visited type
     * @param pAst the token to process
     */
    protected void visitKnownType(@Nonnull final String pBinaryClassName, @Nonnull final DetailAST pAst)
    {
        // optionally filled in by subclass
    }



    /**
     * Called after leaving a CLASS_DEF, INTERFACE_DEF, ANNOTATION_DEF, or ENUM_DEF token and the type's binary class
     * name is known. This is useful in a Java source file with nested inner classes.
     * If the implementing check registers for any of the above tokens, the regular call to
     * {@link #leaveToken(String, DetailAST)} will be performed in addition to (before) this one.
     * @param pBinaryClassName the binary class name of the visited type
     * @param pAst the token being completed
     */
    protected void leaveKnownType(@Nonnull final String pBinaryClassName, @Nonnull final DetailAST pAst)
    {
        // optionally filled in by subclass
    }



    @Override
    public final void visitToken(final DetailAST pAst)
    {
        super.visitToken(pAst);
        if (isCheckStopped()) {
            return;
        }

        switch (pAst.getType()) {
            case TokenTypes.PACKAGE_DEF:
                final String pkg = Util.getFullIdent(pAst);
                iMyPackage = pkg != null ? pkg : "";
                break;
            case TokenTypes.CLASS_DEF: // fall through
            case TokenTypes.INTERFACE_DEF: // fall through
            case TokenTypes.ANNOTATION_DEF: // fall through
            case TokenTypes.ENUM_DEF:
                visitClassDef(pAst);
                visitKnownType(getCurrentBinaryName(), pAst);
                break;
            default:
                // some other token defined by the subclass
                break;
        }

        if (getRelevantTokens().contains(Integer.valueOf(pAst.getType()))) {
            visitToken(getCurrentBinaryName(), pAst);
        }
    }



    private void visitClassDef(final DetailAST pAst)
    {
        final String simpleName = Util.getFirstIdent(pAst);
        iClassDefStack.push(simpleName);

        StringBuilder sb = new StringBuilder();
        if (iMyPackage != null) {
            sb.append(iMyPackage);
            sb.append('.');
        }
        for (Iterator<String> iter = iClassDefStack.descendingIterator(); iter.hasNext();) {
            sb.append(iter.next());
            if (iter.hasNext()) {
                sb.append('$');
            }
        }
        final String binaryName = sb.toString();

        iBinaryNameStack.push(binaryName);
        iClassDeclarationPositions.put(binaryName, pAst);
        if (iOuterClassName == null) {
            iOuterClassName = binaryName;
        }
    }



    @CheckForNull
    protected DetailAST getClassDeclarationPosition(@Nonnull final String pBinaryName)
    {
        return iClassDeclarationPositions.get(pBinaryName);
    }



    @Override
    public final void leaveToken(final DetailAST pAst)
    {
        super.leaveToken(pAst);
        if (isCheckStopped()) {
            return;
        }

        if (getRelevantTokens().contains(Integer.valueOf(pAst.getType()))) {
            leaveToken(getCurrentBinaryName(), pAst);
        }

        switch (pAst.getType()) {
            case TokenTypes.PACKAGE_DEF:
                // ignore
                break;
            case TokenTypes.CLASS_DEF: // fall through
            case TokenTypes.INTERFACE_DEF: // fall through
            case TokenTypes.ANNOTATION_DEF: // fall through
            case TokenTypes.ENUM_DEF:
                leaveKnownType(getCurrentBinaryName(), pAst);
                iClassDefStack.pop();
                iBinaryNameStack.pop();
                break;
            default:
                // some other token defined by subclass
                break;
        }
    }




    /**
     * Gets the binary name of the class currently being traversed on the AST. Normally, this is the single class in the
     * .java file, but it could also be a (nested) named inner class. Whenever a new CLASS_DEF or similar token is
     * visited, the result of this method would change.
     *
     * @return current binary name, or <code>null</code> if no CLASS_DEF or similar token has been encountered yet
     * (e.g. while we are still going through the <code>import</code> statements)
     */
    @CheckForNull
    protected String getCurrentBinaryName()
    {
        return iBinaryNameStack.peek();
    }



    protected String getMyPackage()
    {
        return iMyPackage;
    }



    protected boolean isCheckStopped()
    {
        return stopChecking;
    }



    protected void stopChecking()
    {
        stopChecking = true;
    }
}
