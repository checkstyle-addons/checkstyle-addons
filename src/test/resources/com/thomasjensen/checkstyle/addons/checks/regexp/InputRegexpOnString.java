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
        String s = "foo" + "bar";
        s = 3 + "foo";  // NOT flagged because of mixed AST
    }



    @SuppressWarnings({"bar", "baz", "foo"})
    public static void main(final String[] pArgs)
    {
        InputRegexpOnString obj = new InputRegexpOnString();
        obj.method("foo");
        obj.method2("abc" + "def" + "ghi");
    }



    private String empty = "";



    @SuppressWarnings("abc" + "def" + "ghi")
    private void method2(final String pStr1)
    {
        String s = "abc" + "def" + "ghi";
        s = 3 + "abc" + "def";  // NOT flagged because of mixed AST
        t = "abc" + "def" + 3;  // NOT flagged because of mixed AST
    }



    private String s5 = "abc" + "def";

    private static final String S6 = "abc" + "def" + "ghi";

    private String[] s7 = {"abc" + "def" + "ghi", "literal2"};

    private String[] s8 = {"baz", "bar", "abc" + "def"};

    private String s9 = "aaa"  // line break
        + "zzz";

    private String longString = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        + "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"
        + "cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc"
        + "dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd"
        + "eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee"
        + "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"
        + "gggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggg";

    private String s10 = "liter" + "al3";
}
