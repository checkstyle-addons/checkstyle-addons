#
# Checkstyle-Addons - Additional Checkstyle checks
# Copyright (c) 2015-2022, the Checkstyle Addons contributors
#
# This program is free software: you can redistribute it and/or modify it under the
# terms of the GNU General Public License, version 3, as published by the Free
# Software Foundation.
#
# This program is distributed in the hope that it will be useful, but WITHOUT ANY
# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
# PARTICULAR PURPOSE. See the GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along with this
# program.  If not, see <http://www.gnu.org/licenses/>.
# ____________________________________________________________________________________________________________________
#
# Defines a docker image which can be used to test the Checkstyle Addons plugin for SonarQube
# This is a SonarQube base image plus Checkstyle and Checkstyle Addons.

ARG SQ_VERSION
FROM sonarqube:${SQ_VERSION}-community

ARG CS_SQPLUGIN_VERSION
ARG CS_ADDONS_VERSION_EXT

USER root

RUN apk add curl jq net-tools vim

USER sonarqube

COPY --chown=sonarqube:sonarqube sqtest/config/sq-setup.sh /home/sonarqube/
RUN curl -sSLR --output ${SQ_EXTENSIONS_DIR}/plugins/checkstyle-sonar-plugin-${CS_SQPLUGIN_VERSION}.jar \
    https://github.com/checkstyle/sonar-checkstyle/releases/download/${CS_SQPLUGIN_VERSION}/checkstyle-sonar-plugin-${CS_SQPLUGIN_VERSION}.jar
COPY --chown=sonarqube:sonarqube build/libs/checkstyle-addons-${CS_ADDONS_VERSION_EXT}-sonar.jar ${SQ_EXTENSIONS_DIR}/plugins/
RUN chmod 644 ${SQ_EXTENSIONS_DIR}/plugins/checkstyle-addons-*
