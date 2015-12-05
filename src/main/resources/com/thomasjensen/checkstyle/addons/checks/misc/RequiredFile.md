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
    <span class="propdefault"><code>.</code></span></dd>

<dt><span class="propname">directories</span>
    <span class="proptype"><a href="http://checkstyle.sourceforge.net/property_types.html#stringSet">StringSet</a></span></dt>
<dd><span class="propdesc">Comma-separated list of method calls that should be covered by this check. Each element of
        the list must be the full method call as it occurs in the source file, ignoring whitespace and parentheses.
        For example, <code>LogManager.getLogger</code>. This property or <code>variableNames</code> must be set for
        this check to do anything.</span>
    <span class="propdefault">none (check is disabled)</span></dd>

<dt><span class="propname">ignoreEmptyDirs</span>
    <span class="proptype"><a href="http://checkstyle.sourceforge.net/property_types.html#boolean">Boolean</a></span></dt>
<dd><span class="propdesc">If <code>true</code>, directories which match the glob specified by the
        <code>directories</code> property are still ignored if empty; if <code>false</code>, empty directories are
        not ignored. "Empty" means that the directory does not contain any files; it may however contain other
        directories.</span>
    <span class="propdefault"><code>false</code></span></dd>

<dt><span class="propname">caseSensitive</span>
    <span class="proptype"><a href="http://checkstyle.sourceforge.net/property_types.html#boolean">Boolean</a></span></dt>
<dd><span class="propdesc">If <code>true</code>, the property keys are treated as case sensitive; if <code>false</code>,
        case is ignored.</span>
    <span class="propdefault"><code>true</code></span></dd>
</dl>


### Examples

The following example checks that the current method name is passed as the first argument to certain log methods, or given as a String literal in initializations of variables named `method`:

{% highlight xml %}
<module name="RequiredFile">
  <property name="file" value="README.md"/>
  <property name="baseDir" value="${workspace_loc}"/>
  <property name="caseSensitive" value="true"/>
</module>
{% endhighlight %}

In the next example, the `getLogger` method takes the current class as its first argument. The class shall be specified as a simple class object (e.g. `MyClass.class`). Just `getClass()` would not work. Calls where the argument is not a class object are ignored. Note the optional custom message, which allows tailoring the violation text to the particular use case:

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

JavadocPackage
RegexpOnFilename

### Parent Module

[TreeWalker](http://checkstyle.sourceforge.net/config.html#TreeWalker)
