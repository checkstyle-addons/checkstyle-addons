# Download &amp; Run

Here's how to use {{ site.name }} with your standard software.

{{ site.name }} uses [semantic versioning](http://semver.org/), so you can always use the latest within a major version, but you should try things out before moving to a higher major version.

{% comment %} ======================================================================================= {% endcomment %}
<a name="run-gradle" class="csa-offset-anchor"/>

## Gradle

{{ site.name }} is available on [jcenter](https://bintray.com/checkstyle-addons/checkstyle-addons/checkstyle-addons/view), so you can just use it in your Gradle build. In Gradle, {{ site.name }} runs as part of the [Gradle Checkstyle Plugin](https://gradle.org/docs/current/userguide/checkstyle_plugin.html). The classpath is extended to include {{ site.name }}. Note that the Checkstyle tool version must be specified.

```groovy
plugins {
    id 'checkstyle'
}
repositories {
    jcenter()
}

dependencies {
    checkstyle group: 'com.thomasjensen.checkstyle.addons', name: 'checkstyle-addons', version: '{{ site.latest_version }}'
}

checkstyle {
    configFile file('your-checkstyle.xml')
    toolVersion '{{ site.latest_version_checkstyle }}'   // important
}
```


{% comment %} ======================================================================================= {% endcomment %}
<a name="run-maven" class="csa-offset-anchor"/>

## Maven

{{ site.name }} is available on [jcenter](https://bintray.com/checkstyle-addons/checkstyle-addons/checkstyle-addons/view). In order to use jcenter in your Maven build, you must add it to your Maven *settings.xml*. jcenter offers a superset of Maven Central. Bintray provides a [configuration example](https://github.com/bintray/bintray-examples/blob/master/maven-example/settings.xml) of how to configure your *settings.xml* to use jcenter.

In Maven, {{ site.name }} runs as part of the [Maven Checkstyle Plugin](https://maven.apache.org/plugins/maven-checkstyle-plugin/). The classpath is extended to include {{ site.name }}. Note that the explicit dependency on Checkstyle itself must also be specified in order to set the correct Checkstyle version.

{% highlight xml %}
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
{% endhighlight %}

{% comment %} ======================================================================================= {% endcomment %}
<a name="run-ant" class="csa-offset-anchor"/>

## Ant

In Ant, we must add {{ site.name }} to the classpath of the [Checkstyle Ant Task](http://checkstyle.sourceforge.net/anttask.html). The Ant task is part of the standard Checkstyle distribution. We need **at least Checkstyle 6.2**, better yet, exactly version {{ site.latest_version_checkstyle }}. And of course the {{ site.name }} jar file:

<p><a href="https://github.com/{{ site.github }}/releases/download/v{{ site.latest_version }}/checkstyle-addons-{{ site.latest_version }}.jar" class="btn btn-primary">Download JAR</a></p>

Then, in our Ant *build.xml*, we declare the Checkstyle task as follows:

```xml
<taskdef resource="checkstyletask.properties">
    <classpath>
        <pathelement location="lib/checkstyle-{{ site.latest_version_checkstyle }}-all.jar"/>
        <pathelement location="lib/checkstyle-addons-{{ site.latest_version }}.jar"/>
    </classpath>
</taskdef>
```

The *checkstyletask.properties* is read from checkstyle-{{ site.latest_version_checkstyle }}-all.jar. The call to the Checkstyle task is standard Ant:

```xml
<checkstyle config="/path/to/your-checkstyle.xml">
    <fileset dir="src" includes="**/*.java"/>

    <!-- Where in your build folder to store the cache file used during build for faster analysis -->
    <property key="checkstyle.cache.file" file="target/cachefile"/>
</checkstyle>
```

For more information on Checkstyle Ant task configuration, please refer to its [website](http://checkstyle.sourceforge.net/anttask.html).


{% comment %} ======================================================================================= {% endcomment %}
<a name="run-intellij" class="csa-offset-anchor"/>

## IntelliJ IDEA / Android Studio

In IntelliJ IDEA or Android Studio, {{ site.name }} runs as a third-party addon to the [Checkstyle-IDEA plugin](https://plugins.jetbrains.com/plugin/1065). So first make sure you've got Checkstyle-IDEA. If not, install via *File &rarr; Settings... &rarr; Plugins &rarr; Browse Repostories*.

<p><a href="https://github.com/{{ site.github }}/releases/download/v{{ site.latest_version }}/checkstyle-addons-{{ site.latest_version }}.jar" class="btn btn-primary">Download Plugin</a></p>

 After that, you can add {{ site.name }} to the third-party checks:

![{{ site.name }} in IntelliJ IDEA](images/run-intellij.png)

Checkstyle-IDEA does not offer a visual editor, so you'll have to activate the {{ site.name }} checks by modifying your *checkstyle.xml* directly.


{% comment %} ======================================================================================= {% endcomment %}
<a name="run-eclipse" class="csa-offset-anchor"/>

## Eclipse

In Eclipse, {{ site.name }} runs as an addon to [Eclipse-CS](http://eclipse-cs.sourceforge.net/). So first make sure you've got Eclipse-CS [installed](http://eclipse-cs.sourceforge.net/#!/install) at **version 6.2 or higher**. Then:

<p><a href="https://github.com/{{ site.github }}/releases/download/v{{ site.latest_version }}/checkstyle-addons-eclipse-{{ site.latest_version }}.jar" class="btn btn-primary">Download Eclipse Plugin</a></p>

**Installation:** Drop the downloaded plugin JAR into the *dropins* folder of your Eclipse installation and restart Eclipse.

In order to verify that {{ site.name }} was installed correctly, open the Checkstyle configuration dialog and you should see a new module category named '{{ site.name }}'. {{ site.name }} fully supports the visual Checkstyle configuration editor of Eclipse-CS:

![{{ site.name }} in Eclipse](images/run-eclipse.png)


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

<p><a href="https://github.com/{{ site.github }}/releases/download/v{{ site.latest_version }}/sonar-checkstyleaddons-{{ site.latest_version }}.jar" class="btn btn-primary">Download SonarQube Plugin</a></p>

**Installation:** Drop the downloaded plugin into the *extensions/plugins* folder of your SonarQube installation (where all the other plugins are). Double-check the version of the SonarQube Checkstyle plugin according to the above table. Upgrade that if necessary. Restart SonarQube.


{% comment %} ======================================================================================= {% endcomment %}
<a name="run-command-line" class="csa-offset-anchor"/>

## Command Line

Checkstyle can also be run [from the command line](http://checkstyle.sourceforge.net/cmdline.html). In that case, you can add {{ site.name }} to the classpath.
Make sure to use at least Java&nbsp;7, and at least Checkstyle 6.2. The code you're analyzing can be anything up to Java&nbsp;8.

<p><a href="https://github.com/{{ site.github }}/releases/download/v{{ site.latest_version }}/checkstyle-addons-{{ site.latest_version }}.jar" class="btn btn-primary">Download JAR</a></p>

Now you can run Checkstyle with {{ site.name }} like this:

```
java -cp checkstyle-addons-{{ site.latest_version }}.jar;checkstyle-{{ site.latest_version_checkstyle }}-all.jar com.puppycrawl.tools.checkstyle.Main -c your-checkstyle.xml src
```

The above assumes that your sources are in a subdirectory *src*, and that the required JARs and your configuration file are in the current directory.
