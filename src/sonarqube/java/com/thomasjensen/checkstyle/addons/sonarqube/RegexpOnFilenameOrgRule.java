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
 * SonarQube rule for our <i>RegexpOnFilenameOrg</i> check.
 */
public class RegexpOnFilenameOrgRule
    extends AbstractRuleBase
{
    public RegexpOnFilenameOrgRule(@Nonnull final NewRepository pSqRepo, @Nonnull final String pCheckstyleAddonsVersion)
    {
        super(pSqRepo, pCheckstyleAddonsVersion, true);
    }



    @Nonnull
    @Override
    protected String getRuleId()
    {
        return "com.thomasjensen.checkstyle.addons.checks.regexp.RegexpOnFilenameOrgCheck";
    }



    @Override
    protected void defineRule(@Nonnull final NewRule pNewRule, @Nonnull final String pCheckstyleAddonsVersion)
    {
        pNewRule.setName("Regexp On Filename (Original)")
            .setInternalKey("Checker/RegexpOnFilenameOrg")
            .setHtmlDescription("<p>Checks the names of files against a regular expression.</p>\n"
                + "<p><a href=\"http://checkstyle-addons.thomasjensen.com/v" + pCheckstyleAddonsVersion
                + "/checks/regexp.html#RegexpOnFilenameOrg\" target=\"_blank\">Full Documentation</a></p>")
            .setSeverity(Severity.MAJOR)
            .setStatus(RuleStatus.READY)
            .setTags("checkstyle-addons", "regexp")
            .setTemplate(true);

        pNewRule.setDebtRemediationFunction(pNewRule.debtRemediationFunctions().constantPerIssue("30min"));

        pNewRule.createParam("selection")
            .setType(RuleParamType.STRING)
            .setDescription("Only files which match this expression will be checked. Leave blank for unrestricted.");

        pNewRule.createParam("regexp")
            .setType(RuleParamType.STRING)
            .setDescription("The regular expression to apply to the selected file names. The default expression "
                + "matches when there are leading or trailing spaces in a file name.")
            .setDefaultValue("^(?:\\s+.*|.*?\\s+)$");

        pNewRule.createParam("mode")
            .setType(RuleParamType.singleListOfValues("illegal", "required"))
            .setDescription("Determines if the regular expression must match (``required``) or must not match "
                + "(``illegal``).")
            .setDefaultValue("illegal");

        pNewRule.createParam("simple")
            .setType(RuleParamType.BOOLEAN)
            .setDescription("Check the simple file name only (``true``), or the entire path (``false``).")
            .setDefaultValue("true");
    }
}
