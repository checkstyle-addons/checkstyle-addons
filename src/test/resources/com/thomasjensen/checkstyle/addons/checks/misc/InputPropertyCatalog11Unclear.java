package com.thomasjensen.checkstyle.addons.checks.misc;

// @formatter:off
public enum InputPropertyCatalog11Unclear
{
    Zero(0),  // only this one is ok

    One(42 / 2),

    Two(One.getKey() - 1),

    Three(val()),

    Four(),

    Five;

    private final int key;



    private InputPropertyCatalog11Unclear(final int pKey)
    {
        key = pKey;
    }

    private InputPropertyCatalog11Unclear()
    {
        key = 42;
    }

    public int getKey()
    {
        return key;
    }

    private static int val()
    {
        return 42;
    }
}
