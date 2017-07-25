## RegexpOnFilenameOrg

This check applies a regular expression to the names of files.
Depending on the configuration, a warning is logged if a required match is not found, or if an illegal match is found.

<div class="alert alert-info">
  <p>The Checkstyle team liked this check so much that they added it to the core Checkstyle product. We feel
  honored! Sadly, even though {{ site.name }} is admittedly their inspiration, and we published 10 months ahead of
  them, the fact goes unmentioned.
  <img class="emoji" width="20" height="20" align="absmiddle"
       src="https://assets.github.com/images/icons/emoji/unicode/1f44e.png" alt=":-1:" title=":-1:"><br/>
  In order to avoid name collisions with our original, we had to rename this check from <i>RegexpOnFilename</i> to
  <i>RegexpOnFilenameOrg</i>.</p>
</div>

This check is useful for situations such as:

  - Checking that resources in certain directories follow a naming convention
  - File names contain only legal characters
  - Files of certain types are created in the right places, e.g. Java files under *src/&#42;/java*
  - Prevent certain files or types of files altogether, by "banning" their names

By default, this check flags leading and trailing spaces in file names.

The check works like this:

  1. Select the files to check. Only files which match the regular expression given in `selection` are checked. Leave blank to include all files.
  2. The expression given in `regexp` is matched against each selected file name. What part of the file name it is applied to, and how the result is interpreted is governed by the check properties.


### Properties

<dl>
<dt><span class="propname">selection</span>
    <span class="proptype"><a href="{{ site.link_cs_type_regexp }}">regular expression</a></span></dt>
<dd><span class="propdesc">Limits the check to files whose canonical path name contains the given pattern. The canonical path is the simplest possible absolute path, including the file name (no <code>..</code> elements etc.).</span>
    <span class="propdefault">unrestricted</span></dd>

<dt><span class="propname">regexp</span>
    <span class="proptype"><a href="{{ site.link_cs_type_regexp }}">regular expression</a></span></dt>
<dd><span class="propdesc">The regular expression applied to the file name.</span>
    <span class="propdefault"><code>^(?:\s+.*|.*?\s+)$</code></span></dd>

<dt><span class="propname">mode</span>
    <span class="proptype"><a href="{{ site.baseurl }}/{{ page.check_version }}/apidocs/index.html?com/thomasjensen/checkstyle/addons/checks/regexp/RegexpOnFilenameOrgOption.html">Mode</a></span></dt>
<dd><span class="propdesc">whether <code>regexp</code> finds required or illegal matches. <code>required</code> means that all selected files must match the expression. <code>illegal</code> means that they must not.</span>
    <span class="propdefault"><code>illegal</code></span></dd>

<dt><span class="propname">simple</span>
    <span class="proptype"><a href="{{ site.link_cs_type_boolean }}">Boolean</a></span></dt>
<dd><span class="propdesc">If <code>true</code>, only the simple name of the file will be checked against the pattern specified by <code>regexp</code>; if <code>false</code>, the entire canonical path will be checked.
    Note that this option applies only to the pattern specified by <code>regexp</code>; the <code>selection</code> property is <i>always</i> treated as if <code>simple=false</code>.</span>
    <span class="propdefault"><code>true</code></span></dd>
</dl>

Since this check is a [FileSetCheck]({{ site.link_cs_filesetcheck }}), it also inherits the `fileExtensions` property, which may be configured independently of `selection`. In that case, both properties must match (e.g. `fileExtensions` *and* `selection`, or either of the two if one is missing).

#### Custom Messages

In addition to the properties, optionally adding a `message` element may benefit this check to make the warning easier to understand. The message key depends on the value of the `mode` option. If `mode=required`, the message key `regexp.filepath.required` is used. If `mode=illegal`, the message key `regexp.filepath.illegal` is used. The message text can make use of placeholders `{0}` (the file name as used by the matcher) and `{1}` (the regular expression used by the matcher).


### Examples

By default, the check detects leading and trailing spaces in file names. It is recommended to still add a custom message as shown, but that's optional.

{% highlight xml %}
<module name="RegexpOnFilenameOrg">
  <message key="regexp.filepath.illegal" value="Filename ''{0}'' contains leading or trailing spaces."/>
</module>
{% endhighlight %}

To configure the check to ensure that Java files reside in Java source folders, not resource folders:

{% highlight xml %}
<module name="RegexpOnFilenameOrg">
  <property name="selection" value="\.java$"/>
  <property name="regexp" value="[\\/]src[\\/](?:test|main)[\\/]java[\\/]"/>
  <property name="mode" value="required"/>
  <property name="simple" value="false"/>
  <message key="regexp.filepath.required"
      value="The Java file ''{0}'' must reside in a Java source folder."/>
</module>
{% endhighlight %}

This check is also useful to enforce arbitrary naming conventions. In the following example, we require all HTML files in a folder *html/view* to start with the prefix `view_`:

{% highlight xml %}
<module name="RegexpOnFilenameOrg">
  <property name="selection" value="[\\/]src[\\/]main[\\/]resources[\\/]html[\\/]views[\\/].+?\.html$"/>
  <property name="regexp" value="^view_.*"/>
  <property name="mode" value="required"/>
  <message key="regexp.filepath.required" value="Name of ''{0}'' must start with ''view_''."/>
</module>
{% endhighlight %}

To configure the check to ban GIF files in favor of PNG:

{% highlight xml %}
<module name="RegexpOnFilenameOrg">
  <property name="selection" value="(?i)\.gif$"/>
  <property name="regexp" value="."/>
  <message key="regexp.filepath.illegal" value="''{0}'' must be in PNG format, not GIF."/>
</module>
{% endhighlight %}

The `(?i)` at the start of the `selection` expression turns on case insensitivity, so that `.gif`, `.GIF`, or even `.Gif` are all matched.


### Parent Module

<div class="alert alert-info">
  <p>Important: This check goes directly under <b>Checker</b>, not under <b>TreeWalker</b>.</p>
</div>

[Checker]({{ site.link_cs_checker }})
