#!/usr/bin/env bash
#
# Checkstyle-Addons - Additional Checkstyle checks
# Copyright (c) 2015-2018, Thomas Jensen and the Checkstyle Addons contributors
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
# program.  If not, see http://www.gnu.org/licenses/.
# __________________________________________________________________________________
#
# Set up a blank SonarQube instance for the integration test (runs inside the SonarQube docker container)
#

declare -r sqBaseUrl=http://localhost:9000
declare -r cookieJar=cookies.txt
declare -r tokenFile=api-token.txt

set -e

if [[ -f ${tokenFile} ]]; then
    echo "API token found in file: ${tokenFile} - We're good to go."
    exit 0
fi
echo "Performing initial setup of Checkstyle Addons integration test container for SonarQube."
echo "If anything fails, the container is corrupt and must be discarded."
echo


function acquire_api_token()
# Returns: $1 - the api token
{
    curl ${curlOpts} --cookie-jar ${cookieJar} -X POST \
        "${sqBaseUrl}/api/authentication/login?login=admin&password=admin"

    local -r xsrfToken=$(cat ${cookieJar} | grep -i XSRF-TOKEN | cut -f 7)
    local -r result=$(curl ${curlOpts} --cookie ${cookieJar} --header "X-XSRF-TOKEN: ${xsrfToken}" \
                    -X POST "${sqBaseUrl}/api/user_tokens/generate?login=admin&name=api" | jq --raw-output .token)
    rm ${cookieJar}
    echo "SonarQube API Token created: ${result}"

    eval "$1=\$result"
}


function create_quality_profile()
# Returns: $1 - key of the newly created quality profile
{
    local -r result=$(curl ${curlOpts} --header "Authorization: ${apiToken}" -X POST \
        "${sqBaseUrl}/api/qualityprofiles/create?language=java&name=Checkstyle%20Addons%20IntTest" |
        jq --raw-output .profile.key)
    echo "Created 'Java' quality profile called 'Checkstyle Addons IntTest' with key: ${result}"

    eval "$1=\$result"
}


function create_custom_rule()
# Args:    $2 - key of the custom rule
#          $3 - key of the rule template
#          $4 - issue type
#          $5 - params, url encoded (optional)
# Returns: $1 - key of the newly created rule
{
    local url="${sqBaseUrl}/api/rules/create"
    url="${url}?markdown_description=Flag%20an%20issue%20for%20the%20Checkstyle%20Addons%20integration%20test"
    url="${url}&custom_key=$2&name=$2&severity=MAJOR&template_key=$3&type=$4"
    if [[ ! -z $5 ]]; then
        url="${url}&params=$5"
    fi

    local -r result=$(curl ${curlOpts} --header "Authorization: ${apiToken}" -X POST ${url} | jq --raw-output .rule.key)
    echo "Created custom rule from template: ${result}"

    eval "$1=\$result"
}


function activate_rule()
# Args: $1 - rule name, for display purposes only
#       $2 - rule key
#       $3 - severity
#       $4 - params, url encoded (optional)
{
    local url="${sqBaseUrl}/api/qualityprofiles/activate_rule"
    url="${url}?key=${profileKey}&rule=$2&severity=$3"
    if [[ ! -z $4 ]]; then
        url="${url}&params=$4"
    fi

    curl ${curlOpts} --header "Authorization: ${apiToken}" -X POST ${url}
    echo "Activated rule: $1"
}


function create_project()
{
    local -r projectKey="com.thomasjensen.checkstyle.addons:checkstyle-addons:sqtest"
    curl ${curlOpts} --header "Authorization: ${apiToken}" --output /dev/null -X POST \
        "${sqBaseUrl}/api/projects/create?name=sqtest&project=${projectKey}"
    echo "Created project 'sqtest'"

    local -r qpNameEnc="Checkstyle%20Addons%20IntTest"
    curl ${curlOpts} --header "Authorization: ${apiToken}" --output /dev/null -X POST \
        "${sqBaseUrl}/api/qualityprofiles/add_project?language=java&project=${projectKey}&qualityProfile=${qpNameEnc}"
    echo "Assigned quality profile 'Checkstyle Addons IntTest' to project 'sqtest'"
}


function write_token_file()
{
    echo ${apiTokenRaw} > ${tokenFile}
}



declare -r curlOpts="--silent --show-error --fail"

# Create API token
declare apiTokenRaw
acquire_api_token apiTokenRaw
declare -r apiToken="Basic $(echo -n ${apiTokenRaw}: | base64)"
echo


# Create quality profile
declare profileKey
create_quality_profile profileKey
echo


# Create and activate rules
declare ruleKey

create_custom_rule ruleKey LocalVariableNameTester \
    checkstyle:com.puppycrawl.tools.checkstyle.checks.naming.LocalVariableNameCheck \
    CODE_SMELL
activate_rule ${ruleKey} ${ruleKey} MINOR

create_custom_rule ruleKey LocationReferenceTester \
    checkstyle:com.thomasjensen.checkstyle.addons.checks.misc.LocationReferenceCheck \
    BUG \
    "methodCalls%3DSystem.out.println"
activate_rule ${ruleKey} ${ruleKey} MAJOR

create_custom_rule ruleKey RegexpOnStringTester \
    checkstyle:com.thomasjensen.checkstyle.addons.checks.regexp.RegexpOnStringCheck \
    BUG \
    "regexp%3Dflag%20this"
activate_rule ${ruleKey} ${ruleKey} CRITICAL

create_custom_rule ruleKey RegexpOnFilenameOrgTester \
    checkstyle:com.thomasjensen.checkstyle.addons.checks.regexp.RegexpOnFilenameOrgCheck \
    BUG \
    "regexp%3DIllegal%5C.java%24%3Bsimple%3Dtrue"
activate_rule ${ruleKey} ${ruleKey} MAJOR

activate_rule IllegalMethodCall \
    checkstyle:com.thomasjensen.checkstyle.addons.checks.coding.IllegalMethodCallCheck \
    CRITICAL \
    "illegalMethodNames%3DforName,%20newInstance"

activate_rule LostInstance \
    checkstyle:com.thomasjensen.checkstyle.addons.checks.coding.LostInstanceCheck \
    CRITICAL

activate_rule ModuleDirectoryLayout \
    checkstyle:com.thomasjensen.checkstyle.addons.checks.misc.ModuleDirectoryLayoutCheck \
    CRITICAL \
    "configFile%3Dsqtest%2Fconfig%2Fdirectories.json"

activate_rule PropertyCatalog \
    checkstyle:com.thomasjensen.checkstyle.addons.checks.misc.PropertyCatalogCheck \
    MAJOR \
    "selection%3DPropertyCatalog%3BpropertyFile%3Dsqtest%2Fsrc%2Fmain%2Fresources%2F%7B1%7D.properties"

activate_rule FileTabCharacter \
    checkstyle:com.puppycrawl.tools.checkstyle.checks.whitespace.FileTabCharacterCheck \
    MINOR
echo


# Provision project and assign quality profile
create_project
echo

# Finally, store API token in file, indicating that this container is properly set up.
write_token_file
echo "Container setup completed successfully."


#EOF
