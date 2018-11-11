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

import java.io.File;
import java.io.FilenameFilter;
import javax.annotation.Nonnull;


/**
 * FilenameFilter which selects files that have a <code>.properties</code> extension.
 */
public class PropertyFileFilter
    implements FilenameFilter
{
    @Override
    public boolean accept(@Nonnull final File pDir, @Nonnull final String pName)
    {
        return pName.endsWith(".properties");
    }
}
