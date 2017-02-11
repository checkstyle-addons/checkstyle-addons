package com.thomasjensen.checkstyle.addons.build.tasks;
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

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import groovy.lang.Closure;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencyArtifact;
import org.gradle.api.artifacts.ExternalDependency;
import org.gradle.api.publish.plugins.PublishingPlugin;
import org.gradle.api.tasks.TaskInputs;

import com.thomasjensen.checkstyle.addons.build.BuildUtil;
import com.thomasjensen.checkstyle.addons.build.ExtProp;
import com.thomasjensen.checkstyle.addons.build.PomXml;


/**
 * Gradle task to generate pom.xml files.
 *
 * @author Thomas Jensen
 */
public class GeneratePomFileTask
    extends DefaultTask
{
    private final File pomFile = new File(getTemporaryDir(), "pom.xml");

    private String appendix = null;

    private final List<Configuration> bundledConfigs = new ArrayList<>();

    private final BuildUtil buildUtil;



    /**
     * Constructor.
     */
    public GeneratePomFileTask()
    {
        super();
        setGroup(PublishingPlugin.PUBLISH_TASK_GROUP);

        final Project project = getProject();
        buildUtil = new BuildUtil(project);

        final TaskInputs inputs = getInputs();
        inputs.property("groupId", project.getGroup());
        inputs.property("artifactId", project.getName());
        inputs.property("version", project.getVersion());
        if (getAppendix() != null) {
            inputs.property("appendix", getAppendix());
        }
        inputs.property("name", buildUtil.getLongName());
        inputs.property("description", project.getDescription());
        inputs.property("url", buildUtil.getExtraPropertyValue(ExtProp.Website));
        inputs.property("authorName", buildUtil.getExtraPropertyValue(ExtProp.AuthorName));
        inputs.property("orgName", buildUtil.getExtraPropertyValue(ExtProp.OrgName));
        inputs.property("orgUrl", buildUtil.getExtraPropertyValue(ExtProp.OrgUrl));
        inputs.property("github", buildUtil.getExtraPropertyValue(ExtProp.Github));

        getOutputs().file(pomFile);

        doLast(new Closure<Void>(this)
        {
            @Override
            @SuppressWarnings({"ResultOfMethodCallIgnored", "MethodDoesntCallSuperMethod"})
            public Void call()
            {
                pomFile.getParentFile().mkdirs();
                PomXml pomXml = createPom();
                try {
                    writePomXml(pomXml);
                }
                catch (JAXBException e) {
                    throw new GradleException("error creating pom", e);
                }
                return null;
            }
        });
    }



    private PomXml createPom()
    {
        final TaskInputs inputs = getInputs();

        String effectiveArtifactId = (String) inputs.getProperties().get("artifactId");
        if (getAppendix() != null) {
            effectiveArtifactId += '-' + getAppendix();
        }

        final PomXml pom = new PomXml();
        pom.setGroupId((String) inputs.getProperties().get("groupId"));
        pom.setArtifactId(effectiveArtifactId);
        pom.setVersion((String) inputs.getProperties().get("version"));
        pom.setName((String) inputs.getProperties().get("name"));
        if (inputs.getProperties().containsKey("classifier")) {
            pom.setClassifier((String) inputs.getProperties().get("classifier"));
        }
        pom.setDescription((String) inputs.getProperties().get("description"));
        pom.setUrl((String) inputs.getProperties().get("url"));

        if (bundledConfigs.size() > 0) {
            final List<PomXml.DependencyXml> dependencies = new ArrayList<>();
            for (final Configuration cfg : bundledConfigs) {
                for (Dependency d : cfg.getAllDependencies()) {
                    if (!(d instanceof ExternalDependency)) {
                        throw new GradleException("Incompatible dependency: " + d);
                    }
                    final ExternalDependency jar = (ExternalDependency) d;
                    final Iterator<DependencyArtifact> artifactIterator = jar.getArtifacts().iterator();
                    final DependencyArtifact artifcat = artifactIterator.hasNext() ? artifactIterator.next() : null;
                    final String depClassifier = artifcat != null ? artifcat.getClassifier() : null;

                    PomXml.DependencyXml dep = new PomXml.DependencyXml(jar.getGroup(), jar.getName(), jar.getVersion(),
                        depClassifier, "compile");
                    dependencies.add(dep);
                }
            }
            pom.setDependencies(dependencies);
        }

        pom.setInceptionYear("2015");

        pom.setLicenses(Collections.singletonList(
            new PomXml.LicenseXml("GNU General Public License, Version 3", "https://www.gnu.org/copyleft/gpl.html")));
        pom.setDevelopers(Collections.singletonList(
            new PomXml.DeveloperXml((String) inputs.getProperties().get("authorName"), "checkstyle@thomasjensen.com")));
        pom.setOrganization(new PomXml.OrganizationXml((String) inputs.getProperties().get("orgName"),
            (String) inputs.getProperties().get("orgUrl")));
        pom.setScm(new PomXml.ScmXml("scm:git:git@github.com:" + inputs.getProperties().get("github") + ".git",
            "scm:git:git@github.com:" + inputs.getProperties().get("github") + ".git",
            "git@github.com:" + inputs.getProperties().get("github") + ".git"));
        return pom;
    }



    private void writePomXml(final PomXml pPomXml)
        throws JAXBException
    {
        final JAXBContext jaxbContext = JAXBContext.newInstance(PomXml.class);
        final Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, StandardCharsets.UTF_8.toString());
        marshaller.setProperty("com.sun.xml.internal.bind.xmlHeaders", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION,
            "http://maven.apache.org/POM/" + PomXml.MODEL_VERSION + " http://maven.apache.org/xsd/maven-"
                + PomXml.MODEL_VERSION + ".xsd");
        marshaller.marshal(pPomXml, pomFile);
    }



    public String getAppendix()
    {
        return this.appendix;
    }



    /**
     * Setter.
     *
     * @param pAppendix the appendix to use for this artifact
     */
    public void setAppendix(final String pAppendix)
    {
        this.appendix = pAppendix;
        if (pAppendix != null) {
            getInputs().property("appendix", pAppendix);
        }
        else {
            getInputs().getProperties().remove("appendix");
        }
    }



    /**
     * Add configurations whose artifacts should be listed as dependencies in the POM.
     *
     * @param pConfigs said configurations
     */
    public void addBundledConfigs(final Configuration... pConfigs)
    {
        if (pConfigs != null && pConfigs.length > 0) {
            for (final Configuration cfg : pConfigs) {
                if (cfg != null) {
                    boolean duplicate = false;
                    for (final Configuration c : bundledConfigs) {
                        if (c.getName().equals(cfg.getName())) {
                            duplicate = true;
                            break;
                        }
                    }
                    if (!duplicate) {
                        bundledConfigs.add(cfg);
                    }
                }
            }
        }
    }



    public File getPomFile()
    {
        return pomFile;
    }
}
