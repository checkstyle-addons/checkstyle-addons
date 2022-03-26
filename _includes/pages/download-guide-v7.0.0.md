{% comment %}
    This version of the Download Guide is used starting with Checkstyle 10.0 (Checkstyle Addons v7.0.0).
    For earlier versions, use the download-guide.html (without postfix) or download-guide-2.html.
{% endcomment %}
# Download Guide

The [downloads section](https://github.com/{{ site.github }}/releases/tag/v{{ page.csa_version }}) of a {{ site.name }}
release contains quite a number of files. GitHub does not support setting descriptions for them, so we put the
descriptions here. Also, GitHub does not allow `.jar` extensions, so we had to rename them all to `.zip`. Remember to
rename them back to `.jar` after download.


<div class="alert alert-info">
    <h4 class="alert-heading">Download</h4>
    The premier download location for {{ site.name }} JARs is <a href="{{ site.link_central_download }}">Maven Central</a>.
</div>

The most important factor is the **Checkstyle version** that you use:

- Checkstyle &ge;&nbsp;10.0
- Checkstyle 8.21 -&nbsp;9.3
- Checkstyle 7.0 -&nbsp;8.20
- Checkstyle 6.16.1 - &nbsp;6.19

Find detailed descriptions on how to set things up on the <a href="{{ site.baseurl }}/run.html"><i>Get Started</i>
page</a>.


### Checkstyle 10.0 and up (at least Java&nbsp;11)

- [checkstyle-addons-{{ page.csa_version }}-all.jar](https://github.com/{{ site.github }}/releases/download/v{{
  page.csa_version }}/checkstyle-addons-{{ page.csa_version }}-all.zip) —
  A fatjar containing {{ site.name }} including all its dependencies; useful for IntelliJ IDEA, Android Studio, Ant,
  and command line.
- [checkstyle-addons-{{ page.csa_version }}.jar]({{ site.link_central_download }}/checkstyle-addons/{{
  page.csa_version }}/checkstyle-addons-{{ page.csa_version }}.jar) —
  Just {{ site.name }}, excluding external dependencies. Normally, this file is pulled via Maven Central, so you don't
  need to download it here.
- [checkstyle-addons-{{ page.csa_version }}-javadoc.jar]({{ site.link_central_download }}/checkstyle-addons/{{
  page.csa_version}}/checkstyle-addons-{{ page.csa_version }}-javadoc.jar) —
  The Javadoc in Java&nbsp;11 format.
- [checkstyle-addons-eclipse-{{ page.csa_version }}.jar](https://github.com/{{ site.github }}/releases/download/v{{
  page.csa_version }}/checkstyle-addons-eclipse-{{ page.csa_version }}.zip) —
  The {{ site.name }} plugin for Eclipse (requires Eclipse-CS 10.0 or newer).
- [sonar-checkstyleaddons-{{ page.csa_version }}.jar](https://github.com/{{ site.github }}/releases/download/v{{
  page.csa_version }}/sonar-checkstyleaddons-{{ page.csa_version }}.zip) —
  The SonarQube plugin (requires SonarQube Checkstyle Plugin 10.0 or newer -
  [download](https://github.com/checkstyle/sonar-checkstyle/releases)).


### Checkstyle 8.21 -&nbsp;9.3 (at least Java&nbsp;8)

- [checkstyle-addons-java8b-{{ page.csa_version }}-all.jar](https://github.com/{{ site.github }}/releases/download/v{{
  page.csa_version }}/checkstyle-addons-java8b-{{ page.csa_version }}-all.zip) —
  A fatjar containing {{ site.name }} including all its dependencies; useful for IntelliJ IDEA, Android Studio, Ant,
  and command line.
- [checkstyle-addons-java8b-{{ page.csa_version }}.jar]({{ site.link_central_download }}/checkstyle-addons-java8b/{{
  page.csa_version }}/checkstyle-addons-java8b-{{ page.csa_version }}.jar) —
  Just {{ site.name }}, excluding external dependencies. Normally, this file is pulled via Maven Central, so you don't
  need to download it here.
- [checkstyle-addons-java8b-{{ page.csa_version }}-javadoc.jar]({{ site.link_central_download }}/checkstyle-addons-java8b/{{
  page.csa_version }}/checkstyle-addons-java8b-{{ page.csa_version }}-javadoc.jar) —
  The Javadoc in Java&nbsp;8 format.
