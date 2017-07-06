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
import java.io.IOException;
import javax.annotation.Nonnull;

import com.thomasjensen.checkstyle.addons.util.Util;

// @formatter:off
/**
 * Small utility which allows people to validate the syntactical correctness of their ModuleDirectoryLayout JSON
 * config.
 * <p>Call from command line: <code>java -cp checkstyle-addons-x.x.x-all.jar;checkstyle-7.0-all.jar
 * com.thomasjensen.checkstyle.addons.checks.misc.MdlJsonConfigValidator path/to/my-directories.json</code></p>
 */ // @formatter:on
public final class MdlJsonConfigValidator
{
    private MdlJsonConfigValidator()
    {
        super();
    }



    /**
     * Main.
     *
     * @param pArgs command-line arguments
     */
    public static void main(@Nonnull final String[] pArgs)
    {
        if (pArgs.length == 0) {
            System.err.println("ModuleDirectoryLayout configuration file not specified.");
            System.exit(1);
        }

        final File inputFile = Util.canonize(new File(pArgs[0]));
        System.out.println("Checking " + inputFile);

        MdlJsonConfig jsonConfig = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(inputFile);
            jsonConfig = ModuleDirectoryLayoutCheck.readConfigFile(fis);
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(2);
        }
        finally {
            Util.closeQuietly(fis);
        }

        try {
            jsonConfig.validate();
        }
        catch (ConfigValidationException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(2);
        }

        System.out.println("ModuleDirectoryLayout configuration file OK");
    }
}
