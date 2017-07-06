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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleRepository;
import org.sonar.api.rules.XMLRuleParser;


/**
 * Container for the SonarQube rules provided by this plugin.
 */
public final class CheckstyleExtensionRepository
    extends RuleRepository
{
    private static final String REPOSITORY_KEY = "checkstyle";

    private static final String REPOSITORY_NAME = "Checkstyle";

    private static final String REPOSITORY_LANGUAGE = "java";

    private final String rulesRelativeFilePath;

    private final XMLRuleParser xmlRuleParser;



    /**
     * Constructor.
     *
     * @param pXmlRuleParser the XML rule parser
     */
    public CheckstyleExtensionRepository(final XMLRuleParser pXmlRuleParser)
    {
        this(pXmlRuleParser,
            "/" + CheckstyleExtensionRepository.class.getPackage().getName().replace('.', '/') + "/sonarqube.xml");
    }



    /**
     * Constructor for tests.
     *
     * @param pXmlRuleParser the XML rule parser
     * @param pFilePath the path by which to find the rules description file
     */
    CheckstyleExtensionRepository(final XMLRuleParser pXmlRuleParser, final String pFilePath)
    {
        super(REPOSITORY_KEY, REPOSITORY_LANGUAGE);
        setName(REPOSITORY_NAME);
        xmlRuleParser = pXmlRuleParser;
        rulesRelativeFilePath = pFilePath;
    }



    @Override
    @SuppressFBWarnings("DE_MIGHT_IGNORE")
    public List<Rule> createRules()
    {
        InputStream input = getClass().getResourceAsStream(rulesRelativeFilePath);
        if (input == null) {
            throw new IllegalStateException("File not found: " + rulesRelativeFilePath);
        }
        try {
            return xmlRuleParser.parse(input);
        }
        finally {
            try {
                input.close();
            }
            catch (IOException e) {
                // ignore
            }
        }
    }
}
