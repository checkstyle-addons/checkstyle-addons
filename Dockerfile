# Defines a docker image which can be used to test the Checkstyle Addons plugin for SonarQube
# This is a SonarQube base image plus Checkstyle and Checkstyle Addons.

ARG SQ_VERSION
FROM sonarqube:${SQ_VERSION}-community

ARG CS_SQPLUGIN_VERSION
ARG CS_ADDONS_VERSION_EXT
ENV BOXES=/etc/boxes/boxes-config

USER root

RUN apt-get -y update \
    && apt-get install -y apt-utils net-tools man boxes vim jq \
    && mkdir /home/sonarqube \
    && chown sonarqube:sonarqube /home/sonarqube

USER sonarqube

COPY --chown=sonarqube:sonarqube sqtest/config/sq-setup.sh /home/sonarqube/
RUN curl -sSLR --output /opt/sonarqube/extensions/plugins/checkstyle-sonar-plugin-${CS_SQPLUGIN_VERSION}.jar \
    https://github.com/checkstyle/sonar-checkstyle/releases/download/${CS_SQPLUGIN_VERSION}/checkstyle-sonar-plugin-${CS_SQPLUGIN_VERSION}.jar
COPY --chown=sonarqube:sonarqube build/libs/sonar-checkstyleaddons-${CS_ADDONS_VERSION_EXT}.jar /opt/sonarqube/extensions/plugins/
RUN chmod 644 /opt/sonarqube/extensions/plugins/sonar-checkstyleaddons-*
