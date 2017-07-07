# Download Guide

The [downloads section](https://github.com/{{ site.github }}/releases/tag/v{{ page.csa_version }}) of a {{ site.name }} release
contains quite a number of files. GitHub does not support setting descriptions for them, so we put the
descriptions here. The most important factor is the *Checkstyle version* that you use ({%
  if page.show_java6 == true %}{% if page.show_java8 == true %}&ge;&nbsp;7.0, &lt;&nbsp;6.2, or in between{%
  else %}&ge;&nbsp;6.2, or &lt;&nbsp;6.2{% endif %}{%
  else %}&ge;&nbsp;7.0, or 6.16.1 - 6.19{% endif %}).

<div class="alert alert-info">
  <p>Find detailed descriptions on how to set things up on the <a href="{{ site.baseurl }}/run.html"><i>Get Started</i> page</a>.</p>
</div>

{% if page.show_java8 %}
### Checkstyle 7.0 and up (at least Java&nbsp;8)
{% if page.show_fatjar %}
 - [checkstyle-addons-{{ page.csa_version }}-all.jar](https://github.com/{{ site.github }}/releases/download/v{{ page.csa_version }}/checkstyle-addons-{{ page.csa_version }}-all.jar) —
   A fatjar containing {{ site.name }} including all its dependencies; useful for IntelliJ IDEA, Android Studio, Ant,
   and command line.
 - [checkstyle-addons-{{ page.csa_version }}.jar](https://github.com/{{ site.github }}/releases/download/v{{ page.csa_version }}/checkstyle-addons-{{ page.csa_version }}.jar) —
   Just {{ site.name }}, excluding external dependencies. Normally, this file is pulled via Maven Central or Bintray
   jcenter, so you don't need to download it here.{% else %}
 - [checkstyle-addons-{{ page.csa_version }}.jar](https://github.com/{{ site.github }}/releases/download/v{{ page.csa_version }}/checkstyle-addons-{{ page.csa_version }}.jar) —
   The main {{ site.name }} Jar. Gradle, Maven, and Ivy will download this file from Maven Central or Bintray jcenter,
   but this download is also useful for IntelliJ IDEA, Android Studio, Ant, and command line.{% endif %}
 - [checkstyle-addons-{{ page.csa_version }}-javadoc.jar](https://github.com/{{ site.github }}/releases/download/v{{ page.csa_version }}/checkstyle-addons-{{ page.csa_version }}-javadoc.jar) —
   The Javadoc in Java&nbsp;8 format.
 - [checkstyle-addons-eclipse-{{ page.csa_version }}.jar](https://github.com/{{ site.github }}/releases/download/v{{ page.csa_version }}/checkstyle-addons-eclipse-{{ page.csa_version }}.jar) —
   The {{ site.name }} plugin for Eclipse (requires Eclipse-CS 7.2.0 or newer).
 - [sonar-checkstyleaddons-{{ page.csa_version }}.jar](https://github.com/{{ site.github }}/releases/download/v{{ page.csa_version }}/sonar-checkstyleaddons-{{ page.csa_version }}.jar) —
   The SonarQube plugin (requires SonarQube Checkstyle Plugin {% if page.show_java6 %}2.3{% else %}3.1{% endif %} or newer - [download](https://github.com/checkstyle/sonar-checkstyle/releases)).
{% endif %}

### Checkstyle {{ page.earliest_java7 | default: "6.2" }} {% if page.show_java8 %}- 6.19 (Java&nbsp;7 support){% else %}and up (at least Java&nbsp;7){% endif %}

{% if page.show_fatjar %}
 - [checkstyle-addons{% if page.show_java8 %}-java7{% endif %}-{{ page.csa_version }}-all.jar](https://github.com/{{ site.github }}/releases/download/v{{ page.csa_version }}/checkstyle-addons{% if page.show_java8 %}-java7{% endif %}-{{ page.csa_version }}-all.jar) —
   A fatjar containing {{ site.name }} including all its dependencies; useful for IntelliJ IDEA, Android Studio, Ant,
   and command line.
 - [checkstyle-addons{% if page.show_java8 %}-java7{% endif %}-{{ page.csa_version }}.jar](https://github.com/{{ site.github }}/releases/download/v{{ page.csa_version }}/checkstyle-addons{% if page.show_java8 %}-java7{% endif %}-{{ page.csa_version }}.jar) —
   Just {{ site.name }}, excluding external dependencies. Normally, this file is pulled via Maven Central or Bintray
   jcenter, so you don't need to download it here. {% else %}
 - [checkstyle-addons{% if page.show_java8 %}-java7{% endif %}-{{ page.csa_version }}.jar](https://github.com/{{ site.github }}/releases/download/v{{ page.csa_version }}/checkstyle-addons{% if page.show_java8 %}-java7{% endif %}-{{ page.csa_version }}.jar) —
   The main {{ site.name }} Jar. Gradle, Maven, and Ivy will download this file from Maven Central or Bintray jcenter,
   but this download is also useful for IntelliJ IDEA, Android Studio, Ant, and command line.{% endif %}
 - [checkstyle-addons{% if page.show_java8 %}-java7{% endif %}-{{ page.csa_version }}-javadoc.jar](https://github.com/{{ site.github }}/releases/download/v{{ page.csa_version }}/checkstyle-addons{% if page.show_java8 %}-java7{% endif %}-{{ page.csa_version }}-javadoc.jar) —
   The Javadoc in Java&nbsp;7 format.
 - [checkstyle-addons{% if page.show_java8 %}-java7{% endif %}-eclipse-{{ page.csa_version }}.jar](https://github.com/{{ site.github }}/releases/download/v{{ page.csa_version }}/checkstyle-addons{% if page.show_java8 %}-java7{% endif %}-eclipse-{{ page.csa_version }}.jar) —
   The {{ site.name }} plugin for Eclipse (requires Eclipse-CS {% if page.show_java6 %}6.2.0 or newer{% else %}6.16.0 or 6.19.x{% endif %}).{% if page.show_java6 %}
 - [sonar-checkstyleaddons-{{ page.csa_version }}{% if page.show_java8 %}-csp2.3{% endif %}.jar](https://github.com/{{ site.github }}/releases/download/v{{ page.csa_version }}/sonar-checkstyleaddons-{{ page.csa_version }}{% if page.show_java8 %}-csp2.3{% endif %}.jar) —
   The SonarQube plugin (requires SonarQube Checkstyle Plugin 2.3 or newer - [download](https://github.com/checkstyle-addons/sonar-checkstyle/releases)).
{% endif %}

{% if page.show_java6 %}
### Checkstyle 6.0 - 6.1.1 (Java&nbsp;6 support)

{% if page.show_fatjar %}
 - [checkstyle-addons-java6-{{ page.csa_version }}-all.jar](https://github.com/{{ site.github }}/releases/download/v{{ page.csa_version }}/checkstyle-addons-java6-{{ page.csa_version }}-all.jar) —
   A fatjar containing {{ site.name }} including all its dependencies; useful for IntelliJ IDEA, Android Studio, Ant,
   and command line.
 - [checkstyle-addons-java6-{{ page.csa_version }}.jar](https://github.com/{{ site.github }}/releases/download/v{{ page.csa_version }}/checkstyle-addons-java6-{{ page.csa_version }}.jar) —
   Just {{ site.name }}, excluding external dependencies. Normally, this file is pulled via Maven Central or Bintray
   jcenter, so you don't need to download it here. {% else %}
 - [checkstyle-addons-java6-{{ page.csa_version }}.jar](https://github.com/{{ site.github }}/releases/download/v{{ page.csa_version }}/checkstyle-addons-java6-{{ page.csa_version }}.jar) —
   The main {{ site.name }} Jar. Gradle, Maven, and Ivy will download this file from Maven Central or Bintray jcenter,
   but this download is also useful for IntelliJ IDEA, Android Studio, Ant, and command line.{% endif %}
 - [checkstyle-addons-java6-{{ page.csa_version }}-javadoc.jar](https://github.com/{{ site.github }}/releases/download/v{{ page.csa_version }}/checkstyle-addons-java6-{{ page.csa_version }}-javadoc.jar) —
   The Javadoc in Java&nbsp;6 format.
 - [checkstyle-addons-java6-eclipse-{{ page.csa_version }}.jar](https://github.com/{{ site.github }}/releases/download/v{{ page.csa_version }}/checkstyle-addons-java6-eclipse-{{ page.csa_version }}.jar) —
   The {{ site.name }} plugin for Eclipse (requires Eclipse-CS 6.1.x).
 - [sonar-checkstyleaddons-{{ page.csa_version }}-csp2.2.jar](https://github.com/{{ site.github }}/releases/download/v{{ page.csa_version }}/sonar-checkstyleaddons-{{ page.csa_version }}-csp2.2.jar) —
   The SonarQube plugin (requires SonarQube Checkstyle Plugin 2.2 - [download](https://github.com/checkstyle-addons/sonar-checkstyle/releases/tag/v2.2)).
{% endif %}

### Source Code

 - [Source code (zip)](https://github.com/{{ site.github }}/archive/v{{ page.csa_version }}.zip) —
   the {{ site.name }} v{{ page.csa_version }} source code
 - [Source code (tar.gz)](https://github.com/{{ site.github }}/archive/v{{ page.csa_version }}.tar.gz) —
   same source code as the ZIP, but packed with `tar`
