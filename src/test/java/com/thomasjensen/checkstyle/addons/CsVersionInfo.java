package com.thomasjensen.checkstyle.addons;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Assert;


/**
 * Utility class for unit tests to query the currently used Checkstyle runtime.
 */
public final class CsVersionInfo
{
    public static final String CSVERSION_SYSPROP_NAME = "com.thomasjensen.checkstyle.addons.checkstyle.version";



    private CsVersionInfo()
    {
        // not used
    }



    public static boolean csVersionIsOneOf(@Nonnull final String pExpectedCsVersion,
        @Nullable final String... pOtherPossibleVersions)
    {
        final String actualCsVersion = currentCsVersion();
        final List<String> expectedVersions = new ArrayList<>();
        expectedVersions.add(pExpectedCsVersion);
        if (pOtherPossibleVersions != null) {
            expectedVersions.addAll(Arrays.asList(pOtherPossibleVersions));
        }
        return expectedVersions.contains(actualCsVersion);
    }



    @Nonnull
    public static String currentCsVersion()
    {
        final String sysPropValue = System.getProperty(CSVERSION_SYSPROP_NAME);
        Assert.assertTrue("System property \"" + CSVERSION_SYSPROP_NAME //
                + "\" does not contain a valid Checkstyle version: " + sysPropValue, //
            VersionComparator.isValidVersion(System.getProperty(CSVERSION_SYSPROP_NAME)));
        return sysPropValue;
    }



    public static Matcher<String> isGreaterThanOrEqualTo(@Nonnull final String pExpectedCsVersion)
    {
        return new TypeSafeMatcher<String>()
        {
            @Override
            protected boolean matchesSafely(final String pActualCsVersion)
            {
                return new VersionComparator().compare(pActualCsVersion, pExpectedCsVersion) >= 0;
            }



            @Override
            public void describeTo(final Description pDescription)
            {
                pDescription.appendText("is greater than").appendValue(pExpectedCsVersion);
            }
        };
    }



    public static Matcher<String> isLessThan(@Nonnull final String pExpectedCsVersion)
    {
        return new TypeSafeMatcher<String>()
        {
            @Override
            protected boolean matchesSafely(final String pActualCsVersion)
            {
                return new VersionComparator().compare(pActualCsVersion, pExpectedCsVersion) < 0;
            }



            @Override
            public void describeTo(final Description pDescription)
            {
                pDescription.appendText("is less than").appendValue(pExpectedCsVersion);
            }
        };
    }
}
