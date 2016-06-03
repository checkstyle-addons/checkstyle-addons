# Download Guide

The [downloads section](https://github.com/{{ site.github }}/releases/tag/v{{ page.csa_version }}) of a {{ site.name }} release
contains quite a number of files. GitHub does not support setting descriptions for them, so we put the
descriptions here. The most important factor is the Checkstyle version that you use (&ge; 6.2 or &lt; 6.2).

<div class="alert alert-info">
  <p>The <a href="{{ site.baseurl }}/run.html"><i>Get Started</i> page</a> has better descriptions
  on how to set things up. Really!</p>
</div>

### Checkstyle 6.2 and up (at least Java&nbsp;7)

{% if page.show_fatjar %}
 - [checkstyle-addons-{{ page.csa_version }}-all.jar](https://github.com/{{ site.github }}/releases/download/v{{ page.csa_version }}/checkstyle-addons-{{ page.csa_version }}-all.jar) -
   A fatjar containing {{ site.name }} including all its dependencies; useful for IntelliJ IDEA, Android Studio, Ant,
   and command line.
 - [checkstyle-addons-{{ page.csa_version }}.jar](https://github.com/{{ site.github }}/releases/download/v{{ page.csa_version }}/checkstyle-addons-{{ page.csa_version }}.jar) -
   Just {{ site.name }}, excluding external dependencies. Normally, this file is pulled via Maven Central or Bintray
   jcenter, so you don't need to download it here. {% else %}
 - [checkstyle-addons-{{ page.csa_version }}.jar](https://github.com/{{ site.github }}/releases/download/v{{ page.csa_version }}/checkstyle-addons-{{ page.csa_version }}.jar) -
   The main {{ site.name }} Jar. Gradle, Maven, and Ivy will download this file from Maven Central or Bintray jcenter,
   but this download is also useful for IntelliJ IDEA, Android Studio, Ant, and command line.{% endif %}
 - [checkstyle-addons-{{ page.csa_version }}-javadoc.jar](https://github.com/{{ site.github }}/releases/download/v{{ page.csa_version }}/checkstyle-addons-{{ page.csa_version }}-javadoc.jar) -
   The Javadoc in Java&nbsp;7 format.
 - [checkstyle-addons-eclipse-{{ page.csa_version }}.jar](https://github.com/{{ site.github }}/releases/download/v{{ page.csa_version }}/checkstyle-addons-eclipse-{{ page.csa_version }}.jar) -
   The {{ site.name }} plugin for Eclipse (requires Eclipse-CS 6.2.0 or newer).
 - [sonar-checkstyleaddons-{{ page.csa_version }}.jar](https://github.com/{{ site.github }}/releases/download/v{{ page.csa_version }}/sonar-checkstyleaddons-{{ page.csa_version }}.jar) -
   The SonarQube plugin (requires SonarQube Checkstyle Plugin 2.3 or newer - [download](https://github.com/checkstyle-addons/sonar-checkstyle/releases)).

### Checkstyle 6.0 - 6.1.1 (Java&nbsp;6 support)

{% if page.show_fatjar %}
 - [checkstyle-addons-java6-{{ page.csa_version }}-all.jar](https://github.com/{{ site.github }}/releases/download/v{{ page.csa_version }}/checkstyle-addons-java6-{{ page.csa_version }}-all.jar) -
   A fatjar containing {{ site.name }} including all its dependencies; useful for IntelliJ IDEA, Android Studio, Ant,
   and command line.
 - [checkstyle-addons-java6-{{ page.csa_version }}.jar](https://github.com/{{ site.github }}/releases/download/v{{ page.csa_version }}/checkstyle-addons-java6-{{ page.csa_version }}.jar) -
   Just {{ site.name }}, excluding external dependencies. Normally, this file is pulled via Maven Central or Bintray
   jcenter, so you don't need to download it here. {% else %}
 - [checkstyle-addons-java6-{{ page.csa_version }}.jar](https://github.com/{{ site.github }}/releases/download/v{{ page.csa_version }}/checkstyle-addons-java6-{{ page.csa_version }}.jar) -
   The main {{ site.name }} Jar. Gradle, Maven, and Ivy will download this file from Maven Central or Bintray jcenter,
   but this download is also useful for IntelliJ IDEA, Android Studio, Ant, and command line.{% endif %}
 - [checkstyle-addons-java6-{{ page.csa_version }}-javadoc.jar](https://github.com/{{ site.github }}/releases/download/v{{ page.csa_version }}/checkstyle-addons-java6-{{ page.csa_version }}-javadoc.jar) -
   The Javadoc in Java&nbsp;6 format.
 - [checkstyle-addons-java6-eclipse-{{ page.csa_version }}.jar](https://github.com/{{ site.github }}/releases/download/v{{ page.csa_version }}/checkstyle-addons-java6-eclipse-{{ page.csa_version }}.jar) -
   The {{ site.name }} plugin for Eclipse (requires Eclipse-CS 6.1.x).
 - [sonar-checkstyleaddons-{{ page.csa_version }}-csp2.2.jar](https://github.com/{{ site.github }}/releases/download/v{{ page.csa_version }}/sonar-checkstyleaddons-{{ page.csa_version }}-csp2.2.jar) -
   The SonarQube plugin (requires SonarQube Checkstyle Plugin 2.2 - [download](https://github.com/checkstyle-addons/sonar-checkstyle/releases/tag/v2.2)).

### Source Code

 - [Source code (zip)](https://github.com/{{ site.github }}/archive/v{{ page.csa_version }}.zip) -
   the {{ site.name }} v{{ page.csa_version }} source code
 - [Source code (tar.gz)](https://github.com/{{ site.github }}/archive/v{{ page.csa_version }}.tar.gz) -
   same source code as the ZIP, but packed with `tar`
