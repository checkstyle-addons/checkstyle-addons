package com.thomasjensen.checkstyle.addons.checks.misc;

import java.io.Serializable;


// @formatter:off
public final class InputPropertyCatalog9Excl
    implements Serializable
{
    public static final String LOG = "imagine this was a logger";   // excluded by pattern in test

    private static final long serialVersionUID = 42L;  // excluded because it is private

    public static final int KEY1 = 0;

    public static final int KEY2_NO_EXCLUDE = 1;

    public static final String KEY3 = "2";

    public static final int EXCLUDE_KEY1 = 3;   // excluded by pattern in test
    public static final int EXCLUDE_KEY2 = 4;   // excluded by pattern in test
    public static final int EXCLUDE_KEY3 = 5;   // excluded by pattern in test

    private void foo()
    {
        final int var = 0;
    }
}
