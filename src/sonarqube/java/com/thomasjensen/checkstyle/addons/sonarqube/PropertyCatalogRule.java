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
 * SonarQube rule for our <i>PropertyCatalog</i> check.
 */
public class PropertyCatalogRule
    extends AbstractRuleBase
{
    public PropertyCatalogRule(@Nonnull final NewRepository pSqRepo, @Nonnull final String pCheckstyleAddonsVersion)
    {
        super(pSqRepo, pCheckstyleAddonsVersion, true);
    }



    @Nonnull
    @Override
    protected String getRuleId()
    {
        return "com.thomasjensen.checkstyle.addons.checks.misc.PropertyCatalogCheck";
    }



    @Override
    protected void defineRule(@Nonnull final NewRule pNewRule, @Nonnull final String pCheckstyleAddonsVersion)
    {
        pNewRule.setName("Property out of sync")
            .setInternalKey("Checker/TreeWalker/PropertyCatalog")
            .setHtmlDescription("<p>This check helps to keep a property file in sync with a piece of code that "
                + "contains the property keys.</p>\n"
                + "<p><a href=\"http://checkstyle-addons.thomasjensen.com/v" + pCheckstyleAddonsVersion
                + "/checks/misc.html#PropertyCatalog\" target=\"_blank\">Full Documentation</a></p>")
            .setSeverity(Severity.MAJOR)
            .setStatus(RuleStatus.READY)
            .setTags("checkstyle-addons", "misc");

        pNewRule.setDebtRemediationFunction(pNewRule.debtRemediationFunctions().constantPerIssue("10min"));

        pNewRule.createParam("selection")
            .setType(RuleParamType.STRING)
            .setDescription("Regular expression to select the property catalog. Each selected type must have a "
                + "corresponding property file. Applied to the binary class name, not to the file path. Partial "
                + "matches allowed.")
            .setDefaultValue("^(?!x)x");

        pNewRule.createParam("excludedFields")
            .setType(RuleParamType.STRING)
            .setDescription("Regular expression to match fields that should be ignored. Applied to the field name. "
                + "Expression must match the entire field name.")
            .setDefaultValue("serialVersionUID");

        pNewRule.createParam("enumArgument")
            .setType(RuleParamType.BOOLEAN)
            .setDescription("If ``true``, the first argument of the enum constructor is used as property key. If "
                + "``false``, the enum constant itself is used as the property key. Ignored if the type is not an "
                + "``enum``.")
            .setDefaultValue("false");

        pNewRule.createParam("baseDir")
            .setType(RuleParamType.STRING)
            .setDescription("Base directory to assume for the check execution, usually the project root.")
            .setDefaultValue(".");

        pNewRule.createParam("propertyFile")
            .setType(RuleParamType.STRING)
            .setDescription("Template path to the property file. For constructing the path, the following "
                + "placeholders may be used (examples for ``com.foo.Bar$Inner``):\n"
                + "- ``{0}`` - the original binary class name, for example ``com.foo.Bar$Inner``\n"
                + "- ``{1}`` - the binary class name as a path, for example ``com/foo/Bar/Inner``\n"
                + "- ``{2}`` - fully qualified name of the outer class, for example ``com.foo.Bar``\n"
                + "- ``{3}`` - fully qualified name of the outer class as a path, for example ``com/foo/Bar``\n"
                + "- ``{4}`` - fully qualified name of the outer class as a path of ``..``'s, for example "
                + "``../../..``\n"
                + "- ``{5}`` - the package name as a path, for example ``com/foo``\n"
                + "- ``{6}`` - simple name of the outer class, for example ``Bar``\n"
                + "- ``{7}`` - simple name of the inner class, for example ``Inner``\n"
                + "- ``{8}`` - simple name of the first subdirectory below the current working directory on the path "
                + "to the message catalog, for example ``subdir1``\n"
                + "- ``{9}`` - simple name of the next subdirectory on the path to the message catalog, for example "
                + "``subdir2``\n"
                + "- ``{10}`` - simple name of the third subdirectory on the path to the message catalog, for example "
                + "``subdir3``\n"
                + "- ``{11}`` - This placeholder is special because it is dynamic. It is replaced by the empty "
                + "String, ``{8}/``, ``{8}/{9}/``, and ``{8}/{9}/{10}/`` (in that order). Once the property file is "
                + "found, the location is used. If not, the next variation is checked. This is useful when the same "
                + "Checkstyle configuration is used for multiple projects with different structures.\n"
                + "- ``{12}`` - the relative path fragment between the ``baseDir`` and the package directories (e.g. "
                + "``module1/src/main/java``)");

        pNewRule.createParam("propertyFileEncoding")
            .setType(RuleParamType.STRING)
            .setDescription("Character encoding of the property files. Only relevant if non-ASCII characters appear "
                + "in the property keys.")
            .setDefaultValue("UTF-8");

        pNewRule.createParam("reportDuplicates")
            .setType(RuleParamType.BOOLEAN)
            .setDescription("Whether to report duplicate references")
            .setDefaultValue("true");

        pNewRule.createParam("reportOrphans")
            .setType(RuleParamType.BOOLEAN)
            .setDescription("Whether to report unreferenced properties which exist in the property file")
            .setDefaultValue("true");

        pNewRule.createParam("caseSensitive")
            .setType(RuleParamType.BOOLEAN)
            .setDescription("Should property keys be case sensitive?")
            .setDefaultValue("true");

        pNewRule.createParam("fileExludes")
            .setType(RuleParamType.STRING)
            .setDescription("Files whose absolute path matches this regular expression are not checked. "
                + "Partial matches allowed.")
            .setDefaultValue("[\\\\/]\\.idea[\\\\/](?:checkstyleidea\\.tmp[\\\\/])?csi-\\w+[\\\\/]");
    }
}
