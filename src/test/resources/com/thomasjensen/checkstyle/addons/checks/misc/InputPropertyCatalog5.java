package com.foo;

// @formatter:off
public enum InputPropertyCatalog5
{
    Zero("zero"),

    One("one"),

    Two("two");

    private final String pKey;



    private InputPropertyCatalog4(final String pKey)
    {
        key = pKey;
    }



    public String getKey()
    {
        return key;
    }
}
