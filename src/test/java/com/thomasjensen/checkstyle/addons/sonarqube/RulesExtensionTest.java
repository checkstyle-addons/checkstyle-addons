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

import org.junit.Test;
import org.sonar.api.server.rule.RulesDefinition;


/**
 * Very basic unit test of our {@link RulesExtension} class.
 */
public class RulesExtensionTest
{
    @Test
    public void testRulesDefinition()
    {
        RulesDefinition.Context mockCtx = new RulesDefinition.Context();
        RulesExtension underTest = new RulesExtension();
        underTest.define(mockCtx);
    }
}
