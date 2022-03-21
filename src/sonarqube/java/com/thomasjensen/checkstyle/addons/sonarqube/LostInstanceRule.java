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
import org.sonar.api.rules.RuleType;


/**
 * SonarQube rule for our <i>LostInstance</i> check.
 */
public class LostInstanceRule
    extends AbstractRuleBase
{
    public LostInstanceRule(@Nonnull final NewRepository pSqRepo, @Nonnull final String pCheckstyleAddonsVersion)
    {
        super(pSqRepo, pCheckstyleAddonsVersion, false);
    }



    @Nonnull
    @Override
    protected String getRuleId()
    {
        return "com.thomasjensen.checkstyle.addons.checks.coding.LostInstanceCheck";
    }



    @Override
    protected void defineRule(@Nonnull final NewRule pNewRule, @Nonnull final String pCheckstyleAddonsVersion)
    {
        pNewRule.setName("Lost Instance")
            .setInternalKey("Checker/TreeWalker/LostInstance")
            .setHtmlDescription("<p>Checks that object instances created explicitly with <code>new</code> are "
                + "actually used for something. Just being assigned to a variable or passed as a parameter is "
                + "enough. A full data flow analysis is not performed.</p>\n"
                + "<p><a href=\"http://checkstyle-addons.thomasjensen.com/v" + pCheckstyleAddonsVersion
                + "/checks/coding.html#LostInstance\" target=\"_blank\">Full Documentation</a></p>")
            .setSeverity(Severity.CRITICAL)
            .setStatus(RuleStatus.READY)
            .setTags("checkstyle-addons", "coding")
            .setType(RuleType.BUG);

        pNewRule.setDebtRemediationFunction(pNewRule.debtRemediationFunctions().constantPerIssue("5min"));
    }
}
