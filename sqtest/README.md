# SonarQube Integration Test

This module implements the SonarQube integration test, which currently does this:

- Build a docker image containing SonarQube, Checkstyle Plugin and Checkstyle Addons, plus a quality profile
  which uses the Checkstyle Addons checks.
- Run the docker image
- Execute a SonarQube analysis against it of the code in this module
- Evaluate the results

## Prerequisites

- `.\gradlew clean build` - There must be only exactly one version of Checkstyle Addons in build/libs.
- [Docker](https://www.docker.com/) installed and working with `docker` command, e.g. `docker ps`.

## How to

From the root project directory, run

    .\gradlew :sqtest:checkstyle

to execute the reference analysis. This show the issues placed in the dummy code which are found by Gradle Checsktyle.

Then run

    .\gradlew :sqtest:integrationTest

in order to execute the integration test.


## Configuration

The following component versions:

- Version of the SonarQube platform
- Version of the Checkstyle Plugin for SonarQube
- Dependency config specific postfix of Checkstyle Addons SonarQube plugin to use (empty String for current)

are set in the *sqtest* module's
[build.gradle](https://github.com/checkstyle-addons/checkstyle-addons/blob/ffe91f2c096e2832713dc579d4868922162a122e/sqtest/build.gradle#L50-L51)
file. Checkstyle Addons is always used in its current form as present in the workspace.
