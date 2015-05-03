# Running {{ site.name }}

Here's how to use {{ site.name }} with your standard software.

{{ site.name }} uses [semantic versioning](http://semver.org/), so you can always use the latest within a major version, but you should try things out before moving to a higher major version.

<a name="run-gradle" class="csa-offset-anchor"/>

## Gradle

<a name="run-maven" class="csa-offset-anchor"/>

## Maven

{{ site.name }} is available on [jcenter](https://bintray.com/checkstyle-addons/checkstyle-addons/checkstyle-addons/view). In order to use jcenter in your Maven build, you must add it to your Maven *settings.xml*. jcenter offers a superset of Maven Central. Bintray provides a [configuration example](https://github.com/bintray/bintray-examples/blob/master/maven-example/settings.xml) of how to configure your *settings.xml* to use jcenter.

In Maven, {{ site.name }} runs as part of the [Maven Checkstyle Plugin](https://maven.apache.org/plugins/maven-checkstyle-plugin/). The classpath is simply extended to include {{ site.name }}. Note that the explicit dependency on Checkstyle itself must also be specified in order to set the correct Checkstyle version.

```xml
<project>
  <!-- snip -->
  <properties>
    <!-- path to your checkstyle.xml, relative to the project root -->
    <checkstyle.config.location>your-checkstyle.xml</checkstyle.config.location>
  </properties>
  <!-- snip -->
  <plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-checkstyle-plugin</artifactId>
    <version>2.15</version>
    <dependencies>
      <dependency>
        <groupId>com.puppycrawl.tools</groupId>
        <artifactId>checkstyle</artifactId>
        <version>{{ site.latest_version_checkstyle }}</version>
      </dependency>
      <dependency>
        <groupId>com.thomasjensen.checkstyle.addons</groupId>
        <artifactId>checkstyle-addons</artifactId>
        <version>{{ site.latest_version }}</version>
      </dependency>
    </dependencies>
  </plugin>

</project>
```


<a name="run-ant" class="csa-offset-anchor"/>

## Ant

<a name="run-intellij" class="csa-offset-anchor"/>

## IntelliJ IDEA / Android Studio

In IntelliJ IDEA or Android Studio, {{ site.name }} runs as a third-party addon to the [Checkstyle-IDEA plugin](https://plugins.jetbrains.com/plugin/1065). So first make sure you've got Checkstyle-IDEA. If not, install via *File &rarr; Settings... &rarr; Plugins &rarr; Browse Repostories*. After that, you can add {{ site.name }} to the third-party checks:

![{{ site.name }} in IntelliJ IDEA](images/run-intellij.png)

Checkstyle-IDEA does not offer a visual editor, so you'll have to activate the {{ site.name }} checks by modifying your *checkstyle.xml* directly.


<a name="run-eclipse" class="csa-offset-anchor"/>

## Eclipse

<a name="run-sonarqube" class="csa-offset-anchor"/>

## SonarQube

<a name="run-command-line" class="csa-offset-anchor"/>

## Command Line
