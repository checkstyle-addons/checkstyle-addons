package com.foo;

// @formatter:off
public enum InputPropertyCatalog4
{
    Zero(0, false),

    One(1, true),

    Two(2, true);

    private final int key;

    private final boolean bool;



    private InputPropertyCatalog4(final int pKey, final boolean pBool)
    {
        bool = pBool;
        key = pKey;
    }



    public boolean isBool()
    {
        return bool;
    }



    public int getKey()
    {
        return key;
    }
}
