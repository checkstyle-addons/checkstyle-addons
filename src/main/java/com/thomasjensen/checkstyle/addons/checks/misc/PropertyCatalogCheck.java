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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import com.thomasjensen.checkstyle.addons.checks.AbstractAddonsCheck;
import com.thomasjensen.checkstyle.addons.checks.BinaryName;
import com.thomasjensen.checkstyle.addons.util.Util;


/**
 * This check helps keeping a property file in sync with a piece of code that contains the property keys.
 * <p><a href="http://checkstyle-addons.thomasjensen.com/latest/checks/misc.html#PropertyCatalog"
 * target="_blank">Documentation</a></p>
 */
@SuppressWarnings("MethodDoesntCallSuperMethod")
public class PropertyCatalogCheck
    extends AbstractAddonsCheck
{
    /** AST tokens that we want to visit */
    private static final Set<Integer> TOKENS = Collections.unmodifiableSet(
        new TreeSet<>(Arrays.asList(TokenTypes.ENUM_CONSTANT_DEF, TokenTypes.VARIABLE_DEF)));

    /** speed up processing by skipping types which are not property catalogs */
    private final Deque<Boolean> skipType = new LinkedList<>();

    /**
     * Stack of sets of catalog entries found in the current source file. Each item on the stack is a list for the
     * currently active class; this is used for properly scoping nested inner classes.
     */
    private final Deque<Set<CatalogEntry>> catalogEntries = new LinkedList<>();

    /** Maximum number of directory levels that may exist between the base directory and an individual module root */
    static final int NUM_SUBDIRS = 3;

    /*
     * --------------- Check properties: ---------------------------------------------------------------------------
     */

    /** the base directory to be assumed for this check, usually the project's root directory */
    private File baseDir = Util.canonize(new File("."));

    /** Files that match this pattern are ignored by this check */
    private Pattern fileExludes = Pattern.compile(
        "[\\\\/]\\.idea[\\\\/](?:checkstyleidea\\.tmp[\\\\/])?csi-\\w+[\\\\/]");

    /** Regexp that matches the Java sources which are property catalogs */
    private Pattern selection = Util.NEVER_MATCH;

    /** Regex that matches excluded fields which should not be considered part of the property catalog */
    private Pattern excludedFields = Pattern.compile("serialVersionUID");

    /**
     * <code>true</code> if the first constructor parameter of an enum constant shall be used as key to the property
     * catalog instead of the enum constant itself (only used if the property catalog is an Enum)
     */
    private boolean enumArgument = false;

    /** template for the property file path */
    private String propertyFileTemplate = "";

    /** Character encoding of the property file */
    private Charset propertyFileEncoding = Util.UTF8;

    /** Report if two code references point to the same property? */
    private boolean reportDuplicates = true;

    /** Report if property entries are not referenced in the code? */
    private boolean reportOrphans = true;

    /** <code>true</code> if property keys should be case sensitive, <code>false</code> otherwise */
    private boolean caseSensitiveKeys = true;



    /**
     * Constructor.
     */
    public PropertyCatalogCheck()
    {
        this(null);
    }



    PropertyCatalogCheck(final String pMockFile)
    {
        super(pMockFile);
    }



    @Override
    public Set<Integer> getRelevantTokens()
    {
        return TOKENS;
    }



    @Override
    public void beginTree(final DetailAST pRootAst)
    {
        super.beginTree(pRootAst);
        catalogEntries.clear();
        skipType.clear();
    }



    @Override
    protected void visitKnownType(@Nonnull final BinaryName pBinaryClassName, @Nonnull final DetailAST pAst)
    {
        catalogEntries.push(new TreeSet<CatalogEntry>());
        boolean isExcludedFile = fileExludes.matcher(
            Util.canonize(getApiFixer().getCurrentFileName()).getAbsolutePath()).find();
        boolean isPropertyCatalog = isPropertyCatalog(pBinaryClassName);
        skipType.push(Boolean.valueOf(!isPropertyCatalog || isExcludedFile));
    }



    @Override
    protected void leaveKnownType(@Nonnull final BinaryName pBinaryClassName, @Nonnull final DetailAST pAst)
    {
        final Set<CatalogEntry> collectedEntries = catalogEntries.pop();
        if (skipType.pop().booleanValue()) {
            return;
        }

        Map<String, String> props = null;
        final File propFile = findPropertyFile(pBinaryClassName);
        if (propFile != null) {
            props = loadPropertyFile(propFile);
        }

        if (props == null) {
            final DetailAST classIdent = pAst.findFirstToken(TokenTypes.IDENT);
            String absPath = propFile != null ? propFile.getAbsolutePath() : null;
            String dynamicDirsAll = null;

            if (propertyFileTemplate.contains("{11}")) {
                absPath = normalize(buildPropertyFilePath(pBinaryClassName, 0, false)).getAbsolutePath();
                StringBuilder sb = new StringBuilder();
                for (final String s : getFirstSubdirs(NUM_SUBDIRS)) {
                    sb.append(s);
                    sb.append('/');
                }
                dynamicDirsAll = sb.toString();
            }

            if (dynamicDirsAll != null && !"null/null/null/".equals(dynamicDirsAll)) {
                log(classIdent, "propertycatalog.file.notfound.dynamic", pBinaryClassName, absPath, dynamicDirsAll);
            }
            else {
                log(classIdent, "propertycatalog.file.notfound", pBinaryClassName, absPath);
            }
            return;
        }

        checkCatalog(pAst, collectedEntries, props, propFile);
    }



    @CheckForNull
    private File findPropertyFile(@Nonnull final BinaryName pBinaryClassName)
    {
        File result = null;
        for (int i = 0; i <= NUM_SUBDIRS; i++) {
            result = normalize(buildPropertyFilePath(pBinaryClassName, i, true));
            if (result.canRead()) {
                break;
            }
        }
        return result;
    }



    private File normalize(@Nonnull final String pFilePath)
    {
        File result = new File(pFilePath);
        if (!result.isAbsolute()) {
            result = Util.canonize(new File(baseDir, pFilePath));
        }
        return result;
    }



    private void checkCatalog(@Nonnull final DetailAST pTypeAst, @Nonnull final Set<CatalogEntry> pEntries,
        @Nonnull final Map<String, String> pProps, @Nonnull final File pPropFile)
    {
        final Map<String, CatalogEntry> foundKeys = new TreeMap<String, CatalogEntry>(
            caseSensitiveKeys ? null : String.CASE_INSENSITIVE_ORDER);

        for (CatalogEntry entry : pEntries) {
            if (pProps.get(entry.getKey()) == null) {
                if (entry.getConstantName().equals(entry.getKey())) {
                    log(entry.getAst(), "propertycatalog.missing.property.short", entry.getKey(),
                        pPropFile.getAbsolutePath());
                }
                else {
                    log(entry.getAst(), "propertycatalog.missing.property.long", entry.getConstantName(),
                        entry.getKey(), pPropFile.getAbsolutePath());
                }
            }

            if (reportDuplicates || reportOrphans) {
                final CatalogEntry duplicate = foundKeys.get(entry.getKey());
                if (duplicate != null) {
                    if (reportDuplicates) {
                        CatalogEntry first = entry;
                        CatalogEntry second = duplicate;
                        if (entry.getAst().getLineNo() > duplicate.getAst().getLineNo()) {
                            first = duplicate;
                            second = entry;
                        }
                        log(second.getAst(), "propertycatalog.duplicate.property", second.getConstantName(),
                            first.getConstantName(), first.getAst().getLineNo());
                    }
                }
                else {
                    foundKeys.put(entry.getKey(), entry);
                }
            }
        }

        if (reportOrphans) {
            final Set<String> orphans = new TreeSet<String>();    // The orphan list is always case sensitive.
            for (String prop : pProps.keySet()) {
                if (!foundKeys.containsKey(prop)) {
                    orphans.add(prop);
                }
            }
            DetailAST classIdent = pTypeAst.findFirstToken(TokenTypes.IDENT);
            if (orphans.size() == 1) {
                log(classIdent, "propertycatalog.orphaned.property", orphans.iterator().next(), pPropFile);
            }
            else if (orphans.size() > 1) {
                log(classIdent, "propertycatalog.orphaned.properties", orphans, pPropFile);
            }
        }
    }



    @Nonnull
    @SuppressFBWarnings("CLI_CONSTANT_LIST_INDEX")
    String buildPropertyFilePath(@Nonnull final BinaryName pBinaryClassName, final int pSubDirLevel,
        final boolean pReplace11)
    {
        final String bcn = pBinaryClassName.toString();

        final String completePath = bcn.replace('.', '/').replace('$', '/');
        final String outerFqcn = pBinaryClassName.getOuterFqcn();
        final String outerFqcnPath = outerFqcn.replace('.', '/');
        final String outerFqcnBackrefs = outerFqcnPath.replaceAll("[^/]+", "..");
        final String outerSimpleName = pBinaryClassName.getOuterSimpleName();
        final String innerSimpleName = pBinaryClassName.getInnerSimpleName();

        final String pg = pBinaryClassName.getPackage();
        final String pkgPath = pg != null ? pg.replace('.', '/') : "";
        final String pathToClass = getPathToClass(pkgPath);

        final StringBuilder ph11 = new StringBuilder();
        final String[] subdirs = getFirstSubdirs(NUM_SUBDIRS);
        if (pReplace11) {
            for (int i = 0; i < pSubDirLevel; i++) {
                ph11.append(subdirs[i]);
                ph11.append('/');   // always slash, not backslash
            }
        }

        return MessageFormat.format(propertyFileTemplate, pBinaryClassName, completePath, outerFqcn, outerFqcnPath,
            outerFqcnBackrefs, pkgPath, outerSimpleName, innerSimpleName, subdirs[0], subdirs[1], subdirs[2],
            pReplace11 ? ph11.toString() : "{11}", pathToClass);
    }



    /**
     * Assuming that the currently analyzed file is located below the current working directory, this method returns a
     * new array of exactly <code>pNumSubdirs</code> elements containing the simple names of the directories on the
     * path to the currently analyzed file, starting just below the current working directory.
     *
     * @param pNumSubdirs the number of subdirectory names to return. If fewer exist, they are padded with
     * <code>null</code>
     * @return the first n subdirs, where non-existing elements are <code>null</code>
     */
    @Nonnull
    private String[] getFirstSubdirs(final int pNumSubdirs)
    {
        String[] result = new String[pNumSubdirs];
        Arrays.fill(result, null);

        final File thisFile = Util.canonize(getApiFixer().getCurrentFileName());
        if (thisFile.getPath().startsWith(baseDir.getPath())) {

            final String relPath = thisFile.getPath().substring(baseDir.getPath().length() + 1); // incl. separator char
            final String[] pathElements = relPath.split(Pattern.quote(File.separator), pNumSubdirs + 1);
            int i = 0;
            for (String elem : pathElements) {
                if (i < pNumSubdirs) {
                    result[i++] = elem;
                }
            }
        }
        return result;
    }



    @Nonnull
    private String getPathToClass(@Nonnull final String pPkgPath)
    {
        String result = "";
        final File thisFile = Util.canonize(getApiFixer().getCurrentFileName().getParentFile());
        if (thisFile.getPath().startsWith(baseDir.getPath())
            && thisFile.getPath().length() >= baseDir.getPath().length() + 1) {
            final String relPath = thisFile.getPath().substring(baseDir.getPath().length() + 1);
            if (pPkgPath.isEmpty()) {
                result = relPath;
            }
            else {
                Matcher m = Pattern.compile(pPkgPath.replace("/", Matcher.quoteReplacement("[\\/]")) + "$").matcher(
                    relPath);
                if (m.find()) {
                    result = relPath.substring(0, m.start() - 1);
                }
            }
        }
        return result;
    }



    @CheckForNull
    private Map<String, String> loadPropertyFile(@Nonnull final File pPropertyFile)
    {
        Properties props = new Properties();
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        InputStreamReader isr = null;
        try {
            fis = new FileInputStream(pPropertyFile);
            bis = new BufferedInputStream(fis);
            isr = new InputStreamReader(bis, propertyFileEncoding);
            props.load(isr);
        }
        catch (IOException e) {
            props = null;
        }
        finally {
            Util.closeQuietly(isr);
            Util.closeQuietly(bis);
            Util.closeQuietly(fis);
        }

        Map<String, String> result = null;
        if (props != null) {
            result = caseSensitiveKeys ? new HashMap<String, String>() : new TreeMap<String, String>(
                String.CASE_INSENSITIVE_ORDER);
            for (Map.Entry<Object, Object> entry : props.entrySet()) {
                result.put((String) entry.getKey(), (String) entry.getValue());
            }
        }
        return result;
    }



    @Override
    protected void visitToken(@Nullable final BinaryName pBinaryClassName, @Nonnull final DetailAST pAst)
    {
        if (skipType.peek().booleanValue()) {
            return;
        }

        if (pAst.getType() == TokenTypes.ENUM_CONSTANT_DEF && !isFieldExcluded(pAst)) {
            if (enumArgument) {
                final String key = readConstantValue(TokenTypes.ELIST, pAst);
                if (key != null) {
                    final String constantName = pAst.findFirstToken(TokenTypes.IDENT).getText();
                    final DetailAST toHighlight = pAst.findFirstToken(TokenTypes.ELIST).findFirstToken(TokenTypes.EXPR)
                        .getFirstChild();
                    catalogEntries.peek().add(new CatalogEntry(constantName, key, toHighlight));
                }
                else {
                    log(pAst, "propertycatalog.unclear.enumparam");
                }
            }
            else {
                final DetailAST toHighlight = pAst.findFirstToken(TokenTypes.IDENT);
                final String constantName = toHighlight.getText();
                catalogEntries.peek().add(new CatalogEntry(constantName, constantName, toHighlight));
            }
        }

        else if (isVariableDefOfField(pAst) && isClassOrInterface(pAst)) {
            if (isNonPrivateConstant(pAst) && !isFieldExcluded(pAst)) {
                final String key = readConstantValue(TokenTypes.ASSIGN, pAst);
                if (key != null) {
                    final String constantName = pAst.findFirstToken(TokenTypes.IDENT).getText();
                    final DetailAST toHighlight = pAst.findFirstToken(TokenTypes.ASSIGN).findFirstToken(TokenTypes.EXPR)
                        .getFirstChild();
                    catalogEntries.peek().add(new CatalogEntry(constantName, key, toHighlight));
                }
                else {
                    log(pAst.findFirstToken(TokenTypes.IDENT), "propertycatalog.unclear.constant");
                }
            }
        }
    }



    @CheckForNull
    private String readConstantValue(final int pDrillToken, @Nonnull final DetailAST pAst)
    {
        String result = null;
        DetailAST a = pAst.findFirstToken(pDrillToken);
        if (a != null) {
            a = a.findFirstToken(TokenTypes.EXPR);
            if (a != null) {   // EXPR may not be found in empty ELIST, e.g. EnumConstant()
                a = a.getFirstChild();
            }
        }
        if (a != null) {
            final int t = a.getType();
            if (t == TokenTypes.STRING_LITERAL) {
                result = a.getText().substring(1, a.getText().length() - 1);   // remove quotes
            }
            else if (t == TokenTypes.NUM_INT || t == TokenTypes.NUM_LONG || t == TokenTypes.LITERAL_TRUE
                || t == TokenTypes.LITERAL_FALSE) {
                result = a.getText();
            }
        }
        return result;
    }



    private boolean isVariableDefOfField(@Nonnull final DetailAST pAst)
    {
        return pAst.getType() == TokenTypes.VARIABLE_DEF && pAst.getParent().getType() == TokenTypes.OBJBLOCK;
    }



    private boolean isClassOrInterface(@Nonnull final DetailAST pAst)
    {
        final int type = pAst.getParent().getParent().getType();
        return type == TokenTypes.CLASS_DEF || type == TokenTypes.INTERFACE_DEF;
    }



    private boolean isFieldExcluded(@Nonnull final DetailAST pAst)
    {
        final String varName = Util.getFirstIdent(pAst);
        return excludedFields.matcher(varName).matches();
    }



    private boolean isNonPrivateConstant(@Nonnull final DetailAST pAst)
    {
        boolean result = false;
        if (pAst.getParent().getParent().getType() == TokenTypes.INTERFACE_DEF) {
            // interface
            result = true;
        }
        else {
            // class
            boolean foundPrivate = false;
            boolean foundStatic = false;
            boolean foundFinal = false;
            DetailAST mods = pAst.findFirstToken(TokenTypes.MODIFIERS);
            if (mods != null) {
                for (DetailAST a = mods.getFirstChild(); a != null; a = a.getNextSibling()) {
                    if (a.getType() == TokenTypes.LITERAL_PRIVATE) {
                        foundPrivate = true;
                    }
                    else if (a.getType() == TokenTypes.LITERAL_STATIC) {
                        foundStatic = true;
                    }
                    else if (a.getType() == TokenTypes.FINAL) {
                        foundFinal = true;
                    }
                }
            }
            result = !foundPrivate && foundStatic && foundFinal;
        }
        return result;
    }



    private boolean isPropertyCatalog(@Nullable final BinaryName pBinaryClassName)
    {
        boolean result = false;
        if (pBinaryClassName != null) {
            result = selection.matcher(pBinaryClassName.toString()).find();
        }
        return result;
    }



    public void setBaseDir(final String pBaseDir)
    {
        baseDir = Util.canonize(new File(pBaseDir));
    }



    public void setFileExludes(final String pFileExludes)
    {
        fileExludes = Pattern.compile(pFileExludes);
    }



    public void setSelection(final String pSelection)
    {
        selection = Pattern.compile(pSelection);
    }



    public void setExcludedFields(final String pExcludedFields)
    {
        excludedFields = Pattern.compile(pExcludedFields);
    }



    public void setEnumArgument(final boolean pEnumArgument)
    {
        enumArgument = pEnumArgument;
    }



    public void setPropertyFile(final String pTemplate)
    {
        propertyFileTemplate = pTemplate;
    }



    public void setPropertyFileEncoding(final String pEncoding)
    {
        propertyFileEncoding = Charset.forName(pEncoding);
    }



    public void setReportDuplicates(final boolean pReportDuplicates)
    {
        reportDuplicates = pReportDuplicates;
    }



    public void setReportOrphans(final boolean pReportOrphans)
    {
        reportOrphans = pReportOrphans;
    }



    public void setCaseSensitive(final boolean pCaseSensitive)
    {
        caseSensitiveKeys = pCaseSensitive;
    }
}
