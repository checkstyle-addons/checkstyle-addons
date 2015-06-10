package com.thomasjensen.checkstyle.addons.checks.coding;
// @formatter:off

public class InputIllegalMethodCall
{
    private static final Class<?> field = Class.forName("com.foo.Bar");  // report this

    static {
        Class.forName("com.foo.Bar");  // report this
        String error1 = Class.forName("com.foo.Bar").getName();  // report this
    }

    public void foo()
    {
        // Class.forName("com.foo.Bar")        // no problem, comment
        String error2 = Class.forName("com.foo.Bar").getName();  // report this
        String good = "Class.forName(\"com.foo.Bar\"); is the illegal method call";  // no problem, String
        String.valueOf(Class.forName("com.foo.Bar").getName().toCharArray());  // report this
        java.lang.Class.forName("com.foo.Bar").getName();  // report this
        forName("com.foo.Bar");  // report this

        InputIllegalMethodCall obj = new InputIllegalMethodCall();
        obj.<String>forName("com.foo.Bar");  // report this
    }

    private void forName(final String pFoo)
    {
        String forName = "no problem";
    }


    private <T> T forName(final T pFoo)
    {
        return pFoo;
    }


    private bar()
    {
        Inner1.forName("com.foo.Bar");  // report this
        Inner1.Inner2.forName("com.foo.Bar");  // report this
    }


    private void baz()
    {
        Inner1 inner1 = new Inner1();
        String foo = inner1.method1("foo");
        inner1.<String>method1("foo");
    }


    public static class Inner1 {
        private static void forName(final String pFoo)
        {
            // comment
        }

        private <T> T method1(final T pFoo)
        {
            // comment
            return pFoo;
        }

        public static class Inner2 {
            private static void forName(final String pFoo)
            {
                // comment
            }
        }
    }
}
