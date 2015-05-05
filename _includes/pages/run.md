# Running {{ site.name }}

Here's how to use {{ site.name }} with your standard software.

{{ site.name }} uses [semantic versioning](http://semver.org/), so you can always use the latest within a major version, but you should try things out before moving to a higher major version.

{% comment %} ======================================================================================= {% endcomment %}
<a name="run-gradle" class="csa-offset-anchor"/>

## Gradle

{% comment %} ======================================================================================= {% endcomment %}
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

{% comment %} ======================================================================================= {% endcomment %}
<a name="run-ant" class="csa-offset-anchor"/>

## Ant

{% comment %} ======================================================================================= {% endcomment %}
<a name="run-intellij" class="csa-offset-anchor"/>

## IntelliJ IDEA / Android Studio

In IntelliJ IDEA or Android Studio, {{ site.name }} runs as a third-party addon to the [Checkstyle-IDEA plugin](https://plugins.jetbrains.com/plugin/1065). So first make sure you've got Checkstyle-IDEA. If not, install via *File &rarr; Settings... &rarr; Plugins &rarr; Browse Repostories*.

<p><a href="https://github.com/{{ site.github }}/releases/download/v{{ site.latest_version }}/checkstyle-addons-{{ site.latest_version }}.jar" class="btn btn-primary">Download Checks</a></p>

 After that, you can add {{ site.name }} to the third-party checks:

![{{ site.name }} in IntelliJ IDEA](images/run-intellij.png)

Checkstyle-IDEA does not offer a visual editor, so you'll have to activate the {{ site.name }} checks by modifying your *checkstyle.xml* directly.


{% comment %} ======================================================================================= {% endcomment %}
<a name="run-eclipse" class="csa-offset-anchor"/>

## Eclipse

<p><a href="https://github.com/{{ site.github }}/releases/download/v{{ site.latest_version }}/checkstyle-addons-eclipse-{{ site.latest_version }}.jar" class="btn btn-primary">Download Plugin</a></p>



{% comment %} ======================================================================================= {% endcomment %}
<a name="run-sonarqube" class="csa-offset-anchor"/>

## SonarQube

In [SonarQube](http://www.sonarqube.org/), {{ site.name }} runs as an extension of the SonarQube Checkstyle Plugin. Make sure your SonarQube installation runs on the versions shown in the highlighted column:

<table class="table table-striped csa-version-table" style="width:auto;">
  <tbody>
    <tr>
      <td><a href="http://docs.sonarqube.org/display/SONAR/Checkstyle+Plugin" target="_blank">SonarQube Checkstyle Plugin</a></td>
      <td>2.0</td>
      <td>2.1</td>
      <td>2.1.1</td>
      <td>2.2</td>
      <td class="info">2.3</td>
    </tr>
    <tr>
      <td><a href="http://docs.sonarqube.org/display/SONAR/Java+Plugin" target="_blank">SonarQube Java Plugin</a></td>
      <td>2.0+</td>
      <td>2.2+</td>
      <td>2.2+</td>
      <td>2.2+</td>
      <td class="info">2.2+</td>
    </tr>
    <tr>
      <td><a href="http://docs.sonarqube.org/display/SONAR/Upgrading#Upgrading-ReleaseUpgradeNotes" target="_blank">SonarQube Platform</a></td>
      <td>3.6 - 4.1.2</td>
      <td colspan="3" style="text-align:center;">4.2 - 4.4</td>
      <td class="info">4.5.2+</td>
    </tr>
    <tr>
      <td><a href="http://checkstyle.sourceforge.net/releasenotes.html" target="_blank">Checkstyle</a></td>
      <td>5.6</td>
      <td>5.6</td>
      <td>5.6</td>
      <td>6.1</td>
      <td class="info">6.4.1</td>
    </tr>
    <tr>
      <td>Java (analysis process)</td>
      <td>6+</td>
      <td>6+</td>
      <td>6+</td>
      <td>6+</td>
      <td class="info">7+</td>
    </tr>
    <tr>
      <td>Java (code analyzed)</td>
      <td>&lt;= 7</td>
      <td>&lt;= 7</td>
      <td>&lt;= 7</td>
      <td>&lt;= 8</td>
      <td class="info">&lt;= 8</td>
    </tr>
  </tbody>
</table>

The Checkstyle version is directly determined by the version of the SonarQube Checkstyle plugin, so you don't need to check that explicitly. SonarQube 3.6 is the earliest version on which {{ site.name }} could possibly run, even if you built from source.

The rules provided by {{ site.name }} are all tagged with `checkstyle-addons`, which is useful to filter them from the overall list of Checkstyle rules.

<p><a href="https://github.com/{{ site.github }}/releases/download/v{{ site.latest_version }}/sonar-checkstyleaddons-{{ site.latest_version }}.jar" class="btn btn-primary">Download Plugin</a></p>

**Installation:** Drop the downloaded plugin into the *extensions/plugins* folder of your SonarQube installation (where all the other plugins are). Double-check the version of the SonarQube Checkstyle plugin according to the above table. Upgrade that if necessary. Restart SonarQube.


{% comment %} ======================================================================================= {% endcomment %}
<a name="run-command-line" class="csa-offset-anchor"/>

## Command Line
