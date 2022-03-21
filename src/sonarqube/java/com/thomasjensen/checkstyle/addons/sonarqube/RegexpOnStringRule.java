package com.thomasjensen.checkstyle.addons.sonarqube;
/*
 * Checkstyle-Addons - Additional Checkstyle checks
 * Copyright (c) 2015-2022, the Checkstyle Addons contributors
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

import javax.annotation.Nonnull;

import org.sonar.api.rule.RuleStatus;
import org.sonar.api.rule.Severity;
import org.sonar.api.server.rule.RuleParamType;


/**
 * SonarQube rule for our <i>RegexpOnString</i> check.
 */
public class RegexpOnStringRule
    extends AbstractRuleBase
{
    public RegexpOnStringRule(@Nonnull final NewRepository pSqRepo, @Nonnull final String pCheckstyleAddonsVersion)
    {
        super(pSqRepo, pCheckstyleAddonsVersion, true);
    }



    @Nonnull
    @Override
    protected String getRuleId()
    {
        return "com.thomasjensen.checkstyle.addons.checks.regexp.RegexpOnStringCheck";
    }



    @Override
    protected void defineRule(@Nonnull final NewRule pNewRule, @Nonnull final String pCheckstyleAddonsVersion)
    {
        pNewRule.setName("Regexp On String")
            .setInternalKey("Checker/TreeWalker/RegexpOnString")
            .setHtmlDescription("<p>Checks String literals against a regular expression. Matching Strings are flagged"
                + ".</p>\n<p><a href=\"http://checkstyle-addons.thomasjensen.com/v" + pCheckstyleAddonsVersion
                + "/checks/regexp.html#RegexpOnString\" target=\"_blank\">Full Documentation</a></p>")
            .setSeverity(Severity.MAJOR)
            .setStatus(RuleStatus.READY)
            .setTags("checkstyle-addons", "regexp")
            .setTemplate(true);

        pNewRule.setDebtRemediationFunction(pNewRule.debtRemediationFunctions().constantPerIssue("1h"));

        pNewRule.createParam("regexp")
            .setType(RuleParamType.STRING)
            .setDescription("The regular expression used to find Strings to flag.")
            .setDefaultValue("^(?!x)x");
    }
}
