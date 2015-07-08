#!/usr/bin/env bash
#
# Script to build artifacts for all defined dependency configurations.
# This is merely a little support script for the author; it is not part of Checkstyle Addons itself.
# Run from the project root directory (where the script is located).
#
# @author Thomas Jensen
#
set -e

declare -a depConfigs
declare -i i=0
IFS=$'\n'
for depConfig in $(/bin/ls project/dependencyConfigs/*.properties | sed -e "s/project\/dependencyConfigs\/\([^p]*\).properties$/\1/"); do
    depConfigs[$i]=${depConfig}
    ((i++))
done
IFS=$' '

echo
echo "These dependency configurations will be built:"
for depConfig in "${depConfigs[@]}"; do
    echo "  - ${depConfig}"
done
echo

# set GRADLE_USER_HOME, JAVA_HOME, and PATH
. _support/gradlevars.sh

declare artifactTargetDir='_support/temp'
rm -rfv ${artifactTargetDir}
mkdir -pv ${artifactTargetDir}

for depConfig in "${depConfigs[@]}"; do
    echo
    echo
    echo ------------------------ Building dependency configuration: ${depConfig} ------------------------
    echo
    echo
    ./gradlew -Pcheckstyleaddons_deps=${depConfig} clean build
    cp -aiv build/libs/* ${artifactTargetDir}
done

echo
echo "SUCCESS"
echo
read -p "Press any key ... " -n1 -s
