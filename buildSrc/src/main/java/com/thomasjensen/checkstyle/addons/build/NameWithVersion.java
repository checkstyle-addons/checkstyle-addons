package com.thomasjensen.checkstyle.addons.build;
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
 * Common interface of entities processed by the {@link NameFactory}.
 *
 * @author Thomas Jensen
 */
public interface NameWithVersion
{
    /**
     * Getter.
     *
     * @return the name of the entity without any Checkstyle version added to it, usually the Enum constant name
     */
    String getNameWithoutVersion();



    /**
     * Determine the name of the entity including a Checkstyle version number.
     * @param pVersionParam the version number
     * @return the name of the entity including a Checkstyle version, built from it MessageFormat template
     */
    String getNameWithVersion(String pVersionParam);



    /**
     * Flag that tells us how names are expected to be created when the {@link NameFactory} is called with the default
     * Checkstyle version. If <code>true</code>, the name will be resolved by calling {@link #getNameWithoutVersion} .
     * If <code>false</code>, it will be resolved by calling {@link #getNameWithVersion}.
     *
     * @return flag
     */
    boolean useVersionForDefault();
}
