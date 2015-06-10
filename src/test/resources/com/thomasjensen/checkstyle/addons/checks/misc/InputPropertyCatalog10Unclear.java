package com.thomasjensen.checkstyle.addons.checks.misc;

// @formatter:off
public final class InputPropertyCatalog10Unclear
{
    public static final int ONLY_THIS_ONE_IS_OK = 0;

    public static final int KEY1 = 42 / 2;

    public static final int KEY2 = val();

    public static final String KEY3 = KEY1 - 1;

    public static final String FOO;

    static {
        FOO = "foo";
    }

    private static int val()
    {
        return 42;
    }
}
