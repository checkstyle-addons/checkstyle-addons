package com.thomasjensen.checkstyle.addons.build;
/*
 * Checkstyle-Addons - Additional Checkstyle checks
 * Copyright (c) 2015-2018, Thomas Jensen and the Checkstyle Addons contributors
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

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.jcip.annotations.Immutable;


/**
 * The key in the {@link ClasspathBuilder}'s cache of detached configurations.
 */
@Immutable
public final class CacheKey
{
    private final String depConfigName;

    private final String csVersion;

    private final String classPathConfigName;



    public CacheKey(@Nullable final String pDepConfigName, @Nullable final String pCsVersion,
        @Nonnull final String pClassPathConfigName)
    {
        depConfigName = pDepConfigName != null ? pDepConfigName : DependencyConfigs.DEFAULT_NAME;
        csVersion = pCsVersion;
        classPathConfigName = pClassPathConfigName;
    }



    @Override
    public boolean equals(final Object pOther)
    {
        if (this == pOther) {
            return true;
        }
        if (pOther == null || getClass() != pOther.getClass()) {
            return false;
        }
        CacheKey other = (CacheKey) pOther;
        return Objects.equals(depConfigName, other.depConfigName) && Objects.equals(csVersion, other.csVersion)
            && Objects.equals(classPathConfigName, other.classPathConfigName);
    }



    @Override
    public int hashCode()
    {
        return Objects.hash(depConfigName, csVersion, classPathConfigName);
    }
}
