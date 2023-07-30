package com.thomasjensen.checkstyle.addons.sonarqube;
/*
 * Checkstyle-Addons - Additional Checkstyle checks
 * Copyright (c) 2015-2023, the Checkstyle Addons contributors
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
 * SonarQube rule for our <i>IllegalMethodCall</i> check.
 */
public class IllegalMethodCallRule
    extends AbstractRuleBase
{
    public IllegalMethodCallRule(@Nonnull final NewRepository pSqRepo, @Nonnull final String pCheckstyleAddonsVersion)
    {
        super(pSqRepo, pCheckstyleAddonsVersion, true);
    }



    @Nonnull
    @Override
    protected String getRuleId()
    {
        return "com.thomasjensen.checkstyle.addons.checks.coding.IllegalMethodCallCheck";
    }



    @Override
    protected void defineRule(@Nonnull final NewRule pNewRule, @Nonnull final String pCheckstyleAddonsVersion)
    {
        pNewRule.setName("Illegal Method Call")
            .setInternalKey("Checker/TreeWalker/IllegalMethodCall")
            .setHtmlDescription("<p>Flags calls to methods with certain names. Occurrences are flagged based on the "
                + "name alone; the type of the object to which the method belongs is not taken into account.</p>"
                + "<p><a href=\"http://checkstyle-addons.thomasjensen.com/v" + pCheckstyleAddonsVersion
                + "/checks/coding.html#IllegalMethodCall\" target=\"_blank\">Full Documentation</a></p>")
            .setSeverity(Severity.BLOCKER)
            .setStatus(RuleStatus.READY)
            .setTags("checkstyle-addons", "coding", "constraint");

        pNewRule.setDebtRemediationFunction(pNewRule.debtRemediationFunctions().constantPerIssue("1h"));

        pNewRule.createParam("illegalMethodNames")
            .setType(RuleParamType.STRING)
            .setDescription("Comma-separated list of plain method names, no parameters, no parentheses");

        pNewRule.createParam("excludedQualifiers")
            .setType(RuleParamType.STRING)
            .setDescription("Comma-separated list of method call qualifiers indicating false positives");
    }
}
