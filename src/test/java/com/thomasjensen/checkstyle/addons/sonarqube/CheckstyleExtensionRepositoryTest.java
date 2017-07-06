package com.thomasjensen.checkstyle.addons.sonarqube;
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

import java.util.List;

import junit.framework.TestCase;
import org.junit.Test;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.XMLRuleParser;


/**
 * Unit test of {@link CheckstyleExtensionRepository}.
 */
public class CheckstyleExtensionRepositoryTest
{
    @Test
    public void testNormalConstructor()
    {
        CheckstyleExtensionRepository testee = new CheckstyleExtensionRepository(new XMLRuleParser());
        TestCase.assertNotNull(testee);
    }



    @Test(expected = IllegalStateException.class)
    public void testNonexistentFile()
    {
        CheckstyleExtensionRepository testee = new CheckstyleExtensionRepository(new XMLRuleParser(),
            "nonexistent.xml");
        testee.createRules();
    }



    @Test
    public void testCreateRules()
    {
        CheckstyleExtensionRepository testee = new CheckstyleExtensionRepository(new XMLRuleParser(), "/sonarqube.xml");
        List<Rule> rules = testee.createRules();
        TestCase.assertNotNull(rules);
        TestCase.assertTrue(rules.size() > 0);
    }
}
