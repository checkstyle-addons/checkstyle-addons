package com.thomasjensen.checkstyle.addons.build.tasks;
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

import java.util.Iterator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import groovy.util.Node;
import org.gradle.api.GradleException;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencyArtifact;
import org.gradle.api.artifacts.ExternalDependency;
import org.gradle.api.publish.maven.tasks.GenerateMavenPom;

import com.thomasjensen.checkstyle.addons.build.BuildUtil;
import com.thomasjensen.checkstyle.addons.build.ClasspathBuilder;
import com.thomasjensen.checkstyle.addons.build.DependencyConfig;


/**
 * Add the right dependency information to a generated Maven POM.
 */
public class GeneratePomConfigAction
    extends AbstractTaskConfigAction<GenerateMavenPom>
{
    public GeneratePomConfigAction(@Nonnull final DependencyConfig pDepConfig)
    {
        super(pDepConfig);
    }



    @Override
    protected void configureTaskFor(@Nonnull GenerateMavenPom pPomTask, @Nullable DependencyConfig pDepConfig)
    {
        pPomTask.getPom().withXml(xml ->
            buildDependencies4Pom(xml.asNode().appendNode("dependencies"), pDepConfig));
    }



    private void buildDependencies4Pom(@Nonnull final Node pParentNode, @Nonnull final DependencyConfig pDepConfig)
    {
        final Configuration cfg = new ClasspathBuilder(project).buildMainRuntimeConfiguration(pDepConfig);
        for (Dependency d : cfg.getAllDependencies()) {
            if (!(d instanceof ExternalDependency)) {
                throw new GradleException("Incompatible dependency: " + d);
            }
            final ExternalDependency jar = (ExternalDependency) d;
            final Iterator<DependencyArtifact> artifactIterator = jar.getArtifacts().iterator();
            final DependencyArtifact artifcat = artifactIterator.hasNext() ? artifactIterator.next() : null;
            final String depClassifier = artifcat != null ? artifcat.getClassifier() : null;

            final Node n = pParentNode.appendNode("dependency");
            n.appendNode(BuildUtil.GROUP_ID, jar.getGroup());
            n.appendNode(BuildUtil.ARTIFACT_ID, jar.getName());
            n.appendNode(BuildUtil.VERSION, jar.getVersion());
            if (depClassifier != null) {
                n.appendNode("classifier", depClassifier);
            }
            n.appendNode("scope", "compile");
        }
    }
}
