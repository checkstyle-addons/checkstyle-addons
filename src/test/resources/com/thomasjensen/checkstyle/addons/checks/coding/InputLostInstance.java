package com.thomasjensen.checkstyle.addons.checks.coding;
// @formatter:off

import java.io.Serializable;


public class InputLostInstance
{
    private static final Object CONSTANT = new IllegalArgumentException();
    private Object field = new IllegalArgumentException();

    static {
        new Integer(42);  // report this
    }

    private static class Inner {
        public void foo();
    }

    private class Inner2 {}

    private class Inner3 {
        private class Inner4 {
            private class Inner5 {}
        }
    }

    public void foo(int arg)
    {
        Object[] arr = new Object[]{new IllegalArgumentException(), new IllegalArgumentException()};
        Object f = new IllegalArgumentException();
        f = CONSTANT;
        f = System.currentTimeMillis() % 2 == 0 ? CONSTANT : new IllegalArgumentException();
        f = System.currentTimeMillis() % 2 == 0 ? new IllegalArgumentException() : CONSTANT;

        bar(new InputLostInstance());
        bar(new Inner2());
        bar(new Inner3().new Inner4());
        new Inner3().new Inner4();  // report this (only last one)
        new Inner3().new Inner4().new Inner5();  // report this (only last one)
        new Inner().foo();

        try {
            new Inner();  // report this
            f = new Inner2();
            new Inner2(); // report this
        }
        catch (IllegalArgumentException e) {
            new IllegalArgumentException();    // report this
        }

        if (System.currentTimeMillis() % 2 == 0)
            new IllegalArgumentException();    // report this
        else
            new NullPointerException();    // report this

        if (System.currentTimeMillis() % 2 == 0) {
            new IllegalArgumentException();    // report this
        } else {
            new IllegalArgumentException();    // report this
        }

        for (int i=0; i <10; new IllegalArgumentException()) { // report this
            break;
        }
        for (new IllegalArgumentException();;) { // report this
            break;
        }
        for (int i=0; i <10; i++) new IllegalArgumentException(); // report this

        throw new IllegalArgumentException();
    }



    public void bar(Object arg)
    {
        do new IllegalArgumentException(); // report this
        while (System.currentTimeMillis() % 2 == 0);

        while (System.currentTimeMillis() % 2 == 0) new IllegalArgumentException(); // report this

        if (System.currentTimeMillis() % 2 == 0) {
            return new IllegalArgumentException();
        }

        String s = "foo " + new IllegalArgumentException();
        String t = new Integer(20042) + new String() + new IllegalArgumentException();
        int i = 43 - new Integer(1);

        return new IllegalArgumentException();
    }



    public Serializable baz()
    {
        new String[]{"foo", "bar"}; // report this
        for (final String s : new String[]{"foo", "bar"}) {
            System.out.println(s);
        }
        return new Serializable() {
            public void foo() {
                new IllegalArgumentException(); // report this
            }
        };
    }
}
