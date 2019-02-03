# SonarQube Integration Test

This module implements the SonarQube integration test, which currently does this:

- Build a docker image containing SonarQube, Checkstyle Plugin and Checkstyle Addons, plus a quality profile
  which uses the Checkstyle Addons checks.
- Run the docker image
- Execute a SonarQube analysis against it of the code in this module
- Evaluate the results
