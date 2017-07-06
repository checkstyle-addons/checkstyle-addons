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



/**
 * The ModuleDirectoryLayout check's JSON configuration was faulty.
 */
public class ConfigValidationException
    extends Exception
{
    /**
     * Constructor.
     *
     * @param pMessage the message
     */
    public ConfigValidationException(final String pMessage)
    {
        super(pMessage);
    }



    /**
     * Constructor.
     *
     * @param pMessage the message
     * @param pCause the cause
     */
    public ConfigValidationException(final String pMessage, final Throwable pCause)
    {
        super(pMessage, pCause);
    }
}
