package com.thomasjensen.checkstyle.addons.build;
/*
 * Checkstyle-Addons - Additional Checkstyle checks
 * Copyright (c) 2015-2018, Thomas Jensen and the Checkstyle Addons contributors
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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import net.jcip.annotations.Immutable;


/**
 * The content of a pom.xml.
 */
@XmlRootElement(name = "project")
@SuppressWarnings("unused")
public class PomXml
{
    /** the POM model version */
    public static final String MODEL_VERSION = "4.0.0";

    @XmlAttribute
    private final String xmlns = "http://maven.apache.org/POM/" + MODEL_VERSION;

    @XmlElement
    private final String modelVersion = MODEL_VERSION;

    @XmlElement
    private String groupId;

    @XmlElement
    private String artifactId;

    @XmlElement
    private String version;

    @XmlElement
    private String name;

    @XmlElement
    private String classifier;

    @XmlElement
    private String description;

    @XmlElement
    private String url;

    @XmlElementWrapper
    @XmlElement(name = "dependency")
    private List<DependencyXml> dependencies;

    @XmlElement
    private String inceptionYear;

    @XmlElementWrapper
    @XmlElement(name = "license")
    private List<LicenseXml> licenses;

    @XmlElementWrapper
    @XmlElement(name = "developer")
    private List<DeveloperXml> developers;

    @XmlElement
    private OrganizationXml organization;

    @XmlElement
    private ScmXml scm;



    /**
     * A {@code dependency} node.
     */
    @Immutable
    @XmlRootElement
    public static class DependencyXml
    {
        @XmlElement
        private final String groupId;

        @XmlElement
        private final String artifactId;

        @XmlElement
        private final String version;

        @XmlElement(required = true)
        private final String classifier;

        @XmlElement
        private final String scope;



        /**
         * Constructor.
         *
         * @param pGroupId group id
         * @param pArtifactId artifact id
         * @param pVersion artifact version
         * @param pClassifier classifier
         * @param pScope scope
         */
        public DependencyXml(@Nonnull final String pGroupId, @Nonnull final String pArtifactId,
            @Nonnull final String pVersion, @Nullable final String pClassifier, @Nonnull final String pScope)
        {
            groupId = pGroupId;
            artifactId = pArtifactId;
            version = pVersion;
            classifier = pClassifier;
            scope = pScope;
        }



        /**
         * Required no-args constructor.
         */
        public DependencyXml()
        {
            throw new UnsupportedOperationException("deserialization not supported");
        }
    }



    /**
     * A {@code license} node.
     */
    @Immutable
    @XmlRootElement
    public static class LicenseXml
    {
        @XmlElement
        private final String name;

        @XmlElement
        private final String url;



        /**
         * Constructor.
         *
         * @param pName name of the license
         * @param pUrl URL with the license text
         */
        public LicenseXml(final String pName, final String pUrl)
        {
            name = pName;
            url = pUrl;
        }



        /**
         * Required no-args constructor.
         */
        public LicenseXml()
        {
            throw new UnsupportedOperationException("deserialization not supported");
        }
    }



    /**
     * A {@code developer} node.
     */
    @Immutable
    @XmlRootElement
    public static class DeveloperXml
    {
        @XmlElement
        private final String name;

        @XmlElement
        private final String email;



        /**
         * Constructor.
         *
         * @param pName developer name
         * @param pEmail developer email address
         */
        public DeveloperXml(final String pName, final String pEmail)
        {
            name = pName;
            email = pEmail;
        }



        /**
         * Required no-args constructor.
         */
        public DeveloperXml()
        {
            throw new UnsupportedOperationException("deserialization not supported");
        }
    }



    /**
     * The {@code organization} node.
     */
    @Immutable
    @XmlRootElement
    public static class OrganizationXml
    {
        @XmlElement
        private final String name;

        @XmlElement
        private final String url;



        /**
         * Constructor.
         *
         * @param pName organization name
         * @param pUrl organization website URL
         */
        public OrganizationXml(final String pName, final String pUrl)
        {
            name = pName;
            url = pUrl;
        }



        /**
         * Required no-args constructor.
         */
        public OrganizationXml()
        {
            throw new UnsupportedOperationException("deserialization not supported");
        }
    }



    /**
     * The {@code scm} node.
     */
    @Immutable
    @XmlRootElement
    public static class ScmXml
    {
        @XmlElement
        private final String connection;

        @XmlElement
        private final String developerConnection;

        @XmlElement
        private final String url;



        /**
         * Constructor.
         *
         * @param pConnection git url
         * @param pDeveloperConnection community url
         * @param pUrl GitHub url
         */
        public ScmXml(final String pConnection, final String pDeveloperConnection, final String pUrl)
        {
            connection = pConnection;
            developerConnection = pDeveloperConnection;
            url = pUrl;
        }



        /**
         * Required no-args constructor.
         */
        public ScmXml()
        {
            throw new UnsupportedOperationException("deserialization not supported");
        }
    }



    public void setGroupId(final String pGroupId)
    {
        groupId = pGroupId;
    }



    public void setArtifactId(final String pArtifactId)
    {
        artifactId = pArtifactId;
    }



    public void setVersion(final String pVersion)
    {
        version = pVersion;
    }



    public void setName(final String pName)
    {
        name = pName;
    }



    public void setClassifier(final String pClassifier)
    {
        classifier = pClassifier;
    }



    public void setDescription(final String pDescription)
    {
        description = pDescription;
    }



    public void setUrl(final String pUrl)
    {
        url = pUrl;
    }



    public void setDependencies(final List<DependencyXml> pDependencies)
    {
        dependencies = pDependencies;
    }



    public void setInceptionYear(final String pInceptionYear)
    {
        inceptionYear = pInceptionYear;
    }



    public void setLicenses(final List<LicenseXml> pLicenses)
    {
        licenses = pLicenses;
    }



    public void setDevelopers(final List<DeveloperXml> pDevelopers)
    {
        developers = pDevelopers;
    }



    public void setOrganization(final OrganizationXml pOrganization)
    {
        organization = pOrganization;
    }



    public void setScm(final ScmXml pScm)
    {
        scm = pScm;
    }
}
