package com.thomasjensen.checkstyle.addons.checks.regexp;
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

/**
 * Represents the possible modes of operation for the {@link RegexpOnFilenameCheck}.
 *
 * @author Thomas Jensen
 */
public enum RegexpOnFilenameOption
{
    /**
     * In REQUIRED mode, the regular expression must match the filename, and a violation is logged if the regexp does
     * <i>not</i> match.
     */
    REQUIRED,

    /**
     * In ILLEGAL mode, the regular expression must <i>not</i> match the filename, and a violation is logged if the
     * regexp matched anyway.
     */
    ILLEGAL;
}
