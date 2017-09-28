## PropertyCatalog

The *PropertyCatalog* check helps to keep a property file in sync with a piece of code that contains the property keys.
That piece of code is called the "property catalog", and may be:

  - an enum, where each enum constant requires an entry in the property file, or
  - a class or even an interface with constants, where each constant requires an entry in the property file.

The property catalog and the properties file are expected to be connected to each other by a configurable naming
convention.

### Example

The property catalog code could look like this (other forms are possible, see below):

{% highlight java %}
public enum PropertyCatalog {
  SomeConstant("zero"),
  AnotherConstant("two"),
  AThirdConstant("two"),  // Duplicate!
  YetAnotherConstant("three");

  private final int code;
  private PropertyCatalog(final int pCode) { code = pCode; }
  public int getKey() { return code; }
}
{% endhighlight %}

The constructor arguments of the enum constants are the keys to the property file, which in turn looks like this:

{% highlight properties %}
zero = Some text
one = Some more text
two = Could be anything
three = These values do not matter for the check.
{% endhighlight %}

In this example, the property with key `one` is *orphaned*, because it is not referenced by the property catalog. It
would be flagged along with the duplicate reference to `two`.

### Other forms of property catalogs

In a property catalog, the enum constants themselves may also be used as keys:

{% highlight java %}
public enum PropertyCatalog {
  zero, one, two, three;
}
{% endhighlight %}

The property catalog can also consist of normal constants. Private constants are ignored.

{% highlight java %}
public final class PropertyCatalog {
    public static final String SOME_CONSTANT = "zero";
    public static final String ANOTHER_CONSTANT = "one";
    public static final String A_THIRD_CONSTANT = "two";
    public static final String YET_ANOTHER_CONSTANT = "three";
}
{% endhighlight %}

<div class="alert alert-info">
  <p>Important: The keys in a property catalog must be simple literals of type <code>String</code>, <code>int</code>,
  <code>long</code>, or <code>boolean</code>. No arithmetic allowed, no method calls, no references to other constants.
  Just simple literals. Remember that Checkstyle works on source code.</p>
</div>


### Overview of the check

The check roughly works like this:

  1. Select the Java source files (called the *property catalogs*) to check. Only types which match the regular
     expression specified in `selection` are checked.
  1. The constants defined in the property catalog are read. Constants that match `excludedFields` are ignored.
  1. Using the template specified as `propertyFile`, the corresponding property file is located.
  1. Then, keys from the property catalog are compared to keys in the property file. Warnings are logged if any problems
     are found.

If the property catalog is a class or interface, the values of the defined constants are the property keys. If the
property catalog is an enum, you can choose whether the enum constant itself shall be the key, or the first parameter
passed to the enum constant's constructor (via `enumArgument`). In a class, private constants are never considered.

This check find duplicate keys in the Java code, but not in the property file. Use [UniqueProperties]({{
site.link_cs_check_uniqueprops }}) for that. It also does not help keep the translations of your property files in sync
(e.g. file.properties, file_de.properties, file_fr.properties, etc.). Use [Translation]({{
site.link_cs_check_translation }}) for that, and configure this check only for one of the property files.


### Properties

<dl>
<dt><span class="propname">selection</span>
    <span class="proptype"><a href="{{ site.link_cs_type_regexp }}">regular
        expression</a></span></dt>
<dd><span class="propdesc">Select the Java files that are property catalogs. Each of these files must have a
        corresponding property file. The regular expression must match somewhere in the <i>binary name</i> of the
        type, so a property catalog may also be an inner class. Binary names are basically fully qualified class
        names; the simple names of inner classes are appended by <code>$</code> signs, for example
        <code>com.foo.Outer$Inner</code>.</span>
    <span class="propdefault"><code>^(?!x)x</code> (check is disabled)</span></dd>

<dt><span class="propname">excludedFields</span>
    <span class="proptype"><a href="{{ site.link_cs_type_regexp }}">regular
        expression</a></span></dt>
<dd><span class="propdesc">Regex that matches excluded fields which should not be considered part of the property
        catalog. Choose the regex so that it matches the entire field name. Keep in mind that
        <code>excludedFields</code> is applied to the name of the <i>constant</i>, not the <i>key</i>. With the
        exception of an enum constant used as key, constant and key are two separate things (see examples above).</span>
    <span class="propdefault"><code>serialVersionUID</code></span></dd>

<dt><span class="propname">enumArgument</span>
    <span class="proptype"><a href="{{ site.link_cs_type_boolean }}">Boolean</a></span></dt>
<dd><span class="propdesc">Determines whether the property key shall be the enum constant itself (<code>false</code>),
        or the first argument of the enum constant's constructor (<code>true</code>). When the property catalog is not
        an enum, then this property is ignored.</span>
    <span class="propdefault"><code>false</code></span></dd>

<dt><span class="propname">baseDir</span>
    <span class="proptype"><a href="{{ site.link_cs_type_string }}">String</a></span></dt>
<dd><span class="propdesc">Base directory to assume for the check execution, usually the project root</span>
    <span class="propdefault"><code>.</code></span></dd>

<dt><span class="propname">propertyFile</span>
    <span class="proptype"><a href="{{ site.link_cs_type_string }}">String</a></span></dt>
