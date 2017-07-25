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
import java.util.Collections;
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
import com.puppycrawl.tools.checkstyle.api.FileText;

import com.thomasjensen.checkstyle.addons.util.CallableNoEx;
import com.thomasjensen.checkstyle.addons.util.Util;


/**
 * This check helps ensure that the source folder structure in a module follows a configurable convention. <p><a
 * href="http://checkstyle-addons.thomasjensen.com/latest/checks/misc.html#ModuleDirectoryLayout"
 * target="_blank">Documentation</a></p>
 */
public class ModuleDirectoryLayoutCheck
    extends AbstractFileSetCheck
{
    private static final String DEFAULT_CONFIG_FILENAME = "ModuleDirectoryLayout-default.json";

    /** matches the file extension in a pathname in its capturing group */
    private static final Pattern FILE_EXTENSION = Pattern.compile("\\.([^\\\\/]+)$");

    private static final Pattern SINGLE_MODULE_PROJECT = Pattern.compile("");

    /** the base directory to be assumed for this check, usually the project's root directory */
    private File baseDir = Util.canonize(new File("."));

    private boolean failQuietly = false;

    private CallableNoEx<MdlConfig> mdlConfigCallable;

    private MdlConfig mdlConfigCache = null;



    /** SpecLists can be allow or deny lists. */
    private enum SpecListType
    {
        Allow(true),

        Deny(false);

        //

        private final boolean caseSensitive;



        private SpecListType(final boolean pCaseSensitive)
        {
            caseSensitive = pCaseSensitive;
        }



        public boolean isCaseSensitive()
        {
            return caseSensitive;
        }
    }



    /**
     * Constructor. Activates the default config file, which may be overridden by the <code>configFile</code> check
     * property.
     */
    public ModuleDirectoryLayoutCheck()
    {
        super();
        mdlConfigCallable = new CallableNoEx<MdlConfig>()
        {
            @Override
            @Nonnull
            public MdlConfig call()
            {
                MdlConfig result = null;
                InputStream is = null;
                try {
                    is = ModuleDirectoryLayoutCheck.class.getResourceAsStream(DEFAULT_CONFIG_FILENAME);
                    result = activateConfigFile(is, DEFAULT_CONFIG_FILENAME);
                }
                finally {
                    Util.closeQuietly(is);
                }
                return result;
            }
        };
    }



    @Override
    public void beginProcessing(final String pCharset)
    {
        super.beginProcessing(pCharset);
        mdlConfigCache = null;
    }



    @Override
    protected void processFiltered(final File pFile, final List<String> pLines)
    {
        final MdlConfig wrapper = getMdlConfig();
        final MdlJsonConfig mdlConfig = wrapper.getJson();
        if (mdlConfig != null) {
            final String filePath = Util.canonize(pFile).getPath();
            final DecomposedPath decomposedPath = decomposePath(wrapper, filePath);
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

                else if (!isSpecificPathAllowedInMdl(mdlConfig, mdlSpec, decomposedPath)
                    || !isAllowListPostProcessingOk(mdlSpec.getAllow(), decomposedPath)) {
                    log(0, "moduledirectorylayout.illegalcontent", decomposedPath.getMdlPath(),
                        decomposedPath.getSpecificPath());
                }
            }
        }
    }



    protected void processFiltered(final File pFile, final FileText pLines)
    {
        processFiltered(pFile, Collections.<String>emptyList());
    }



    private boolean isAllowListPostProcessingOk(@Nullable final List<MdlJsonConfig.SpecElement> pAllowList,
        @Nonnull final DecomposedPath pDecomposedPath)
    {
        boolean ok = true;
        if (pAllowList != null && pAllowList.size() > 0) {
            for (final MdlJsonConfig.SpecElement se : pAllowList) {
                if (se.getType() == MdlContentSpecType.TopLevelFolder) {
                    ok = isTopLevelFolderNestingOk(se.getSpec(), pDecomposedPath.getSpecificFolders());
                    if (!ok) {
                        break;
                    }
                }
            }
        }
        return ok;
    }



    private boolean checkTopLevelFolder(@Nonnull final String pTopLevelFolder,
        @Nonnull final List<String> pSpecificFolders, final boolean pCaseSensitive)
    {
        boolean ok = true;
        if (pSpecificFolders.size() > 0) {
            ok = Util.stringEquals(pSpecificFolders.get(0), pTopLevelFolder, pCaseSensitive)
                && isTopLevelFolderNestingOk(pTopLevelFolder, pSpecificFolders);
        }

        return ok;
    }



    private boolean isTopLevelFolderNestingOk(@Nonnull final String pTopLevelFolder,
        @Nonnull final List<String> pSpecificFolders)
    {
        boolean ok = true;
        if (pSpecificFolders.size() > 1) {
            ok = !Util.containsString(pSpecificFolders.subList(1, pSpecificFolders.size()), pTopLevelFolder,
                false); // never case sensitive
        }
        return ok;
    }



    private boolean isSpecificPathAllowedInMdl(@Nonnull final MdlJsonConfig pJsonConfig,
        @Nonnull final MdlJsonConfig.MdlSpec pMdlSpec, @Nonnull final DecomposedPath pDecomposedPath)
    {
        boolean allowed = true;
        boolean denied = false;
        if (pMdlSpec.isWhitelist() && pMdlSpec.getAllow() != null && !pMdlSpec.getAllow().isEmpty()) {
            allowed = processSpecList(pJsonConfig, pMdlSpec.getAllow(), pDecomposedPath, SpecListType.Allow);
        }
        if (pMdlSpec.getDeny() != null && !pMdlSpec.getDeny().isEmpty()) {
            denied = processSpecList(pJsonConfig, pMdlSpec.getDeny(), pDecomposedPath, SpecListType.Deny);
        }
        return allowed && !denied;
    }



    private boolean isMdlAllowedForModule(@Nonnull final MdlJsonConfig.MdlSpec pMdlSpec,
        @Nonnull final DecomposedPath pDecomposedPath)
    {
        return pDecomposedPath.getModulePath().isEmpty() || pMdlSpec.getModules() == null || pMdlSpec.getModules()
            .matcher(pDecomposedPath.getModulePath()).find();
    }



    private boolean processSpecList(@Nonnull final MdlJsonConfig pJsonConfig,
        @Nullable final List<MdlJsonConfig.SpecElement> pSpecList, @Nonnull final DecomposedPath pDecomposedPath,
        @Nonnull final SpecListType pListType)
    {
        boolean match = false;
        if (pSpecList != null) {
            for (final MdlJsonConfig.SpecElement se : pSpecList) {
                switch (se.getType()) {

                    case FileExtensions:
                        for (final String allowedExtension : se.getSpec().split("\\s*,\\s*")) {
                            if (Util.containsString(pDecomposedPath.getFileExtensions(), allowedExtension,
                                pListType.isCaseSensitive())) {
                                match = true;
                                break;
                            }
                        }
                        break;

                    case TopLevelFolder:
                        if (pListType == SpecListType.Allow) {
                            match = checkTopLevelFolder(se.getSpec(), pDecomposedPath.getSpecificFolders(),
                                pListType.isCaseSensitive());
                            break;
                        }
                        // fall through

                    case SimpleFolder:
                        match = Util.containsString(pDecomposedPath.getSpecificFolders(), se.getSpec(),
                            pListType.isCaseSensitive());
                        break;

                    case SimpleName:
                        match = Util.stringEquals(pDecomposedPath.getSimpleFilename(), se.getSpec(),
                            pListType.isCaseSensitive());
                        break;

                    case SpecificPathRegex:
                        match = Pattern.compile(se.getSpec()).matcher(pDecomposedPath.getSpecificPath()).find();
                        break;

                    case FromPath:
                        // only possible in deny lists
                        match = processSpecList(pJsonConfig, pJsonConfig.getStructure().get(se.getSpec()).getAllow(),
                            pDecomposedPath, pListType);
                        break;

                    default:
                        throw new IllegalStateException("Unexpected enum constant: " + se.getType());
                }
                if (match) {
                    break;
                }
            }
        }
        return match;
    }



    @CheckForNull
    DecomposedPath decomposePath(@Nonnull final MdlConfig pMdlConfig, @Nonnull final String pFilePath)
    {
        final MdlJsonConfig mdlConfig = pMdlConfig.getJson();
        final Pattern moduleRegexp = pMdlConfig.getModuleRegex();
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

        if (pMdlConfig.getExcludeRegex().matcher(filePath).find()) {
            return null;   // the file path is excluded from checking
        }

        if (moduleRegexp.pattern().length() > 0) {
            final Matcher matcher = moduleRegexp.matcher(filePath);
            if (matcher.find() && matcher.start() == 0) {
                modulePath = cutSlashes(matcher.group(0));
            }
            else if (filePath.contains("\\") || filePath.contains("/")) {     // no error if file is in baseDir
                log(0, "moduledirectorylayout.invalid.module", filePath, moduleRegexp.pattern());
                return null;
            }
        }
        if (modulePath.length() > 0) {
            filePath = cutSlashes(filePath.substring(modulePath.length()));
        }

        for (final String mdlPathCandidate : mdlConfig.getStructure().keySet()) {
            if (mdlPathCandidate.length() < filePath.length() //
                && filePath.startsWith(Util.standardizeSlashes(mdlPathCandidate)) //
                && "/\\".indexOf(filePath.charAt(mdlPathCandidate.length())) >= 0) //
            {
                mdlPath = mdlPathCandidate;
                filePath = cutSlashes(filePath.substring(mdlPathCandidate.length()));
                break;
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
        mdlConfigCallable = new CallableNoEx<MdlConfig>()
        {
            @Override
            @Nonnull
            public MdlConfig call()
            {
                MdlConfig result = null;
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(Util.canonize(new File(pConfigFile)));
                    result = activateConfigFile(fis, pConfigFile);
                }
                catch (IllegalArgumentException e) {
                    result = activateConfigFile(null, null);
                    throw e;
                }
                catch (FileNotFoundException e) {
                    result = activateConfigFile(null, null);
                    if (!failQuietly) {
                        throw new IllegalArgumentException(
                            "Config file not found for " + ModuleDirectoryLayoutCheck.class.getSimpleName() + ": "
                                + pConfigFile, e);
                    }
                }
                finally {
                    Util.closeQuietly(fis);
                }
                return result;
            }
        };
    }



    public void setFailQuietly(final boolean pFailQuietly)
    {
        failQuietly = pFailQuietly;
    }



    @Nonnull
    private MdlConfig activateConfigFile(@Nullable final InputStream pInputStream, @Nullable final String pFilename)
    {
        MdlConfig result = new MdlConfig(null, SINGLE_MODULE_PROJECT, Util.NEVER_MATCH);
        if (pInputStream != null) {
            MdlJsonConfig json = null;
            Pattern moduleRegexp = null;
            Pattern excludeRegexp = null;
            try {
                json = readConfigFile(pInputStream);
            }
            catch (IOException e) {
                throw new IllegalArgumentException(
                    "Could not read or parse the module directory layout configFile: " + pFilename, e);
            }

            try {
                json.validate();
                moduleRegexp = Pattern.compile(json.getSettings().getModuleRegex());
                excludeRegexp = Pattern.compile(json.getSettings().getExcludeRegex());
            }
            catch (ConfigValidationException e) {
                json = null;
                moduleRegexp = SINGLE_MODULE_PROJECT;
                excludeRegexp = Util.NEVER_MATCH;
                throw new IllegalArgumentException(
                    "Module directory layout configFile contains invalid configuration: " + pFilename, e);
            }
            result = new MdlConfig(json, moduleRegexp, excludeRegexp);
        }
        return result;
    }



    static MdlJsonConfig readConfigFile(@Nonnull final InputStream pInputStream)
        throws IOException
    {
        final byte[] fileContents = Util.readBytes(pInputStream);
        final String json = new String(fileContents, Util.UTF8);
        final MdlJsonConfig result = new ObjectMapper().readValue(json, MdlJsonConfig.class);
        return result;
    }



    @Nonnull
    MdlConfig getMdlConfig()
    {
        if (mdlConfigCache == null) {
            mdlConfigCache = mdlConfigCallable.call();
        }
        return mdlConfigCache;
    }
}
