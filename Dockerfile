# Defines a docker image which can be used to test the Checkstyle Addons plugin for SonarQube
# This is a SonarQube base image plus Checkstyle and Checkstyle Addons.

FROM sonarqube:7.4-community

ARG CS_SQPLUGIN_VERSION=4.17
ENV BOXES=/etc/boxes/boxes-config

USER root

RUN apt-get -y update \
    && apt-get install -y apt-utils net-tools man boxes vim jq \
    && mkdir /home/sonarqube \
    && chown sonarqube:sonarqube /home/sonarqube

USER sonarqube

RUN curl -sSLR --output /opt/sonarqube/extensions/plugins/checkstyle-sonar-plugin-$CS_SQPLUGIN_VERSION.jar \
    https://github.com/checkstyle/sonar-checkstyle/releases/download/$CS_SQPLUGIN_VERSION/checkstyle-sonar-plugin-$CS_SQPLUGIN_VERSION.jar
COPY --chown=sonarqube:sonarqube build/libs/sonar-checkstyleaddons-* /opt/sonarqube/extensions/plugins/
RUN chmod 644 /opt/sonarqube/extensions/plugins/sonar-checkstyleaddons-*