- [checkstyle-addons-java8b-eclipse-{{ page.csa_version }}.jar](https://github.com/{{ site.github }}/releases/download/v{{
  page.csa_version }}/checkstyle-addons-java8b-eclipse-{{ page.csa_version }}.zip) —
  The {{ site.name }} plugin for Eclipse (requires Eclipse-CS 8.21.0 -&nbsp;9.3).
- [sonar-checkstyleaddons-{{ page.csa_version }}-csp4.29.jar](https://github.com/{{ site.github }}/releases/download/v{{
  page.csa_version }}/sonar-checkstyleaddons-{{ page.csa_version }}-csp4.29.zip) —
  The SonarQube plugin (requires SonarQube Checkstyle Plugin 4.29 -&nbsp;9.3 -
  [download](https://github.com/checkstyle/sonar-checkstyle/releases)).


### Checkstyle 7.0 -&nbsp;8.20 (also at least Java&nbsp;8)

- [checkstyle-addons-java8a-{{ page.csa_version }}-all.jar](https://github.com/{{ site.github }}/releases/download/v{{
  page.csa_version }}/checkstyle-addons-java8a-{{ page.csa_version }}-all.zip) —
  A fatjar containing {{ site.name }} including all its dependencies; useful for IntelliJ IDEA, Android Studio, Ant,
  and command line.
- [checkstyle-addons-java8a-{{ page.csa_version }}.jar]({{ site.link_central_download }}/checkstyle-addons-java8a/{{
  page.csa_version }}/checkstyle-addons-java8a-{{ page.csa_version }}.jar) —
  Just {{ site.name }}, excluding external dependencies. Normally, this file is pulled via Maven Central, so you don't
  need to download it here.
- [checkstyle-addons-java8a-{{ page.csa_version }}-javadoc.jar]({{ site.link_central_download }}/checkstyle-addons-java8a/{{
  page.csa_version }}/checkstyle-addons-java8a-{{ page.csa_version }}-javadoc.jar) —
  The Javadoc in Java&nbsp;8 format.
- [checkstyle-addons-java8a-eclipse-{{ page.csa_version }}.jar](https://github.com/{{ site.github }}/releases/download/v{{
  page.csa_version }}/checkstyle-addons-java8a-eclipse-{{ page.csa_version }}.zip) —
  The {{ site.name }} plugin for Eclipse (requires Eclipse-CS 7.2.0 -&nbsp;8.20.0).


### Checkstyle 6.16.1 -&nbsp;6.19 (at least Java&nbsp;7)

- [checkstyle-addons-java7-{{ page.csa_version }}-all.jar](https://github.com/{{ site.github }}/releases/download/v{{
  page.csa_version }}/checkstyle-addons-java7-{{ page.csa_version }}-all.zip) —
  A fatjar containing {{ site.name }} including all its dependencies; useful for IntelliJ IDEA, Android Studio, Ant,
  and command line.
- [checkstyle-addons-java7-{{ page.csa_version }}.jar]({{ site.link_central_download }}/checkstyle-addons-java7/{{
  page.csa_version }}/checkstyle-addons-java7-{{ page.csa_version }}.jar) —
  Just {{ site.name }}, excluding external dependencies. Normally, this file is pulled via Maven Central, so you don't
  need to download it here.
- [checkstyle-addons-java7-{{ page.csa_version }}-javadoc.jar]({{ site.link_central_download }}/checkstyle-addons-java7/{{
  page.csa_version }}/checkstyle-addons-java7-{{ page.csa_version }}-javadoc.jar) —
  The Javadoc in Java&nbsp;7 format.
- [checkstyle-addons-java7-eclipse-{{ page.csa_version }}.jar](https://github.com/{{ site.github }}/releases/download/v{{
  page.csa_version }}/checkstyle-addons-java7-eclipse-{{ page.csa_version }}.zip) —
  The {{ site.name }} plugin for Eclipse (requires Eclipse-CS 6.16.0 or 6.19.x).


### Source Code

 - [Source code (zip)](https://github.com/{{ site.github }}/archive/v{{ page.csa_version }}.zip) —
   the {{ site.name }} v{{ page.csa_version }} source code
 - [Source code (tar.gz)](https://github.com/{{ site.github }}/archive/v{{ page.csa_version }}.tar.gz) —
   same source code as the ZIP, but packed with `tar`
