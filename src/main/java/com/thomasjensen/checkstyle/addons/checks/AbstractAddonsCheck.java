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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.thomasjensen.checkstyle.addons.util.CheckstyleApiFixer;
import com.thomasjensen.checkstyle.addons.util.Util;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;


/**
 * Base class of all Checkstyle Addons checks.
 */
@SuppressFBWarnings("ACEM_ABSTRACT_CLASS_EMPTY_METHODS")
public abstract class AbstractAddonsCheck
    extends AbstractCheck
{
    private static final Set<Integer> TOKENS = Collections.unmodifiableSet(new HashSet<Integer>(Arrays
        .asList(TokenTypes.PACKAGE_DEF, TokenTypes.ENUM_DEF, TokenTypes.CLASS_DEF, TokenTypes.INTERFACE_DEF,
            TokenTypes.ANNOTATION_DEF)));

    /** binary name of the outer (or only) class */
    private BinaryName iOuterClassName = null;

    /** Map from binary class names to ASTs */
    private final Map<BinaryName, DetailAST> iClassDeclarationPositions = new HashMap<BinaryName, DetailAST>();

    /** CLASS_DEF/IDENTs as encountered */
    private final Deque<String> iClassDefStack = new LinkedList<String>();

    /** Encountered binary class names in the current Java file */
    private final Deque<BinaryName> iBinaryNameStack = new LinkedList<BinaryName>();

    /** Package that the currently checked class resides in */
    private String iMyPackage = null;

    private final CheckstyleApiFixer apiFixer;



    protected AbstractAddonsCheck()
    {
        this(null);
    }



    protected AbstractAddonsCheck(@Nullable final String pMockfile)
    {
        super();
        apiFixer = new CheckstyleApiFixer(this, pMockfile);
    }



    /**
     * The tokens which this check is interested in. Will be added to the tokens of the base class.
     *
     * @return the relevant tokens
     */
    public abstract Set<Integer> getRelevantTokens();



    @Override
    public final int[] getRequiredTokens()
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



    @Override
    public final int[] getAcceptableTokens()
    {
        return getRequiredTokens();
    }



    @Override
    public final int[] getDefaultTokens()
    {
        return getRequiredTokens();
    }



    @Override
    public void beginTree(final DetailAST pRootAst)
    {
        super.beginTree(pRootAst);

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
        finishTree(iOuterClassName, pRootAst);
    }



    /**
     * Called after a tree is fully processed. Ideal place to report on information collected whilst processing a tree.
     *
     * @param pOuterClassName the fully qualified class name of the outer class
     * @param pRootAst the root of the tree
     */
    @SuppressWarnings({"unused", "EmptyMethod"})
    protected void finishTree(@Nonnull final BinaryName pOuterClassName, @Nonnull final DetailAST pRootAst)
    {
        // optionally filled in by subclass
    }



    /**
     * Called to process a token.
     *
     * @param pBinaryClassName the currently active binary class name
     * @param pAst the token to process
     */
    @SuppressWarnings("unused")
    protected void visitToken(@Nullable final BinaryName pBinaryClassName, @Nonnull final DetailAST pAst)
    {
        // optionally filled in by subclass
    }



    /**
     * Called after all the child nodes have been processed.
     *
     * @param pBinaryClassName the currently active binary class name
     * @param pAst the token being completed
     */
    @SuppressWarnings("unused")
    protected void leaveToken(@Nullable final BinaryName pBinaryClassName, @Nonnull final DetailAST pAst)
    {
        // optionally filled in by subclass
    }



    /**
     * Called after visiting a CLASS_DEF, INTERFACE_DEF, ANNOTATION_DEF, or ENUM_DEF token and successfully determining
     * the type's binary class name. This is useful in a Java source file with nested inner classes. If the implementing
     * check registers for any of the above tokens, the regular call to {@link #visitToken(BinaryName, DetailAST)} will
     * be performed in addition to (after) this one.
     *
     * @param pBinaryClassName the binary class name of the visited type
     * @param pAst the token to process
     */
    @SuppressWarnings("unused")
    protected void visitKnownType(@Nonnull final BinaryName pBinaryClassName, @Nonnull final DetailAST pAst)
    {
        // optionally filled in by subclass
    }



    /**
     * Called after leaving a CLASS_DEF, INTERFACE_DEF, ANNOTATION_DEF, or ENUM_DEF token and the type's binary class
     * name is known. This is useful in a Java source file with nested inner classes. If the implementing check
     * registers for any of the above tokens, the regular call to {@link #leaveToken(BinaryName, DetailAST)} will be
     * performed in addition to (before) this one.
     *
     * @param pBinaryClassName the binary class name of the visited type
     * @param pAst the token being completed
     */
    @SuppressWarnings("unused")
    protected void leaveKnownType(@Nonnull final BinaryName pBinaryClassName, @Nonnull final DetailAST pAst)
    {
        // optionally filled in by subclass
    }



    @Override
    public final void visitToken(final DetailAST pAst)
    {
        super.visitToken(pAst);

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

        final List<String> names = new ArrayList<String>(iClassDefStack);
        Collections.reverse(names);
        final BinaryName binaryName = new BinaryName(iMyPackage, names);

        iBinaryNameStack.push(binaryName);
        iClassDeclarationPositions.put(binaryName, pAst);
        if (iOuterClassName == null) {
            iOuterClassName = binaryName;
        }
    }



    @CheckForNull
    @SuppressWarnings("unused")
    protected DetailAST getClassDeclarationPosition(@Nonnull final BinaryName pBinaryName)
    {
        return iClassDeclarationPositions.get(pBinaryName);
    }



    @Override
    public final void leaveToken(final DetailAST pAst)
    {
        super.leaveToken(pAst);

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
     * @return current binary name, or <code>null</code> if no CLASS_DEF or similar token has been encountered yet (e.g.
     * while we are still going through the <code>import</code> statements)
     */
    @CheckForNull
    protected BinaryName getCurrentBinaryName()
    {
        return iBinaryNameStack.peek();
    }



    /**
     * Gets the simple name of the current class, interface, annotation, or enum.
     *
     * @return the simple class name, or <code>null</code> if we are outside of a type definition
     */
    @CheckForNull
    protected String getCurrentSimpleName()
    {
        return iClassDefStack.peek();
    }



    @SuppressWarnings("unused")
    protected String getMyPackage()
    {
        return iMyPackage;
    }



    @Nonnull
    protected CheckstyleApiFixer getApiFixer()
    {
        return apiFixer;
    }
}
