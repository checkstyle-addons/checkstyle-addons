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
        s = 3 + "foo";
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
        s = 3 + "abc" + "def";
        t = "abc" + "def" + 3;
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

    private String s11 = S2 + "bar";   // constant not evaluated

    private String s12 = ("foo".length - (4 - 3)) + "abc" + "def";

    private String s12 = "abc" + ( "def" );

    private String s13 = "ab" + ("cd") + "ef";

    private String s14 = (("ab")) + "cd" + "ef";

    private String s15 = "ab" + ("cd" + "ef");

    private String s16 = ("ab" + "cd") + "ef";

    private Sring method3(final String pStr1) { return null; }

    private String s16 = method3("abc") + "def";
}
