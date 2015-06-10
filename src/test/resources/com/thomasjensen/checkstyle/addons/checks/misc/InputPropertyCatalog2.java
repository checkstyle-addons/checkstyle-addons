package com.foo;

import java.io.Serializable;

// @formatter:off
public final class InputPropertyCatalog2
{
    public static final String KEY1 = "zero";
    public static final String KEY2 = "one";

    public static final String KEY3 = "two";


    public void foo()
    {
        new Serializable()   // this is a LITERAL_NEW, not a CLASS_DEF
        {
            // foo
        }.toString();
    }
}
