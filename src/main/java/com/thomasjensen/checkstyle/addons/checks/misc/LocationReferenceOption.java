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

import javax.annotation.Nonnull;

import com.thomasjensen.checkstyle.addons.util.Util;


/**
 * Represents the possible locations that an instance of the {@link LocationReferenceCheck} may refer to.
 */
public enum LocationReferenceOption
{
    /** the name of the current method, as a {@link String} literal */
    Method,

    /** the simple name of the current type, as a {@link String} literal */
    SimpleClass,

    /** the fully qualified name of the current type, as a {@link String} literal */
    FullClass,

    /** the current type, as a static {@link Class} object (e.g. <code>MyClass.class</code>) */
    ClassObject;



    /**
     * Variant of {@link Enum#valueOf} that ignores value case.
     *
     * @param pValue the String value
     * @return the enum value
     *
     * @throws IllegalArgumentException the given String value does not match a valid enum value
     */
    @Nonnull
    public static LocationReferenceOption valueOfIgnoreCase(@Nonnull final String pValue)
    {
        return Util.valueOfIgnoreCase(pValue, LocationReferenceOption.class);
    }
}
