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
 * SonarQube rule for our <i>ModuleDirectoryLayout</i> check.
 */
public class ModuleDirectoryLayoutRule
    extends AbstractRuleBase
{
    public ModuleDirectoryLayoutRule(@Nonnull final NewRepository pSqRepo,
        @Nonnull final String pCheckstyleAddonsVersion)
    {
        super(pSqRepo, pCheckstyleAddonsVersion, true);
    }



    @Nonnull
    @Override
    protected String getRuleId()
    {
        return "com.thomasjensen.checkstyle.addons.checks.misc.ModuleDirectoryLayoutCheck";
    }



    @Override
    protected void defineRule(@Nonnull final NewRule pNewRule, @Nonnull final String pCheckstyleAddonsVersion)
    {
        pNewRule.setName("Module directory layout not followed")
            .setInternalKey("Checker/ModuleDirectoryLayout")
            .setHtmlDescription("<p>This check helps ensure that the source folder structure in a module follows a "
                + "configurable convention.</p>\n"
                + "<p><a href=\"http://checkstyle-addons.thomasjensen.com/v" + pCheckstyleAddonsVersion
                + "/checks/misc.html#ModuleDirectoryLayout\" target=\"_blank\">Full Documentation</a></p>")
            .setSeverity(Severity.CRITICAL)
            .setStatus(RuleStatus.READY)
            .setTags("checkstyle-addons", "misc", "constraint");

        pNewRule.setDebtRemediationFunction(pNewRule.debtRemediationFunctions().constantPerIssue("10min"));

        pNewRule.createParam("baseDir")
            .setType(RuleParamType.STRING)
            .setDescription("Base directory to assume for the check execution, usually the project root")
            .setDefaultValue(".");

        pNewRule.createParam("configFile")
            .setType(RuleParamType.STRING)
            .setDescription("Location of the JSON configuration file");

        pNewRule.createParam("failQuietly")
            .setType(RuleParamType.BOOLEAN)
            .setDescription("When the file specified in the ``configFile`` option cannot be found, and this flag is "
                + "``true``, the check will quietly disable itself and do nothing. If ``false``, an exception will be "
                + "thrown.")
            .setDefaultValue("false");
    }
}
