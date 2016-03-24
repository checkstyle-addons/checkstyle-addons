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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.puppycrawl.tools.checkstyle.api.AbstractFileSetCheck;
import com.thomasjensen.checkstyle.addons.util.Util;


/**
 * This check helps ensure that the source folder structure in a module follows a configurable convention. <p><a
 * href="http://checkstyle-addons.thomasjensen.com/latest/checks/misc.html#ModuleDirectoryLayout"
 * target="_blank">Documentation</a></p>
 *
 * @author Thomas Jensen
 */
public class ModuleDirectoryLayoutCheck
    extends AbstractFileSetCheck
{
    private static final String DEFAULT_CONFIG_FILENAME = "ModuleDirectoryLayout-default.json";

    /** the base directory to be assumed for this check, usually the project's root directory */
    private File baseDir = Util.canonize(new File("."));

    /** the parsed contents of the UTF-8 encoded configuration file in JSON */
    private MdlJsonConfig mdlConfig;

    /** matches the file extension in a pathname in its capturing group */
    private static final Pattern FILE_EXTENSION = Pattern.compile("\\.([^\\\\/]+)$");

    private static final Pattern SINGLE_MODULE_PROJECT = Pattern.compile("");

    private Pattern moduleRegexp = SINGLE_MODULE_PROJECT;



    /**
     * Constructor. Activates the default config file, which may be overridden by the <code>configFile</code> check
     * property.
     */
    public ModuleDirectoryLayoutCheck()
    {
        super();
        InputStream is = null;
        try {
            is = getClass().getResourceAsStream(DEFAULT_CONFIG_FILENAME);
            activateConfigFile(is, DEFAULT_CONFIG_FILENAME);
        }
        finally {
            Util.closeQuietly(is);
        }
    }



    @Override
    protected void processFiltered(final File pFile, final List<String> pLines)
    {
        if (mdlConfig != null) {
            final String filePath = Util.canonize(pFile).getPath();
            final DecomposedPath decomposedPath = decomposePath(filePath);
            if (decomposedPath != null) {
                MdlJsonConfig.MdlSpec mdlSpec = mdlConfig.getStructure().get(decomposedPath.getMdlPath());

                if (!mdlConfig.getSettings().isAllowNestedSrcFolder() && decomposedPath.getSpecificFolders().contains(
                    "src")) {
                    log(0, "moduledirectorylayout.nestedsrcfolder", decomposedPath.getSpecificPath());
                }

                else if (!isMdlAllowedForModule(mdlSpec, decomposedPath)) {
                    log(0, "moduledirectorylayout.notinthismodule", decomposedPath.getMdlPath(),
                        decomposedPath.getModulePath());
                }

                else if (!isSpecificPathAllowedInMdl(mdlSpec, decomposedPath) || !isAllowListPostProcessingOk(
                    mdlSpec.getAllow(), decomposedPath)) {
                    log(0, "moduledirectorylayout.illegalcontent", decomposedPath.getMdlPath(),
                        decomposedPath.getSpecificPath());
                }
            }
        }
    }



    private boolean isAllowListPostProcessingOk(@Nullable final List<MdlJsonConfig.SpecElement> pAllowList,
        @Nonnull final DecomposedPath pDecomposedPath)
    {
        boolean result = true;
        if (pAllowList != null && pAllowList.size() > 0) {
            for (final MdlJsonConfig.SpecElement se : pAllowList) {
                if (se.getType() == MdlContentSpecType.TopLevelFolder) {
                    result = checkTopLevelFolder(se.getSpec(), pDecomposedPath.getSpecificFolders(), true);
                    if (result) {
                        break;
                    }
                }
            }
        }
        return result;
    }



    private boolean checkTopLevelFolder(@Nonnull final String pTopLevelFolder,
        @Nonnull final List<String> pSpecificFolders, final boolean pCaseSensitive)
    {
        boolean result = false;
        if (pSpecificFolders.size() > 0) {
            result = Util.stringEquals(pSpecificFolders.get(0), pTopLevelFolder, pCaseSensitive);
            if (pSpecificFolders.size() > 1) {
                result = result && !Util.containsString(pSpecificFolders.subList(1, pSpecificFolders.size()),
                    pTopLevelFolder, false); // never case sensitive
            }
        }
        return result;
    }



    private boolean isSpecificPathAllowedInMdl(@Nonnull final MdlJsonConfig.MdlSpec pMdlSpec,
        @Nonnull final DecomposedPath pDecomposedPath)
    {
        boolean allowed = true;
        boolean denied = false;
        if (pMdlSpec.isWhitelist() && pMdlSpec.getAllow() != null && !pMdlSpec.getAllow().isEmpty()) {
            // allow lists are case sensitive
            allowed = processSpecList(pMdlSpec.getAllow(), pDecomposedPath, true);
        }
        if (pMdlSpec.getDeny() != null && !pMdlSpec.getDeny().isEmpty()) {
            // deny lists are case insensitive
            denied = processSpecList(pMdlSpec.getDeny(), pDecomposedPath, false);
        }
        return allowed && !denied;
    }



    private boolean isMdlAllowedForModule(@Nonnull final MdlJsonConfig.MdlSpec pMdlSpec,
        @Nonnull final DecomposedPath pDecomposedPath)
    {
        return pDecomposedPath.getModulePath().isEmpty() || pMdlSpec.getModules() == null || pMdlSpec.getModules()
            .matcher(pDecomposedPath.getModulePath()).find();
    }



    private boolean processSpecList(@Nullable final List<MdlJsonConfig.SpecElement> pSpecList,
        @Nonnull final DecomposedPath pDecomposedPath, final boolean pCaseSensitive)
    {
        boolean result = false;
        if (pSpecList != null) {
            for (final MdlJsonConfig.SpecElement se : pSpecList) {
                switch (se.getType()) {

                    case FileExtensions:
                        for (final String allowedExtension : se.getSpec().split("\\s*,\\s*")) {
                            if (Util.containsString(pDecomposedPath.getFileExtensions(), allowedExtension,
                                pCaseSensitive)) {
                                result = true;
                                break;
                            }
                        }
                        break;

                    case SimpleFolder:
                        result = Util.containsString(pDecomposedPath.getSpecificFolders(), se.getSpec(),
                            pCaseSensitive);
                        break;

                    case TopLevelFolder:
                        result = checkTopLevelFolder(se.getSpec(), pDecomposedPath.getSpecificFolders(),
                            pCaseSensitive);
                        break;

                    case SimpleName:
                        result = Util.stringEquals(pDecomposedPath.getSimpleFilename(), se.getSpec(), pCaseSensitive);
                        break;

                    case SpecificPathRegex:
                        result = Pattern.compile(se.getSpec()).matcher(pDecomposedPath.getSpecificPath()).find();
                        break;

                    case FromPath:
                        // only possible in deny lists
                        result = processSpecList(mdlConfig.getStructure().get(se.getSpec()).getAllow(), pDecomposedPath,
                            pCaseSensitive);
                        break;

                    default:
                        throw new IllegalStateException("Unexpected enum constant: " + se.getType());
                }
                if (result) {
                    break;
                }
            }
        }
        return result;
    }



    @CheckForNull
    DecomposedPath decomposePath(@Nonnull final String pFilePath)
    {
        String modulePath = "";
        String mdlPath = null;
        String specificPath = null;
        String simpleFilename = null;
        Set<String> fileExtensions = new HashSet<String>();
        List<String> specificFolders = new ArrayList<String>();

        if (!pFilePath.startsWith(baseDir.getPath())) {
            return null;
        }
        String filePath = cutSlashes(pFilePath.substring(baseDir.getPath().length()));

        if (moduleRegexp.pattern().length() > 0) {
            final Matcher matcher = moduleRegexp.matcher(filePath);
            if (matcher.find() && matcher.start() == 0) {
                modulePath = cutSlashes(matcher.group(0));
            }
            else {
                log(0, "moduledirectorylayout.invalid.module", filePath, moduleRegexp.pattern());
                return null;
            }
        }
        if (modulePath.length() > 0) {
            filePath = cutSlashes(filePath.substring(modulePath.length()));
        }

        for (final String mdlPathCandidate : mdlConfig.getStructure().keySet()) {
            if (filePath.startsWith(Util.standardizeSlashes(mdlPathCandidate))) {
                mdlPath = mdlPathCandidate;
                filePath = cutSlashes(filePath.substring(mdlPathCandidate.length()));
            }
        }
        if (mdlPath == null) {
            if (filePath.indexOf(File.separatorChar) > 0) {   // no error if file is in module root
                log(0, "moduledirectorylayout.invalid.mdlpath", filePath);
            }
            return null;
        }

        specificPath = filePath;

        Matcher matcher = FILE_EXTENSION.matcher(filePath);
        if (matcher.find()) {
            String ext = matcher.group(1);
            for (int d = ext.lastIndexOf('.'); d > 0; d = ext.lastIndexOf('.', d - 1)) {
                fileExtensions.add(ext.substring(d + 1));
            }
            fileExtensions.add(ext);
        }

        int lastSlash = filePath.lastIndexOf(File.separatorChar);
        simpleFilename = filePath.substring(lastSlash + 1);

        String[] fragments = filePath.split("[\\\\/]");
        specificFolders.addAll(Arrays.asList(fragments).subList(0, fragments.length - 1));

        DecomposedPath result = new DecomposedPath(modulePath, mdlPath, specificPath, simpleFilename, fileExtensions,
            specificFolders);
        return result;
    }



    @Nonnull
    String cutSlashes(@Nonnull final String pPath)
    {
        String result = pPath;
        if (pPath.length() > 0) {
            boolean leadingSlash = false;
            if (pPath.charAt(0) == '\\' || pPath.charAt(0) == '/') {
                leadingSlash = true;
            }
            boolean trailingSlash = false;
            if (pPath.length() > 1 && (pPath.charAt(pPath.length() - 1) == '\\' || pPath.charAt(pPath.length() - 1)
                == '/')) {
                trailingSlash = true;
            }
            if (leadingSlash || trailingSlash) {
                result = pPath.substring(leadingSlash ? 1 : 0, pPath.length() - (trailingSlash ? 1 : 0));
            }
        }
        return result;
    }



    public void setBaseDir(final String pBaseDir)
    {
        baseDir = Util.canonize(new File(pBaseDir));
    }



    /**
     * Setter.
     *
     * @param pConfigFile the location of the JSON configuration file
     */
    public final void setConfigFile(@Nonnull final String pConfigFile)
    {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(Util.canonize(new File(pConfigFile)));
            activateConfigFile(fis, pConfigFile);
        }
        catch (IllegalArgumentException e) {
            activateConfigFile(null, null);
            throw e;
        }
        catch (FileNotFoundException e) {
            activateConfigFile(null, null);
            throw new IllegalArgumentException(
                "Config file not found for " + getClass().getSimpleName() + ": " + pConfigFile, e);
        }
        finally {
            Util.closeQuietly(fis);
        }
    }



    private void activateConfigFile(@Nullable final InputStream pInputStream, @Nullable final String pFilename)
    {
        if (pInputStream != null) {
            try {
                mdlConfig = readConfigFile(pInputStream);
            }
            catch (IOException e) {
                throw new IllegalArgumentException(
                    "Could not read or parse the module directory layout configFile: " + pFilename, e);
            }

            try {
                mdlConfig.validate();
                moduleRegexp = Pattern.compile(mdlConfig.getSettings().getModuleRegex());
            }
            catch (ConfigValidationException e) {
                mdlConfig = null;
                moduleRegexp = SINGLE_MODULE_PROJECT;
                throw new IllegalArgumentException(
                    "Module directory layout configFile contains invalid configuration: " + pFilename, e);
            }
        }
        else {
            mdlConfig = null;
            moduleRegexp = SINGLE_MODULE_PROJECT;
        }
    }



    static MdlJsonConfig readConfigFile(@Nonnull final InputStream pInputStream)
        throws IOException
    {
        final byte[] fileContents = Util.readBytes(pInputStream);
        final String json = new String(fileContents, Util.UTF8);
        final MdlJsonConfig result = new ObjectMapper().readValue(json, MdlJsonConfig.class);
        return result;
    }
}
