package com.thomasjensen.checkstyle.addons.checks.coding;
// @formatter:off

public class Test
{
    public String foo(Object x)
    {
        return switch (x) {
            case String y -> {
                yield new String("42");
            }
            default -> "42";
        };
    }
}
