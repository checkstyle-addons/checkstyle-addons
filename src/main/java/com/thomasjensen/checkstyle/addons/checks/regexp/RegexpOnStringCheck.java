package com.thomasjensen.checkstyle.addons.checks.regexp;
/*
 * Checkstyle-Addons - Additional Checkstyle checks
 * Copyright (C) 2015 Thomas Jensen
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 3, as published by the Free
 * Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.thomasjensen.checkstyle.addons.checks.AbstractAddonsCheck;
import com.thomasjensen.checkstyle.addons.checks.BinaryName;
import com.thomasjensen.checkstyle.addons.util.Util;


/**
 * This check applies a regular expression to String literals found in the source.
 *
 * <p><a href="http://checkstyle-addons.thomasjensen.com/latest/checks/regexp.html#RegexpOnString"
 * target="_blank">Documentation</a></p>
 *
 * @author Thomas Jensen
 */
public class RegexpOnStringCheck
    extends AbstractAddonsCheck
{
    private static final Set<Integer> TOKEN_TYPES = Collections.singleton(Integer.valueOf(TokenTypes.STRING_LITERAL));

    /** the given regexp */
    private Pattern regexp = Util.NEVER_MATCH;



    @Override
    public Set<Integer> getRelevantTokens()
    {
        return TOKEN_TYPES;
    }



    @Override
    protected void visitToken(@Nullable final BinaryName pBinaryClassName, @Nonnull final DetailAST pAst)
    {
        final String s = pAst.getText().substring(1, pAst.getText().length() - 1);
        if (regexp.matcher(s).find()) {
            log(pAst, "regexp.string", s, regexp);
        }
    }



    /**
     * Setter.
     *
     * @param pRegexp the new value of {@link #regexp}
     */
    public void setRegexp(final String pRegexp)
    {
        if (pRegexp != null && pRegexp.length() > 0) {
            regexp = Pattern.compile(pRegexp);
        }
    }
}
