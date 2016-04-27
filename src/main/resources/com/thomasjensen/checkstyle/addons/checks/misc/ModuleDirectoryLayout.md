## ModuleDirectoryLayout

The *ModuleDirectoryLayout* check enforces a configurable directory structure in all modules of a project. It also
checks that certain content is located in particular source folders. This is especially useful for large multi-module
projects. By default, it is configured for a basic [Maven Standard Directory Layout](https://maven.apache.org/guides/introduction/introduction-to-the-standard-directory-layout.html).


### Overview

For the purposes of this check, the absolute path of any file checked by Checkstyle is composed of four parts:

<p><tt><span style="background-color:#dddddd;">D:\Projects\myproject</span>\<span style="background-color:#ddddff;">dir1\mymodule1</span>\<span style="background-color:#ffdddd;">src\main\java</span>\<span style="background-color:#ddffdd;">com\acme\MyClass.java</span></tt></p>

- the <span style="background-color:#dddddd;">**baseDir**</span> (gray) as specified by the corresponding check
  property `baseDir`
- the <span style="background-color:#ddddff;">**module path**</span> (blue) is determined by the `moduleRegex`
  setting in *directories.json*. In a single-module project, the module path is the empty
  String.
- the <span style="background-color:#ffdddd;">**MDL path**</span> (red) as configured in the *directories.json* config
  file. Within a module, every file must be located either on an MDL path or in the module root.
- The <span style="background-color:#ddffdd;">**specific path**</span> (green) may play a role if it is used in the
  *directories.json* config file in order to control the contents of specific MDL paths.

The example is given as a Windows path, but the same would work for UNIX style paths. In fact, it does not matter
whether you specify the separators as slashes or backslashes.

This check uses a *directories.json* config file via the `configFile` property in order to list all possible MDL paths
and the specific content they may contain.


### Properties

<dl>
<dt><span class="propname">baseDir</span>
    <span class="proptype"><a href="{{ site.link_cs_type_string }}">String</a></span></dt>
<dd><span class="propdesc">Base directory to assume for the check execution, usually the project root</span>
    <span class="propdefault"><code>.</code></span></dd>

<dt><span class="propname">configFile</span>
    <span class="proptype"><a href="{{ site.link_cs_type_string }}">String</a></span></dt>
<dd><span class="propdesc">Location of the configuration file for this check. Relative paths are interpreted relative
    to the current directory of the Checkstyle analysis process, which is usually the project root. The configuration
    file is a JSON file with UTF-8 character set in the format described below.<br/>
    If the specified file cannot be found or parsed, the check will be disabled.</span>
    <span class="propdefault"><a href="https://github.com/{{ site.github }}/blob/{{ page.check_version }}/src/main/resources/com/thomasjensen/checkstyle/addons/checks/misc/ModuleDirectoryLayout-default.json">Maven
    Directory Layout</a></span></dd>
</dl>

Only these two properties are set in the check configuration. Everything else is configured via the *directories.json*
file, which allows a central check definition (e.g. in SonarQube) to be used for many different projects, each of which
features its own *directories.json*.


### Format of *directories.json*

The configuration file is most easily understood by studying the [default
configuration](https://github.com/{{ site.github }}/blob/{{ page.check_version }}/src/main/resources/com/thomasjensen/checkstyle/addons/checks/misc/ModuleDirectoryLayout-default.json),
and using that as a starting point. The file must be UTF-8 encoded. It is composed of two mandatory sections: `settings` and `structure`.

**settings:**

 - `formatVersion`: The version number of the config file format. Later versions of this check may change the format,
   and use this field to stay backwards-compatible. For now, always put a `1` here.
 - `moduleRegex`: A regular expression to identify the module path in a file's pathname. The file path passed to this
   expression is the relative path to the file from the `baseDir`. The default is the empty String, which means that
   we have a single-module project.
 - `allowNestedSrcFolder`: Flag indicating if `src` may be used as a package name fragment or subfolder anywhere. The
   default is `false`.

**structure:**

The `structure` section contains a map whose keys are the MDL paths which can be present in each module. The value
objects of the map may be empty, but they may also contain any of the following elements:

 - `modules`: A regular expression applied to the *module path*. The expression matches all module paths for which
   the current MDL path is valid. For example, in this way, the MDL path `src/main/webapp` may be restricted to web
   modules.
 - `whitelist`: A flag which tells us if the `allow` list in this object is a whitelist. If so, only files which
   explicitly match one of the conditions in the `allow` list may be present on this MDL path. If `whitelist` is
   `false`, then the `allow` list is largely irrelevant, except where it is referenced from `deny` lists or for the
   `TopLevelFolder` type element.
 - `allow`: List of conditions for allowed content of the MDL path
 - `deny`: List of conditions for forbidden content of the MDL path

The `allow` and `deny` lists contain elements of the following form: `{ "type": "FileExtensions", "spec": "java" }`. The `type` element may be one of the constants from
[MdlContentSpecType]({{ site.baseurl }}/{{ page.check_version }}/apidocs/index.html?com/thomasjensen/checkstyle/addons/checks/misc/MdlContentSpecType.html). Note that
the `FromPath` type may only occur in `deny` lists.


#### Validation

In order to be sure that your customized *directories.json* file is syntactically correct and does not violate any
constraints, a small verification program is supplied. Download a copy of
[checkstyle-addons-{{ page.check_version | remove_first:'v' }}-all.jar](https://github.com/{{ site.github }}/releases/tag/{{ page.check_version }}),
then run the validator from the command line:

    java -cp checkstyle-addons-{{ page.check_version | remove_first:'v' }}-all.jar com.thomasjensen.checkstyle.addons.checks.misc.MdlJsonConfigValidator path/to/my/directories.json


### Examples

In the following example, the check is configured to ensure that a single-module project follows the Maven
 Directory Convention:

{% highlight xml %}
<module name="ModuleDirectoryLayout"/>
{% endhighlight %}

More typically, you will want to customize the check to allow for multi-module projects and specialized folder
structures:

{% highlight xml %}
<module name="ModuleDirectoryLayout">
  <property name="baseDir" value="${workspace_loc}"/>
  <property name="configFile" value="config/directories.json"/>
</module>
{% endhighlight %}

The above example is for Eclipse, where `${workspace_loc}` may be used to [refer](http://eclipse-cs.sourceforge.net/#!/properties) to the file system location of the current workspace. For SonarQube, you may use relative file paths. For the other environments, you may define a custom [module property]({{ site.link_cs_properties }}), which you dynamically set to the project directory. Example for Gradle:

{% highlight groovy %}
checkstyle {
    configProperties 'workspace_loc': project.projectDir;
}
{% endhighlight %}


### Parent Module

<div class="alert alert-info">
  <p>Important: This check goes directly under <b>Checker</b>, not under <b>TreeWalker</b>.</p>
</div>

[Checker]({{ site.link_cs_checker }})
