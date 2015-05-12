## Building from Source

In order to build {{ site.name }} yourself, follow these steps:

  1. Make sure you have [Git](http://git-scm.com/) installed
  2. Make sure you have a Java&nbsp;7 SDK installed, and you are using it for this build ([download](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html))
  3. Clone the {{ site.name }} repo by running on the command line:
     `git clone https://github.com/{{ site.github }}.git`
  4. `cd checkstyle-addons`
  5. `gradlew build`
     This will download Gradle if you haven't got it yet. It will also take care of downloading everything else needed for a successful build of {{ site.name }}.
  6. Find the fresh artifacts in build/libs. Done!

## Resources

A quick list of development resources:

  - [Sources](https://github.com/{{ site.github }}) on GitHub
  - [Javadoc]({{ site.baseurl }}/latest/apidocs/) (current version)
    Javadoc for previous versions may be found in the [release history]({{ site.baseurl }}/releases.html).
  - [Change log](https://github.com/{{ site.github }}/commits/master)
  - This website is also hosted on GitHub: [website sources](https://github.com/{{ site.github }}/tree/gh-pages)

## Quality Matters

TODO