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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thomasjensen.checkstyle.addons.util.Util;


/**
 * Deserialized version of the <i>directories.json</i>.
 */
public class MdlJsonConfig
    implements SelfValidating
{
    @JsonProperty
    private String comment;

    @JsonProperty
    private Settings settings;

    @JsonProperty
    private Map<String, MdlSpec> structure;



    /**
     * Encapsulates the general settings which are supported in a <i>directories.json</i>.
     */
    public static class Settings
        implements SelfValidating
    {
        @JsonProperty
        private int formatVersion = 1;

        @JsonProperty
        private String moduleRegex = "";

        @JsonProperty
        private String excludeRegex = Util.NEVER_MATCH.pattern();

        @JsonProperty
        private boolean allowNestedSrcFolder = false;



        public int getFormatVersion()
        {
            return formatVersion;
        }



        public String getModuleRegex()
        {
            return moduleRegex;
        }



        public String getExcludeRegex()
        {
            return excludeRegex;
        }



        public boolean isAllowNestedSrcFolder()
        {
            return allowNestedSrcFolder;
        }



        @Override
        public void validate()
            throws ConfigValidationException
        {
            if (getFormatVersion() != 1) {
                throw new ConfigValidationException("The settings field 'formatVersion' must be present unchanged");
            }
            ensureRegexSyntax("moduleRegex", moduleRegex);
            ensureRegexSyntax("excludeRegex", excludeRegex);
        }
    }



    /**
     * An MDL Spec specifies the characteristics of one directory in the module directory layout, for example
     * <tt>src/main/java</tt>.
     */
    public static class MdlSpec
        implements SelfValidating
    {
        @JsonProperty
        private String comment;

        @JsonProperty
        private String modules;

        @JsonProperty
        private boolean whitelist = false;

        @JsonProperty
        private List<SpecElement> allow;

        @JsonProperty
        private List<SpecElement> deny;



        @CheckForNull
        public String getComment()
        {
            return comment;
        }



        @JsonIgnore
        @CheckForNull
        public Pattern getModules()
        {
            return modules != null ? Pattern.compile(modules) : null;
        }



        public boolean isWhitelist()
        {
            return whitelist;
        }



        @CheckForNull
        public List<SpecElement> getAllow()
        {
            return allow;
        }



        @CheckForNull
        public List<SpecElement> getDeny()
        {
            return deny;
        }



        @Override
        @SuppressWarnings("unchecked")
        public void validate()
            throws ConfigValidationException
        {
            if (modules != null) {
                ensureRegexSyntax("modules", modules);
            }
            for (List<SpecElement> list : Arrays.asList(allow, deny)) {
                if (list != null) {
                    for (SpecElement element : list) {
                        element.validate();
                    }
                }
            }
        }
    }



    /**
     * The allow and deny lists in each MDL path specification are composed of elements of <code>SpecElement</code>s.
     */
    public static class SpecElement
        implements SelfValidating
    {
        @JsonProperty
        private MdlContentSpecType type = null;

        @JsonProperty
        private String spec = null;



        /**
         * No-args constructor.
         */
        public SpecElement()
        {
            super();
        }



        /**
         * Constructor for test cases.
         *
         * @param pType value of type
         * @param pSpec value of spec
         */
        SpecElement(@Nullable final MdlContentSpecType pType, @Nullable final String pSpec)
        {
            type = pType;
            spec = pSpec;
        }



        public MdlContentSpecType getType()
        {
            return type;
        }



        public String getSpec()
        {
            return spec;
        }



        @Override
        public void validate()
            throws ConfigValidationException
        {
            if (spec == null) {
                throw new ConfigValidationException("Required field 'spec' not set in SpecElement");
            }
            if (type == null) {
                throw new ConfigValidationException("Required field 'type' not set in SpecElement");
            }
            if (type == MdlContentSpecType.SpecificPathRegex) {
                ensureRegexSyntax("spec", spec);
            }
        }



        @Override
        public String toString()
        {
            final StringBuilder sb = new StringBuilder("{");
            sb.append(type);
            sb.append(", ");
            if (spec != null) {
                sb.append('\"').append(spec).append('\"');
            }
            else {
                sb.append("null");
            }
            sb.append('}');
            return sb.toString();
        }
    }



    @CheckForNull
    public String getComment()
    {
        return comment;
    }



    @Nonnull
    public Settings getSettings()
    {
        return settings;
    }



    @Nonnull
    public Map<String, MdlSpec> getStructure()
    {
        return structure;
    }



    @Override
    public void validate()
        throws ConfigValidationException
    {
        if (settings == null) {
            throw new ConfigValidationException("Missing top-level field 'settings'");
        }
        settings.validate();

        if (structure == null) {
            throw new ConfigValidationException("Missing top-level field 'structure'");
        }
        for (String key1 : structure.keySet()) {
            if (key1.length() == 0) {
                throw new ConfigValidationException("Empty String used as MDL path");
            }
            if (key1.charAt(0) == '/' || key1.charAt(0) == '\\' || key1.charAt(key1.length() - 1) == '/' || key1.charAt(
                key1.length() - 1) == '\\') {
                throw new ConfigValidationException("Leading or trailing (back)slashes on MDL path: " + key1);
            }
            for (String key2 : structure.keySet()) {
                if (!key1.equals(key2)) {
                    if (key1.startsWith(key2) || key2.startsWith(key1)) {
                        throw new ConfigValidationException("MDL paths '" + key1 + "' and '" + key2 + "' are nested");
                    }
                }
            }
        }

        for (final Map.Entry<String, MdlSpec> entry : structure.entrySet()) {
            final MdlSpec mdlSpec = entry.getValue();
            mdlSpec.validate();
            if (mdlSpec.getDeny() != null) {
                for (final SpecElement se : mdlSpec.getDeny()) {
                    if (se.getType() == MdlContentSpecType.FromPath) {
                        if (!structure.containsKey(se.getSpec())) {
                            throw new ConfigValidationException("Reference to unknown MDL path '" + se.getSpec() + "'");
                        }
                        if (entry.getKey().equals(se.getSpec())) {
                            throw new ConfigValidationException(
                                "Circular reference to MDL path '" + se.getSpec() + "'");
                        }
                    }
                }
            }
            if (mdlSpec.getAllow() != null) {
                for (final SpecElement se : mdlSpec.getAllow()) {
                    if (se.getType() == MdlContentSpecType.FromPath) {
                        throw new ConfigValidationException(
                            "\"" + MdlContentSpecType.FromPath + "\" may only occur in deny lists");
                    }
                }
            }
        }
    }



    private static void ensureRegexSyntax(@Nonnull final String pFieldName, @Nonnull final String pRegex)
        throws ConfigValidationException
    {
        try {
            Pattern.compile(pRegex);
        }
        catch (PatternSyntaxException e) {
            throw new ConfigValidationException("Invalid pattern in '" + pFieldName + "' field: " + pRegex, e);
        }
    }
}
