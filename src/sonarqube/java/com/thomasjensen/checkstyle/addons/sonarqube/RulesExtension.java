package com.thomasjensen.checkstyle.addons.sonarqube;
/*
 * Checkstyle-Addons - Additional Checkstyle checks
 * Copyright (c) 2015-2024, the Checkstyle Addons contributors
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
import java.util.Properties;
import javax.annotation.Nonnull;

import org.sonar.api.server.rule.RulesDefinition;


/**
 * "Meta-rule" which defines all the concrete rules.
 */
public class RulesExtension
    implements RulesDefinition
{
    static final String REPOSITORY_KEY = "checkstyle";

    private static final String REPOSITORY_NAME = "Checkstyle";

    private static final String REPOSITORY_LANGUAGE = "java";



    @Override
    public void define(@Nonnull final Context pContext)
    {
        final RulesDefinition.NewRepository repository = pContext.createRepository(REPOSITORY_KEY, REPOSITORY_LANGUAGE)
            .setName(REPOSITORY_NAME);
        final String checkstyleAddonsVersion = readCheckstyleAddonsVersion();

        new IllegalMethodCallRule(repository, checkstyleAddonsVersion).define(pContext);
        new LocationReferenceRule(repository, checkstyleAddonsVersion).define(pContext);
        new LostInstanceRule(repository, checkstyleAddonsVersion).define(pContext);
        new ModuleDirectoryLayoutRule(repository, checkstyleAddonsVersion).define(pContext);
        new PropertyCatalogRule(repository, checkstyleAddonsVersion).define(pContext);
        new RegexpOnFilenameOrgRule(repository, checkstyleAddonsVersion).define(pContext);
        new RegexpOnStringRule(repository, checkstyleAddonsVersion).define(pContext);

        repository.done();
    }



    @Nonnull
    private String readCheckstyleAddonsVersion()
    {
        String result = null;
        Properties props = new Properties();
        try (InputStream is = RulesExtension.class.getResourceAsStream("version.properties")) {
            props.load(is);
            String gitVersion = props.getProperty("version");
            int dashPos = gitVersion.indexOf('-');
            if (dashPos < 0) {
                result = gitVersion;
            }
            else {
                result = gitVersion.substring(0, dashPos);
            }
        }
        catch (IOException | RuntimeException e) {
            throw new IllegalStateException("Checkstyle Addons version cannot be determined", e);
        }
        return result;
    }
}
