package com.thomasjensen.checkstyle.addons.checks;
// @formatter:off


public class InputBinaryName
{
    public static class A   // latin
    {
    }

    public static class Α   // greek
    {
    }

    public static class А  // cyrillic
    {
    }


    public static class $
    {
        public static class B$
        {
            public static class $B
            {
                public static class C
                {
                }
            }
        }
    }


    public static class C$B
    {
    }
/*
    // this would cause a 'duplicate class' error:
    public static class C {
        public static class B {
        }
    }
*/
}
