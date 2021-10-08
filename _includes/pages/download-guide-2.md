{% comment %}
    This version of the Download Guide is used starting with Checkstyle 8.21 (Checkstyle Addons v6.0.0).
    For earlier versions, use the download-guide.html (without postfix).
{% endcomment %}
# Download Guide

The [downloads section](https://github.com/{{ site.github }}/releases/tag/v{{ page.csa_version }}) of a {{ site.name }}
release contains quite a number of files. GitHub does not support setting descriptions for them, so we put the
descriptions here. The most important factor is the **Checkstyle version** that you use:

- Checkstyle &ge;&nbsp;8.21
- Checkstyle 7.0 -&nbsp;8.20
- Checkstyle 6.16.1 - &nbsp;6.19

<div class="alert alert-info">
  <p>Find detailed descriptions on how to set things up on the <a href="{{ site.baseurl }}/run.html"><i>Get Started</i>
  page</a>.</p>
</div>


### Checkstyle 8.21 and up (at least Java&nbsp;8)

- [checkstyle-addons-{{ page.csa_version }}-all.jar](https://github.com/{{ site.github }}/releases/download/v{{
  page.csa_version }}/checkstyle-addons-{{ page.csa_version }}-all.jar) —
  A fatjar containing {{ site.name }} including all its dependencies; useful for IntelliJ IDEA, Android Studio, Ant,
  and command line.
- [checkstyle-addons-{{ page.csa_version }}.jar](https://github.com/{{ site.github }}/releases/download/v{{
  page.csa_version }}/checkstyle-addons-{{ page.csa_version }}.jar) —
  Just {{ site.name }}, excluding external dependencies. Normally, this file is pulled via Maven Central, so you don't
  need to download it here.
- [checkstyle-addons-{{ page.csa_version }}-javadoc.jar](https://github.com/{{ site.github }}/releases/download/v{{
  page.csa_version }}/checkstyle-addons-{{ page.csa_version }}-javadoc.jar) —
  The Javadoc in Java&nbsp;8 format.
- [checkstyle-addons-eclipse-{{ page.csa_version }}.jar](https://github.com/{{ site.github }}/releases/download/v{{
  page.csa_version }}/checkstyle-addons-eclipse-{{ page.csa_version }}.jar) —
  The {{ site.name }} plugin for Eclipse (requires Eclipse-CS 8.21.0 or newer).
- [sonar-checkstyleaddons-{{ page.csa_version }}.jar](https://github.com/{{ site.github }}/releases/download/v{{
  page.csa_version }}/sonar-checkstyleaddons-{{ page.csa_version }}.jar) —
  The SonarQube plugin (requires SonarQube Checkstyle Plugin 4.21 or newer -
  [download](https://github.com/checkstyle/sonar-checkstyle/releases)).


### Checkstyle 7.0 -&nbsp;8.20 (also at least Java&nbsp;8)

- [checkstyle-addons-java8a-{{ page.csa_version }}-all.jar](https://github.com/{{ site.github }}/releases/download/v{{
  page.csa_version }}/checkstyle-addons-java8a-{{ page.csa_version }}-all.jar) —
  A fatjar containing {{ site.name }} including all its dependencies; useful for IntelliJ IDEA, Android Studio, Ant,
  and command line.
- [checkstyle-addons-java8a-{{ page.csa_version }}.jar](https://github.com/{{ site.github }}/releases/download/v{{
  page.csa_version }}/checkstyle-addons-java8a-{{ page.csa_version }}.jar) —
  Just {{ site.name }}, excluding external dependencies. Normally, this file is pulled via Maven Central, so you don't
  need to download it here.
- [checkstyle-addons-java8a-{{ page.csa_version }}-javadoc.jar](https://github.com/{{ site.github }}/releases/download/v{{
  page.csa_version }}/checkstyle-addons-java8a-{{ page.csa_version }}-javadoc.jar) —
  The Javadoc in Java&nbsp;8 format.
- [checkstyle-addons-java8a-eclipse-{{ page.csa_version }}.jar](https://github.com/{{ site.github }}/releases/download/v{{
  page.csa_version }}/checkstyle-addons-java8a-eclipse-{{ page.csa_version }}.jar) —
  The {{ site.name }} plugin for Eclipse (requires Eclipse-CS 7.2.0 -&nbsp;8.20.0).
- [sonar-checkstyleaddons-{{ page.csa_version }}-csp3.1.jar](https://github.com/{{ site.github }}/releases/download/v{{
  page.csa_version }}/sonar-checkstyleaddons-{{ page.csa_version }}-csp3.1.jar) —
  The SonarQube plugin (requires SonarQube Checkstyle Plugin 3.1 -&nbsp;4.20 -
  [download](https://github.com/checkstyle/sonar-checkstyle/releases)).


### Checkstyle 6.16.1 -&nbsp;6.19 (at least Java&nbsp;7)

- [checkstyle-addons-java7-{{ page.csa_version }}-all.jar](https://github.com/{{ site.github }}/releases/download/v{{
  page.csa_version }}/checkstyle-addons-java7-{{ page.csa_version }}-all.jar) —
  A fatjar containing {{ site.name }} including all its dependencies; useful for IntelliJ IDEA, Android Studio, Ant,
  and command line.
- [checkstyle-addons-java7-{{ page.csa_version }}.jar](https://github.com/{{ site.github }}/releases/download/v{{
  page.csa_version }}/checkstyle-addons-java7-{{ page.csa_version }}.jar) —
  Just {{ site.name }}, excluding external dependencies. Normally, this file is pulled via Maven Central, so you don't
  need to download it here.
- [checkstyle-addons-java7-{{ page.csa_version }}-javadoc.jar](https://github.com/{{ site.github }}/releases/download/v{{
  page.csa_version }}/checkstyle-addons-java7-{{ page.csa_version }}-javadoc.jar) —
  The Javadoc in Java&nbsp;7 format.
- [checkstyle-addons-java7-eclipse-{{ page.csa_version }}.jar](https://github.com/{{ site.github }}/releases/download/v{{
  page.csa_version }}/checkstyle-addons-java7-eclipse-{{ page.csa_version }}.jar) —
  The {{ site.name }} plugin for Eclipse (requires Eclipse-CS 6.16.0 or 6.19.x).


### Source Code

 - [Source code (zip)](https://github.com/{{ site.github }}/archive/v{{ page.csa_version }}.zip) —
   the {{ site.name }} v{{ page.csa_version }} source code
 - [Source code (tar.gz)](https://github.com/{{ site.github }}/archive/v{{ page.csa_version }}.tar.gz) —
   same source code as the ZIP, but packed with `tar`
