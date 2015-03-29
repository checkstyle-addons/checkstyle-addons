/*
 * Checkstyle-Addons - Additional Checkstyle checks
 * Copyright (C) 2015 Thomas Jensen, All rights reserved.
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
package com.thomasjensen.checkstyle.addons.sonarqube;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleRepository;
import org.sonar.api.rules.XMLRuleParser;


/**
 * Container for the SonarQube rules provided by this plugin.
 *
 * @author Thomas Jensen
 */
public final class CheckstyleExtensionRepository
    extends RuleRepository
{
    private static final String REPOSITORY_KEY = "checkstyle";

    private static final String REPOSITORY_NAME = "Checkstyle";

    private static final String REPOSITORY_LANGUAGE = "java";

    private static final String RULES_RELATIVE_FILE_PATH = "/" +
        CheckstyleExtensionRepository.class.getPackage().getName().replace('.', '/') +
        "/sonarqube.xml";

    private final XMLRuleParser xmlRuleParser;



    public CheckstyleExtensionRepository(final XMLRuleParser pXmlRuleParser)
    {
        super(REPOSITORY_KEY, REPOSITORY_LANGUAGE);
        setName(REPOSITORY_NAME);
        xmlRuleParser = pXmlRuleParser;
    }



    @Override
    public List<Rule> createRules()
    {
        InputStream input = getClass().getResourceAsStream(RULES_RELATIVE_FILE_PATH);
        try {
            return xmlRuleParser.parse(input);
        }
        finally {
            try {
                if (input != null) {
                    input.close();
                }
            }
            catch (IOException e) {
                // ignore
            }
        }
    }
}
