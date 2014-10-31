package com.thomasjensen.checkstyle.addons;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.api.Configuration;


/**
 * This class was copied from the Checkstyle project. The original source is <a
 * href="https://github.com/checkstyle/checkstyle/blob/checkstyle-5
 * .8/src/test/java/com/puppycrawl/tools/checkstyle/BaseFileSetCheckTestSupport.java"
 * target="_blank">on GitHub</a>. <p/>Credit goes to Oliver Burn, Ivan Sopov, et al.
 * <p/>
 * Used under the terms of the GNU LESSER GENERAL PUBLIC LICENSE, Version 2.1.
 */
public class BaseFileSetCheckTestSupport
    extends BaseCheckTestSupport
{
    @Override
    protected DefaultConfiguration createCheckerConfig(Configuration aCheckConfig)
    {
        final DefaultConfiguration dc = new DefaultConfiguration("root");
        dc.addChild(aCheckConfig);
        return dc;
    }
}
