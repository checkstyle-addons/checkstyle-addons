package com.thomasjensen.checkstyle.addons.util;

import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import org.junit.Assert;
import org.junit.Test;

import com.thomasjensen.checkstyle.addons.checks.regexp.RegexpOnStringCheck;


/**
 * Some unit tests for {@link CheckstyleApiFixer}, as far as they are necessary in addition to the others.
 */
public class CheckstyleApiFixerTest
{
    @Test
    public void getTokenName_DOT_ok()
    {
        final CheckstyleApiFixer underTest = new CheckstyleApiFixer(new RegexpOnStringCheck());
        final String tokenName = underTest.getTokenName(TokenTypes.DOT);
        Assert.assertEquals("DOT", tokenName);
    }
}