<dd><span class="propdesc">Template for the property file path. Relative paths are resolved against the
    <code>baseDir</code>. In this template, the following placeholders may be used (examples are for
    <code>com.foo.Bar$Inner</code>):</span>
    <dl class="inner"><dt><code>{0}</code></dt>
        <dd>the original binary class name (<code>com.foo.Bar$Inner</code>)</dd>
        <dt><code>{1}</code></dt>
        <dd>the binary class name as a path (<code>com/foo/Bar/Inner</code>)</dd>
        <dt><code>{2}</code></dt>
        <dd>fully qualified name of the outer class (<code>com.foo.Bar</code>)</dd>
        <dt><code>{3}</code></dt>
        <dd>fully qualified name of the outer class as a path (<code>com/foo/Bar</code>)</dd>
        <dt><code>{4}</code></dt>
        <dd>fully qualified name of the outer class as a path of <code>..</code>'s (<code>../../..</code>)</dd>
        <dt><code>{5}</code></dt>
        <dd>the package name as a path (<code>com/foo</code>)</dd>
        <dt><code>{6}</code></dt>
        <dd>simple name of the outer class (<code>Bar</code>)</dd>
        <dt><code>{7}</code></dt>
        <dd>simple name of the inner class (<code>Inner</code>)</dd>
        <dt><code>{8}</code></dt>
        <dd>simple name of the first subdirectory below the <code>baseDir</code> on the path to the message catalog
            (<code>subdir1</code>). This placeholder, as well as <code>{9}</code> and <code>{10}</code> are useful if
            your project being analyzed consists of submodules.</dd>
        <dt><code>{9}</code></dt>
        <dd>simple name of the next subdirectory on the path to the message catalog (<code>subdir2</code>)</dd>
        <dt><code>{10}</code></dt>
        <dd>simple name of the third subdirectory on the path to the message catalog (<code>subdir3</code>)</dd>
        <dt><code>{11}</code></dt>
        <dd>This placeholder is special because it is dynamic. It is replaced by the empty String, <code>{8}/</code>,
            <code>{8}/{9}/</code>, and <code>{8}/{9}/{10}/</code> (in that order). Once the property file is found, the
            location is used. If not, the next variation is checked. This is useful when the same Checkstyle
            configuration is used for multiple projects with different structures.</dd>
        <dt><code>{12}</code></dt>
        <dd>the relative path fragment between the <code>baseDir</code> and the package directories (e.g.
            <code>module1/src/main/java</code>)</dd>
    </dl>
    <span class="propdefault">(not set)</span></dd>

<dt><span class="propname">propertyFileEncoding</span>
    <span class="proptype"><a href="{{ site.link_cs_type_string }}">String</a></span></dt>
<dd><span class="propdesc">Character encoding of the property file</span>
    <span class="propdefault"><code>UTF-8</code></span></dd>

<dt><span class="propname">reportDuplicates</span>
    <span class="proptype"><a href="{{ site.link_cs_type_boolean }}">Boolean</a></span></dt>
<dd><span class="propdesc">Report if two code references point to the same property?</span>
    <span class="propdefault"><code>true</code></span></dd>

<dt><span class="propname">reportOrphans</span>
    <span class="proptype"><a href="{{ site.link_cs_type_boolean }}">Boolean</a></span></dt>
<dd><span class="propdesc">Report if property entries are not referenced in the code?</span>
    <span class="propdefault"><code>true</code></span></dd>

<dt><span class="propname">caseSensitive</span>
    <span class="proptype"><a href="{{ site.link_cs_type_boolean }}">Boolean</a></span></dt>
<dd><span class="propdesc">If <code>true</code>, the property keys are treated as case sensitive; if <code>false</code>,
        case is ignored.</span>
    <span class="propdefault"><code>true</code></span></dd>
    
<dt><span class="propname">fileExludes</span>
    <span class="proptype"><a href="{{ site.link_cs_type_regexp }}">regular expression</a></span></dt>
<dd><span class="propdesc">Files whose absolute path matches this regular expression are not checked.</span>
    <span class="propdefault"><code>[\\/]\.idea[\\/](?:checkstyleidea\.tmp[\\/])?csi-\w+[\\/]</code> (temp files of the
        Checkstyle plugin for IntelliJ IDEA)</span></dd>
</dl>


### Examples

In the following example, it is assumed that you have a naming convention which requires all property catalogs to have
a type name that ends in `Catalog`. The corresponding property file is assumed to share the name of the Java file and
reside in the same package, but under *src/main/resources*:

{% highlight xml %}
<module name="PropertyCatalog">
  <property name="selection" value="\wCatalog$"/>
  <property name="baseDir" value="${workspace_loc}"/>
  <property name="propertyFile" value="MyProject/src/main/resources/{1}.properties"/>
</module>
{% endhighlight %}

The above example is for Eclipse and IntelliJ, where `${workspace_loc}` may be used to
[refer](http://eclipse-cs.sourceforge.net/#!/properties) to the file system location of the current workspace. For
SonarQube, you may use relative file paths. For the other environments, you may define a custom [module property]({{
site.link_cs_properties }}), which you dynamically set to the project directory. Example for Gradle:

{% highlight groovy %}
checkstyle {
    configProperties 'workspace_loc': project.projectDir;
}
{% endhighlight %}


### Parent Module

[TreeWalker]({{ site.link_cs_treewalker }})
