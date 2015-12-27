## RequiredFile

The *RequiredFile* check helps make sure that certain files exist in specified places.


### Properties

<dl>
<dt><span class="propname">file</span>
    <span class="proptype"><a href="http://checkstyle.sourceforge.net/property_types.html#string">String</a></span></dt>
<dd><span class="propdesc">Simple name of the file required to be present in the specified directories</span>
    <span class="propdefault">none (check is disabled)</span></dd>

<dt><span class="propname">baseDir</span>
    <span class="proptype"><a href="http://checkstyle.sourceforge.net/property_types.html#string">String</a></span></dt>
<dd><span class="propdesc">Base directory to assume for the check execution, usually the project root</span>
    <span class="propdefault"><code>.</code> (the project root)</span></dd>

<dt><span class="propname">directories</span>
    <span class="proptype"><a href="http://checkstyle.sourceforge.net/property_types.html#stringSet">StringSet</a></span></dt>
<dd><span class="propdesc">Comma-separated list of directories in which the <code>file</code> is required to be
        present. It is possible to use file globs here, in which case existing directories are checked against the
        glob expression. If there is a match, the <code>file</code> is required to be present. All directories in
        this list are interpreted as relative to the <code>baseDir</code>.</span>
    <span class="propdefault">none (check is disabled)</span></dd>

<dt><span class="propname">ignoreEmptyDirs</span>
    <span class="proptype"><a href="http://checkstyle.sourceforge.net/property_types.html#boolean">Boolean</a></span></dt>
<dd><span class="propdesc">If <code>true</code>, <code>directories</code> are ignored if they contain no files;
        if <code>false</code>, directories are never ignored. "Empty" means that the directory does not contain
        any files; it may however contain other directories.</span>
    <span class="propdefault"><code>false</code></span></dd>

<dt><span class="propname">caseSensitive</span>
    <span class="proptype"><a href="http://checkstyle.sourceforge.net/property_types.html#boolean">Boolean</a></span></dt>
<dd><span class="propdesc">If <code>true</code>, the <code>file</code> must be present with the exact spelling
        given, including upper and lowercase; if <code>false</code>, case is ignored.</span>
    <span class="propdefault"><code>true</code></span></dd>
</dl>


### Examples

The following example checks that a *README.md* file is present in the project root:

{% highlight xml %}
<module name="RequiredFile">
  <property name="file" value="README.md"/>
  <property name="baseDir" value="${workspace_loc}"/>
  <property name="caseSensitive" value="true"/>
</module>
{% endhighlight %}

In the next example, a *logo.png* file is required in all *img* folders two level below any *web* folders:

{% highlight xml %}
<module name="RequiredFile">
  <property name="file" value="logo.png"/>
  <property name="baseDir" value="${workspace_loc}"/>
  <property name="directories" value="**/web/*/img"/>
</module>
{% endhighlight %}

This check can also serve as a replacement of JavadocPackage with better configurability. In this example, we configure the check to work just like JavadocPackage, but only on main sources:

{% highlight xml %}
<module name="RequiredFile">
  <property name="file" value="package-info.java"/>
  <property name="baseDir" value="${workspace_loc}/src/main/java"/>
  <property name="directories" value="**"/>
  <property name="ignoreEmptyDirs" value="true"/>
</module>
{% endhighlight %}


### Related Checks

If you only want to check for *package-info.java* files, [JavadocPackage](http://checkstyle.sourceforge.net/config_javadoc.html#JavadocPackage) may be enough. This check has better configurability though.
Anything to do with already existing files is probably covered by [RegexpOnFilename]({{ site.baseurl }}/latest/checks/regexp.html#RegexpOnFilename).

### Parent Module

[TreeWalker](http://checkstyle.sourceforge.net/config.html#TreeWalker)
