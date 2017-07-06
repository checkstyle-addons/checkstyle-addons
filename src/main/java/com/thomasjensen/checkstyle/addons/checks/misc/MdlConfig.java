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

import java.util.regex.Pattern;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.jcip.annotations.Immutable;


/**
 * Encapsulates the data gained from reading the JSON configuration file.
 */
@Immutable
public class MdlConfig
{
    private final MdlJsonConfig json;

    private final Pattern moduleRegex;

    private final Pattern excludeRegex;



    public MdlConfig(@Nullable final MdlJsonConfig pJson, @Nonnull final Pattern pModuleRegex,
        @Nonnull final Pattern pExcludeRegex)
    {
        json = pJson;
        moduleRegex = pModuleRegex;
        excludeRegex = pExcludeRegex;
    }



    /**
     * Getter.
     *
     * @return the parsed contents of the UTF-8 encoded configuration file in JSON
     */
    @CheckForNull
    public MdlJsonConfig getJson()
    {
        return json;
    }



    @Nonnull
    public Pattern getModuleRegex()
    {
        return moduleRegex;
    }



    @Nonnull
    public Pattern getExcludeRegex()
    {
        return excludeRegex;
    }
}
