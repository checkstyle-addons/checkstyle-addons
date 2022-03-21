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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.annotation.Nonnull;

import org.sonar.api.rule.RuleKey;
import org.sonar.api.server.rule.RulesDefinition;


/**
 * Common superclass of all our SonarQube rules.
 */
public abstract class AbstractRuleBase
    implements RulesDefinition
{
    private final NewRepository sqRepo;

    private final String checkstyleAddonsVersion;

    private final boolean shouldRunOnTestCode;



    protected AbstractRuleBase(@Nonnull final NewRepository pSqRepo, @Nonnull final String pCheckstyleAddonsVersion,
        final boolean pShouldRunOnTestCode)
    {
        sqRepo = pSqRepo;
        checkstyleAddonsVersion = pCheckstyleAddonsVersion;
        shouldRunOnTestCode = pShouldRunOnTestCode;
    }



    @Nonnull
    protected final RuleKey getRuleKey()
    {
        return RuleKey.of(RulesExtension.REPOSITORY_KEY, getRuleId());
    }



    @Nonnull
    protected abstract String getRuleId();



    @Override
    public final void define(@Nonnull final Context pContext)
    {
        NewRule rule = sqRepo.createRule(getRuleKey().rule());
        defineRule(rule, checkstyleAddonsVersion);
        setScope(rule);
    }



    /**
     * Reflectively call <code>pRule.setScope(shouldRunOnTestCode ? RuleScope.ALL : RuleScope.MAIN);</code>. This must
     * be performed reflectively so that our <i>sonarqube</i> source set compiles with Java 7 and a really old Sonar
     * API. The scope is set to <code>ALL</code> when the rule should run on test code, and <code>MAIN</code> otherwise.
     *
     * @param pRule the rule whose scope to set
     */
    private void setScope(@Nonnull final NewRule pRule)
    {
        try {
            final Class<?> rsClass = Class.forName("org.sonar.api.rule.RuleScope");
            if (rsClass.isEnum()) {
                for (Object enumValue : rsClass.getEnumConstants()) {
                    Method m = rsClass.getMethod("name");
                    String name = (String) m.invoke(enumValue);
                    if (("ALL".equals(name) && shouldRunOnTestCode) || ("MAIN".equals(name) && !shouldRunOnTestCode)) {
                        Method setScopeMethod = pRule.getClass().getMethod("setScope", rsClass);
                        setScopeMethod.setAccessible(true);
                        setScopeMethod.invoke(pRule, enumValue);
                        break;
                    }
                }
            }
        }
        catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("RuleScope enum was found, but could not be used to set the rule scope", e);
        }
        catch (ClassNotFoundException e) {
            // RuleScope class not found, which means we are API < 7.1. Fine, we do nothing.
        }
    }



    protected abstract void defineRule(@Nonnull NewRule pNewRule, @Nonnull String pCheckstyleAddonsVersion);
}
