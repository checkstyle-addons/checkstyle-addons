package com.foo;

// @formatter:off
public interface InputPropertyCatalog6
{
    int KEY1 = 0;

    int KEY2 = 1;


    public static class Foo
    {
        public static final int KEY1 = 0;

        public static final int KEY2 = 1;

        public static final int KEY3 = 2;
    }


    public static class Orphaned1
    {
        public static final int KEY1 = 0;

        public static final int KEY3 = 2;
    }


    int KEY3 = 2;


    @Deprecated
    public static class Orphaned2
    {
        public static final int KEY1 = 0;
    }
}
