## RegexpOnFilename

This check applies a given regular expression to the names of files.
Depending on the configuration, a warning is logged if a required match is not found, or if an illegal match is found.

This is useful for situations such as:

  - Checking that resources in certain directories follow a naming convention
  - Resource file names contain only legal characters
  - Files of certain types are created in the right places, e.g. Java files under *src/&#42;/java*
  - Prevent certain files or types of files altogether, by "banning" their names

By default, this check flags leading and trailing spaces in file names.

The check works like this:

  1. If file extensions are configured, it is checked if the file extension applies. As with all [FileSetChecks]({{ site.link_cs_filesetcheck }}), this check only ever does anything if the file extension matches. Leave out the property to match all file extensions.
  2. If configured, the regular expression given in the `selection` property is applied to the canonical file name. Only files that match this expression are checked. Leave out the property to match all files.
  3. The given `regexp` is matched against the file name. What part of the file name it is applied to, and how the result is interpreted is governed by the check properties.


### Properties

<dl>
<dt><span class="propname">fileExtensions</span>
    <span class="proptype"><a href="{{ site.link_cs_type_stringset }}">StringSet</a></span></dt>
<dd><span class="propdesc">Comma-separated list of file extensions. Leading dots are optional. Spaces after the commas are allowed. Only files with one of these extensions are checked against the regular expression.</span>
    <span class="propdefault">unrestricted</span></dd>

<dt><span class="propname">selection</span>
    <span class="proptype"><a href="{{ site.link_cs_type_regexp }}">regular expression</a></span></dt>
<dd><span class="propdesc">Limits the check to files whose canonical path name contains the given pattern. The canonical path is the simplest possible absolute path, including the file name (no <code>..</code> elements etc.).</span>
    <span class="propdefault">unrestricted</span></dd>

<dt><span class="propname">regexp</span>
    <span class="proptype"><a href="{{ site.link_cs_type_regexp }}">regular expression</a></span></dt>
<dd><span class="propdesc">The regular expression applied to the file name.</span>
    <span class="propdefault"><tt>^(?:\s+.*|.*?\s+)$</tt></span></dd>

<dt><span class="propname">mode</span>
    <span class="proptype"><a href="{{ site.baseurl }}/latest/apidocs/index.html?com/thomasjensen/checkstyle/addons/checks/regexp/RegexpOnFilenameOption.html">Mode</a></span></dt>
<dd><span class="propdesc">whether <tt>regexp</tt> finds required or illegal matches</span>
    <span class="propdefault"><tt>illegal</tt></span></dd>

<dt><span class="propname">simple</span>
    <span class="proptype"><a href="{{ site.link_cs_type_boolean }}">Boolean</a></span></dt>
<dd><span class="propdesc">If <code>true</code>, only the simple name of the file will be checked against the pattern specified by <tt>regexp</tt>; if <code>false</code>, the entire canonical path will be checked.
    Note that this option applies only to the pattern specified by <tt>regexp</tt>; the <tt>selection</tt> property is <i>always</i> treated as if <tt>simple=false</tt>.</span>
    <span class="propdefault"><tt>true</tt></span></dd>
</dl>

In addition to the properties, optionally adding a `message` element may benefit this check to make the warning easier to understand. The message key depends on the value of the `mode` option. If `mode=required`, the message key `regexp.filepath.required` is used. If `mode=illegal`, the message key `regexp.filepath.illegal` is used. The message text can make use of placeholders `{0}` (the file name as used by the matcher) and `{1}` (the regular expression used by the matcher).


### Examples

To configure the check to detect leading and trailing spaces in file names:

```xml
<module name="RegexpOnFilename">
  <message key="regexp.filepath.illegal" value="Filename ''{0}'' contains leading or trailing spaces."/>
</module>
```

To configure the check to ensure that Java files reside in java folders, not resource folders:

```xml
<module name="RegexpOnFilename">
  <property name="fileExtensions" value="java"/>
  <property name="regexp" value="[\\/]src[\\/](?:test|main)[\\/]java[\\/]"/>
  <property name="mode" value="required"/>
  <property name="simple" value="false"/>
  <message key="regexp.filepath.required"
      value="The Java file ''{0}'' must reside in a Java source folder."/>
</module>
```

To configure the check to enforce an HTML file naming convention on files in a certain folder:

```xml
<module name="RegexpOnFilename">
  <property name="fileExtensions" value="html"/>
  <property name="selection" value="[\\/]src[\\/]main[\\/]resources[\\/]html[\\/]views[\\/]"/>
  <property name="regexp" value="^view_.*"/>
  <property name="mode" value="required"/>
  <message key="regexp.filepath.required" value="Name of ''{0}'' must start with ''view_''."/>
</module>
```

To configure the check to ban GIF files in favor of PNG:

```xml
<module name="RegexpOnFilename">
  <property name="fileExtensions" value="gif"/>
  <property name="regexp" value="."/>
  <message key="regexp.filepath.illegal" value="''{0}'' must be in PNG format, not GIF."/>
</module>
```


### Parent Module

[Checker]({{ site.link_cs_checker }})
