---
layout: post
title: "Gradle Checkstyle - How to scan all of the sources instead of just Java files"
date: 2019-03-09 14:48:00 +0100
release: false
emphasis: false
---

The Gradle Checkstyle plugin only passes `.java` files to Checkstyle by default. Here's how to make sure no files
are missed.<!--break-->

Many Checkstyle checks only unleash their full potential if really *all* files are passed to Checkstyle. For example,
our [ModuleDirectoryLayout]({{ site.baseurl }}/latest/checks/misc.html#ModuleDirectoryLayout) check does not make
much sense if it can see only `.java` files. Like all [FileSetChecks]({{ site.link_cs_filesetcheck }}), it can operate
on any type of file, and actually should in order to do its job right.

How to configure Gradle to pass all files to Checkstyle is not immediately obvious from the
[documentation](https://docs.gradle.org/5.2.1/userguide/checkstyle_plugin.html), but it's actually not complicated
either:

**Option 1:** Do it only for one particular source set, for example `main`:

```groovy
tasks['checkstyleMain'].setSource(project.sourceSets.main.allSource);
```

**Option 2:** Do it for all Checkstyle tasks for all source sets:

```groovy
project.extensions.findByName('checkstyle').sourceSets.each { SourceSet s ->
    Checkstyle task = (Checkstyle) tasks.findByName(s.getTaskName('checkstyle', null));
    task.setSource(s.allSource);
    getLogger().info('Reconfigured task \'' + task.name +
        '\' to include all files in sourceSet \'' + s.name + '\'');
}
```

The key operation in both examples above is the call to `setSource()`. The Gradle Checkstyle plugin is
[hardcoded](https://github.com/gradle/gradle/blob/v5.2.1/subprojects/code-quality/src/main/groovy/org/gradle/api/plugins/quality/CheckstylePlugin.java#L165)
to use the `allJava` property, which includes only the Java files. Fortunately, we can overwrite this as shown above.  
Note that the classpath is not affected, because the non-Java files usually don't get compiled into `.class` files.

This post is based on Gradle 5.2.1 and Checkstyle 8.18.
