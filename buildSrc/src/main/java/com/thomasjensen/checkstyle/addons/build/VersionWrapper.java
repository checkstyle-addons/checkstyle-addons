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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.annotation.Nonnull;

import org.gradle.api.GradleException;
import org.gradle.api.Project;


/**
 * Wrapper around our version.properties file.
 */
public class VersionWrapper
{
    private int major;

    private int minor;

    private int patch;

    private boolean snapshot;

    private String strVersion;



    public VersionWrapper(@Nonnull final Project pProject)
    {
        final File versionFile = new File(pProject.getRootDir(), "project/version.properties");
        final Properties props = new Properties();
        try (InputStream fis = new FileInputStream(versionFile)) {
            props.load(fis);
        }
        catch (IOException e) {
            throw new GradleException("error loading version.properties", e);
        }

        major = Integer.parseInt(props.getProperty("major"));
        minor = Integer.parseInt(props.getProperty("minor"));
        patch = Integer.parseInt(props.getProperty("patch"));
        snapshot = !Boolean.parseBoolean(props.getProperty("release"));

        final StringBuilder sb = new StringBuilder();
        sb.append(major);
        sb.append('.');
        sb.append(minor);
        sb.append('.');
        sb.append(patch);
        if (snapshot) {
            sb.append("-SNAPSHOT");
        }

        strVersion = sb.toString();
        pProject.getLogger().lifecycle("Building version: " + strVersion);
    }



    public int getMajor()
    {
        return major;
    }



    public int getMinor()
    {
        return minor;
    }



    public int getPatch()
    {
        return patch;
    }



    public boolean isSnapshot()
    {
        return snapshot;
    }



    @Override
    public String toString()
    {
        return strVersion;
    }
}
