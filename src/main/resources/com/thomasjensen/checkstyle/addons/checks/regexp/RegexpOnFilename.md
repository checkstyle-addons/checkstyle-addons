## RegexpOnFilename

This check applies a regular expression to the names of files.
Depending on the configuration, a warning is logged if a required match is not found, or if an illegal match is found.

This is useful for situations such as:

  - Checking that resources in certain directories follow a naming convention
  - File names contain only legal characters
  - Files of certain types are created in the right places, e.g. Java files under *src/&#42;/java*
  - Prevent certain files or types of files altogether, by "banning" their names
  - Ensure that required files are present in certain locations

By default, this check flags leading and trailing spaces in file names.

The check works like this:

  1. Select the files to check. Only files which match the regular expression given in `selection` are checked. The `selection` regexp is applied to the canonical file name, which included the entire path. Leave blank to include all files.
  2. The expression given in `regexp` is matched against each selected file name. What part of the file name it is applied to, and how the result is interpreted is governed by the check properties.


### Properties

<dl>
<dt><span class="propname">selection</span>
    <span class="proptype"><a href="http://checkstyle.sourceforge.net/property_types.html#regexp">regular expression</a></span></dt>
<dd><span class="propdesc">Limits the check to files whose canonical path name contains the given pattern. The canonical path is the simplest possible absolute path, including the file name (no <code>..</code> elements etc.).</span>
    <span class="propdefault">unrestricted</span></dd>

<dt><span class="propname">regexp</span>
    <span class="proptype"><a href="http://checkstyle.sourceforge.net/property_types.html#regexp">regular expression</a></span></dt>
<dd><span class="propdesc">The regular expression applied to the file name.</span>
    <span class="propdefault"><tt>^(?:\s+.*|.*?\s+)$</tt></span></dd>

<dt><span class="propname">mode</span>
    <span class="proptype"><a href="{{ site.baseurl }}/latest/apidocs/index.html?com/thomasjensen/checkstyle/addons/checks/regexp/RegexpOnFilenameOption.html">Mode</a></span></dt>
<dd><span class="propdesc">whether <tt>regexp</tt> finds required or illegal matches</span>
    <span class="propdefault"><tt>illegal</tt></span></dd>

<dt><span class="propname">simple</span>
    <span class="proptype"><a href="http://checkstyle.sourceforge.net/property_types.html#boolean">Boolean</a></span></dt>
<dd><span class="propdesc">If <code>true</code>, only the simple name of the file will be checked against the pattern specified by <tt>regexp</tt>; if <code>false</code>, the entire canonical path will be checked.
    Note that this option applies only to the pattern specified by <tt>regexp</tt>; the <tt>selection</tt> property is <i>always</i> treated as if <tt>simple=false</tt>.</span>
    <span class="propdefault"><tt>true</tt></span></dd>
</dl>

Since this check is a [FileSetCheck](http://checkstyle.sourceforge.net/writingchecks.html#Writing_FileSetChecks), it inherits the `fileExtensions` property, which, if configured, would take precedence over `selection`.

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

To configure the check to enforce a naming convention on files in a certain folder:

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

<div class="alert alert-dismissible alert-warning">
  <button type="button" class="close" data-dismiss="alert">×</button>
  <h4>Important: This check goes under <b>Checker</b>, not <b>TreeWalker</b>.</h4>
  <p>Placing the <i>checkstyle.xml</i> module definition incorrectly is a common mistake.</p>
</div>

[Checker](http://checkstyle.sourceforge.net/config.html#Checker)
