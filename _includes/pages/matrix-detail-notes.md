## Detail Notes

The following sections give information on how the data for each column was obtained.

<a name="a" class="csa-offset-anchor"/>

### A. Checkstyle Version

This column simply contains all Checkstyle versions that were released, starting from version 5.0. Version 5.0 was the first Checkstyle version to require Java 5. Since this document was started in 2015, older Java levels seem to be of historical rather than practical interest.

<a name="c" class="csa-offset-anchor"/>

### C. Check behavior backward compatible?

A check mark in this column indicates that a version upgrade of Checkstyle from the previous version:

- does not require changing the Checkstyle configuration file to continue functioning, and
- does not generate different warnings than before if the same configuration is used.

Only the differences to the immediately preceding version are considered.
This information was primarily gathered from the [release notes](http://checkstyle.sourceforge.net/releasenotes.html), but additional analysis of the commit history on GitHub has turned up further breaking changes:

<div class="checkpage"><dl>
<dt class="title">Checkstyle</dt><dd class="title">– Breaking Change</dd>
<dt>6.12.1</dt><dd>– default value of property <code>processJavadoc</code> changed in <i>UnusedImports</i> check (commit <a href="https://github.com/checkstyle/checkstyle/commit/afbb944fe23b1be090ed69bfb97641be9dc2842c">#afbb944</a>, mentioned in release notes as "bug fix")</dd>
<dt>6.12</dt><dd>– <code>package</code> declarations are no longer covered by <i>LineLength</i> check (commit <a href="https://github.com/checkstyle/checkstyle/commit/9a39d19a31f06c8614d33fcc9c3f7654ec9cdd9f">#9a39d19</a>, mentioned in release notes as "bug fix")</dd>
<dt>6.4.1</dt><dd>– property <code>immutableClassNames</code> renamed to <code>immutableClassCanonicalNames</code> in <i>VisibilityModifier</i> check (commit <a href="https://github.com/checkstyle/checkstyle/commit/de4485a22bfe8eafc3c2e273fd328a0221d0ff9e">#de4485a</a>, mentioned in release notes as "bug fix")</dd>
<dt>6.3</dt><dd>– Issue <a href="https://github.com/checkstyle/checkstyle/issues/585">#585</a> mentioned as the only breaking change in the release notes is not in fact breaking anything, but the <i>IllegalType</i> check got its default illegal types changed (commit <a href="https://github.com/checkstyle/checkstyle/commit/677acc1e8491acffd24c859e553b06362da0d471">#677acc1</a>)</dd>
<dt>5.8</dt><dd>– <i>TypeName</i> check now covers enums and annotations (commits <a href="https://github.com/checkstyle/checkstyle/commit/686d009cf4d21d0ace52b9e7dccc848e8b9ce91a">#686d009</a> and <a href="https://github.com/checkstyle/checkstyle/commit/f3d433723bebb55e09eba1562b6c75407f6fe46f">#f3d4337</a>, mentioned in release notes as "new feature" and "bug fix")</dd>
</dl></div>

This is not a complete list of breaking changes - only the first breaking change was confirmed, but more may exist. The breaking change may not affect your configuration, if you don't use the changed check.

<a name="d" class="csa-offset-anchor"/>

### D. Public API backward compatible?

A check mark in this column indicates that a version upgrade of Checkstyle from the previous version does not require changes to a custom check, as long as that custom check uses only the public API. Cases where the custom check is implemented by subclassing a built-in check were not considered. Only the differences to the immediately preceding version are considered. This information was primarily gathered from the [release notes](http://checkstyle.sourceforge.net/releasenotes.html), but additional analysis of the commit history on GitHub has turned up further breaking changes:

<div class="checkpage"><dl>
<dt class="title">Checkstyle</dt><dd class="title">– Breaking Change</dd>
<dt>6.12</dt><dd>– public method renamed in utils/CheckUtils (commit <a href="https://github.com/checkstyle/checkstyle/commit/e2b4e687d7b8f9d6d5e1346d874b90e4567aff81">#e2b4e68</a>)</dd>
<dt>6.9</dt><dd>– public methods removed from api/JavadocTagInfo (commit <a href="https://github.com/checkstyle/checkstyle/commit/534536aa623b2f723daf95d9e43d31f1b2734906">#534536a</a>)</dd>
<dt>6.6</dt><dd>– public method <code>getFilename()</code> renamed to <code>getFileName()</code> in api/FileContents (commit <a href="https://github.com/checkstyle/checkstyle/commit/7dd24c8c35572b5db3e5c905d440e813cfe2538c">#7dd24c8</a>)</dd>
<dt>5.7</dt><dd>– signature of public method <code>fireErrors()</code> changed in api/MessageDispatcher (commit <a href="https://github.com/checkstyle/checkstyle/commit/1d614c3a7ecf8a3ede4df8a50da46e71792d0025">#1d614c3</a>)</dd>
</dl></div>

This is not the complete list of undocumented breaking changes - only the first breaking change was confirmed, but more may exist. The breaking change may not affect your custom checks - if you don't make use of the changed part of the API.

<a name="e" class="csa-offset-anchor"/>

### E. Java Version used to run Checkstyle

This is the Java version required by Checkstyle, i.e. the version of the JVM in which the Checkstyle process is running. It has nothing to do with the Java version of the code being analyzed. Java versions are backwards compatible, so only a minimum version is shown. The required Java version is clearly stated in the Checkstyle [release notes](http://checkstyle.sourceforge.net/releasenotes.html). Where that did not suffice, the class files of the binary distributions were used to obtain the minimum Java version.

<a name="f" class="csa-offset-anchor"/>

### F. Java Version of the code being analyzed

This is the Java version of the code being given to Checkstyle for analysis. Checkstyle must understand its grammar, so there is a maximum Java level for each Checkstyle version, which is clearly stated in the release notes.

<a name="g" class="csa-offset-anchor"/>

### G. Checkstyle-IDEA

The compatibility information was obtained by unpacking [each version](https://plugins.jetbrains.com/plugin/1065?pr=&showAllUpdates=true) of Checkstyle-IDEA and looking inside which version of Checkstyle it contained. As a cross-check, the [version history](https://github.com/jshiell/checkstyle-idea#version-history) of Checkstyle-IDEA was referenced.

<a name="h" class="csa-offset-anchor"/>

### H. Java Version required by Checkstyle-IDEA

Checkstyle-IDEA sometimes requires a more recent Java version than Checkstyle itself. Depending on the Checkstyle-IDEA version, it may be required to run IntelliJ IDEA at a particular Java level, which is given in this column. The information was obtained by looking inside the `org.infernus.idea.checkstyle.CheckStylePlugin` class file and reading the version number of the class file format.

<a name="i" class="csa-offset-anchor"/>

### I. Eclipse-CS

[Eclipse-CS](http://eclipse-cs.sourceforge.net), the Checkstyle plugin for Eclipse, is very professionally managed. Its version numbers are synched to the Checkstyle version numbers, and every change is meticulously documented in its [release notes](http://eclipse-cs.sourceforge.net/#!/releasenotes). The required Java level is the same as for Checkstyle itself.

<a name="j" class="csa-offset-anchor"/>

### J. Maven Checkstyle Plugin

For the [Maven Checkstyle Plugin](https://maven.apache.org/plugins/maven-checkstyle-plugin/), its POM files where analyzed as found on [Maven Central](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22org.apache.maven.plugins%22%20AND%20a%3A%22maven-checkstyle-plugin%22). This column can have one of the following entries:

<div class="checkpage"><dl>
<dt class="title">Entry</dt><dd class="title">– Description</dd>
<dt>✓</dt><dd>– This version of Checkstyle can be used, but must be <a
href="https://maven.apache.org/plugins/maven-checkstyle-plugin/examples/upgrading-checkstyle.html">configured explicitly</a>.
Be sure to choose the base version of the plugin to match your Java version (for example, use 2.15 if on Java 6).
In 2015, the Checkstyle team has started checking compatibility with the Maven Checkstyle Plugin in their build process, so it generally works well.</dd>
<dt>2.15</dt><dd>– The version number of the Maven Checkstyle plugin which is configured to use this version of Checkstyle by default.
This implies a checkmark (✓).</dd>
<dt>(✓) <span style="font-weight: normal;">or</span> (2.4)</dt><dd>– This version of Checkstyle can be used, but it is not available on Maven Central, so you would have to
provide it via your own infrastructure.</dd>
<dt>--</dt><dd>– This version of Checkstyle cannot be used with Maven.</dd>
</dl></div>

TODO We may have to improve on this data by actually trying the different combinations of Maven Plugin and Checkstyle versions, because breaking changes are introduced and may forbid specific combinations.

<a name="k" class="csa-offset-anchor"/>

### K. Gradle

In [Gradle](https://gradle.org/) 1.0, the Gradle Checkstyle plugin was created by extracting it out of the Code Quality plugin which existed in Gradle&nbsp;0.9. At the same time, the `toolVersion` property was introduced to the Gradle Checkstyle plugin, which allows choosing the Checkstyle version to run. Previous versions of Gradle used hardcoded Checkstyle versions, which is why they are not included in our matrix.
Up to a breaking change in Checkstyle 6.8 (Checkstyle issue [#1108](https://github.com/checkstyle/checkstyle/issues/1108)), Gradle was compatible with all versions of Checkstyle that are available on Maven Central or Bintray jcenter. Checkstyle 5.2 is the oldest version available there, so it is also the oldest you can use with Gradle, unless you are willing to provide it via your own infrastructure. Because of this, the corresponding entries are set in parentheses.
This data was gathered by GitHub source code analysis.

<a name="l" class="csa-offset-anchor"/>

### L. Gradle Checkstyle Plugin

This column shows which versions of Checkstyle are used as the default version by the [Gradle Checkstyle Plugin](https://docs.gradle.org/current/userguide/checkstyle_plugin.html). This information was gained by analyzing the [source code of the plugin](https://github.com/gradle/gradle/tree/REL_2.7/subprojects/code-quality/src/main/groovy/org/gradle/api/plugins/quality) on GitHub. The default version gets used when `toolVersion` is not set. A checkmark (✓) indicates that the Checkstyle version is available via `toolVersion`, but not by default. The bottom line is that one should *always* set `toolVersion`.
Entries are set in parentheses when either the Checkstyle version is less than 5.2 (because then it is not available on Maven Central or Bintray jcenter), or the Gradle version is less than 1.0 (because then no Checkstyle plugin exists).

<a name="m" class="csa-offset-anchor"/>

### M. {{ site.name }}

{{ site.name }} features a build process that actually tries running the compiled checks against every single Checkstyle runtime ([example log file](https://travis-ci.org/{{ site.github }}/jobs/78284770#L353)). Since we have very [high test coverage](https://coveralls.io/builds/3457153), the fact that all unit tests complete successfully against a Checkstyle runtime is significant. {{ site.name }} also includes a reflection-based component that smoothes over the API differences between Checkstyle versions, so that we have full compatibility with every recent Checkstyle version.

<a name="n" class="csa-offset-anchor"/>

### N. SevNTU Checkstyle

For [SevNTU Checkstyle](https://github.com/sevntu-checkstyle/sevntu.checkstyle), source versions are tagged on GitHub for versions 1.8.0 and upwards, so source archives are easily available for downloading. For versions down to 1.5.x, POM files could still be found with the [binaries](https://github.com/sevntu-checkstyle/sevntu.checkstyle/tree/gh-pages/maven2/com/github/sevntu/checkstyle/sevntu-checks) which give information on the Checkstyle version for which that version of SevNTU Checkstyle was built. For versions 1.8.0 and upwards, SevNTU Checkstyle is compiled and the unit tests are executed against the different Checkstyle runtimes. When all tests pass without errors, the versions are considered compatible. Versions declared in the POM files are *always* considered compatible (and proved to be so in the tests conducted). Compilation and tests for SevNTU Checkstyle versions 1.13.0 and upwards where performed with Java&nbsp;7, all previous versions where compiled and tested with Java&nbsp;6.
Compatibility was analyzed based on the assumption that the full SevNTU Checkstyle feature set is required to work. In many cases, only a handful of tests were failing, so it may still be an option to use SevNTU Checkstyle with an incompatible version of Checkstyle, if the subset of SevNTU checks you are using work.
Also, you may be able to set up the SevNTU checks in a separate job with a configuration file of its own. That second job would use a version of Checkstyle that is compatible with SevNTU Checkstyle (for example, v6.6).

<a name="o" class="csa-offset-anchor"/>

### O. SonarQube Checkstyle Plugin

The [SonarQube Checkstyle Plugin](http://docs.sonarqube.org/display/PLUG/Checkstyle+Plugin) lists the Checkstyle versions it uses on its website. The given version of Checkstyle is bundled with the plugin and cannot be changed. The plugin has a dependency on the SonarQube platform and the SonarQube Java plugin. Updates to this plugin are quite rare, so only a small number of Checkstyle versions are supported by SonarQube.

<a name="p" class="csa-offset-anchor"/>

### P. SonarQube Platform

This column shows the version of the SonarQube platform (a.k.a. the SonarQube version) required to run the SonarQube Checkstyle plugin at the given version.

<a name="q" class="csa-offset-anchor"/>

### Q. SonarQube Java Plugin

This column shows the version of the [SonarQube Java Plugin](http://docs.sonarqube.org/display/PLUG/Java+Plugin) required to run the SonarQube Checkstyle plugin at the given version. This was obtained by looking at the manifest.mf entry in the plugin's JAR file.


### Jenkins / Bamboo

TODO
Jenkins does not run Checkstyle, but parses the XML report generated by Checkstyle. Still, it needs to understand what it sees.
It seems that the format of the XML report created by Checkstyle has been quite stable over time.
