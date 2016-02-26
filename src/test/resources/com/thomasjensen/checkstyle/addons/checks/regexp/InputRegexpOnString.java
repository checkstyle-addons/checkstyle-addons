package com.thomasjensen.checkstyle.addons.checks.regexp;

import java.util.HashMap;
import java.util.Map;


// @formatter:off
public final class InputRegexpOnString
{
    private String s1 = "foo";

    private static final String S2 = "foo";

    private String[] s3 = {"foo", "literal1"};

    private String[] s4 = {"baz", "bar", "foo"};

    private Map<String, String> dbimap = new HashMap<String, String>()
    { {
        put("foo", "bar");
        put("bar", "foo");
        put("baz", "baz");
    } };



    @SuppressWarnings("foo")
    private void method(final String pStr1)
    {
        String s = "foo";
        s = 3 + "foo";
    }



    @SuppressWarnings({"bar", "baz", "foo"})
    public static void main(final String[] pArgs)
    {
        InputRegexpOnString obj = new InputRegexpOnString();
        obj.method("foo");
    }



    private String empty = "";
}
