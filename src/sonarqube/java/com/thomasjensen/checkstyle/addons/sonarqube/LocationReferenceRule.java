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
import org.sonar.api.server.rule.RuleParamType;


/**
 * SonarQube rule for our <i>LocationReference</i> check.
 */
public class LocationReferenceRule
    extends AbstractRuleBase
{
    public LocationReferenceRule(@Nonnull final NewRepository pSqRepo, @Nonnull final String pCheckstyleAddonsVersion)
    {
        super(pSqRepo, pCheckstyleAddonsVersion, true);
    }



    @Nonnull
    @Override
    protected String getRuleId()
    {
        return "com.thomasjensen.checkstyle.addons.checks.misc.LocationReferenceCheck";
    }



    @Override
    protected void defineRule(@Nonnull final NewRule pNewRule, @Nonnull final String pCheckstyleAddonsVersion)
    {
        pNewRule.setName("Broken Code Location Reference")
            .setInternalKey("Checker/TreeWalker/LocationReference")
            .setHtmlDescription("<p>This check helps in cases where the name of the current method or class must be "
                + "used as an argument to a method call or as initial value of a declared variable. It compares the "
                + "actual argument to the current method or class name, and flags it if a mismatch is detected.</p>\n"
                + "<p><a href=\"http://checkstyle-addons.thomasjensen.com/v" + pCheckstyleAddonsVersion
                + "/checks/misc.html#LocationReference\" target=\"_blank\">Full Documentation</a></p>")
            .setTemplate(true)
            .setSeverity(Severity.MAJOR)
            .setStatus(RuleStatus.READY)
            .setTags("checkstyle-addons", "misc")
            .setType(RuleType.BUG);

        pNewRule.setDebtRemediationFunction(pNewRule.debtRemediationFunctions().constantPerIssue("5min"));

        pNewRule.createParam("methodCalls")
            .setType(RuleParamType.STRING)
            .setDescription("Comma-separated list of method calls that should be checked");

        pNewRule.createParam("variableNames")
            .setType(RuleParamType.STRING)
            .setDescription("Comma-separated list of variable names whose declarations should be checked");

        pNewRule.createParam("location")
            .setType(RuleParamType.singleListOfValues("method", "simpleclass", "fullclass", "classobject"))
            .setDescription("The location information expected here. Possible values are ``method``, ``simpleclass``, "
                + "``fullclass``, or ``classobject``.")
            .setDefaultValue("method");

        pNewRule.createParam("argumentPosition")
            .setType(RuleParamType.INTEGER)
            .setDescription("The position of the location reference as an index of the argument list (used only for "
                + "method calls; ignored for variable assignments)")
            .setDefaultValue("0");
    }
}
