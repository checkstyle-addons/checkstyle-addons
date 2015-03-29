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

  1. If file extensions are configured, it is checked if the file extension applies.
     As with all [FileSetChecks](http://checkstyle.sourceforge.net/writingchecks.html#Writing_FileSetChecks),
     this check only ever does anything if the file extension matches. Leave out the property to match all file
     extensions.
  2. If configured, the regular expression given in the `selection` property is
     applied to the canonical file name. Only files that match this expression
     are checked. Leave out the property to match all files.
  3. The given `regexp` is matched against the file name. What part of the file
     name it is applied to, and how the result is interpreted is governed by the check
     properties.

### Properties

<table border="1">
<tr>
    <th>name</th><th>description</th><th>type</th><th>default value</th>
</tr>
<tr>
<td>fileExtensions</td>
<td>Comma-separated list of file extensions. Leading dots are optional.
    Spaces after the commas are allowed. Only files with one of these
    extensions are checked against the regular expression.</td>
<td><a href="http://addons.sourceforge.net/property_types.html#stringSet">StringSet</a></td>
<td>unrestricted</td>
</tr>
<tr valign="top">
<td>selection</td>
<td>Limits the check to files whose canonical path name contains the given
    pattern. The canonical path is the simplest possible absolute path,
    including the file name (no '<tt>..</tt>' elements etc.).</td>
<td><a href="http://addons.sourceforge.net/property_types.html#regexp">regular
    expression</a></td>
<td>unrestricted</td>
</tr>
<tr valign="top">
<td>regexp</td>
<td>The regular expression applied to the file name.</td>
<td><a href="http://addons.sourceforge.net/property_types.html#regexp">regular
    expression</a></td>
<td><tt>^(?:\s+.*|.*?\s+)$</tt></td>
</tr>
<tr valign="top">
<td>mode</td>
<td>whether <tt>regexp</tt> finds required or illegal matches</td>
<td>{@link RegexpOnFilenameOption Mode}</td>
<td><code>illegal</code></td>
</tr>
<tr valign="top">
<td>simple</td>
<td>If <code>true</code>, only the simple name of the file will be checked
    against the pattern specified by <tt>regexp</tt>;
    if <code>false</code>, the entire canonical path will be checked.<br/>
    Note that this option applies only to the pattern specified by
<tt>regexp</tt>; the <tt>selection</tt> property is <i>always</i> treated
    as if <tt>simple=false</tt>.</td>
<td><a href="http://addons.sourceforge.net/property_types.html#boolean">Boolean</a></td>
<td><code>true</code></td>
</tr>
</table>

In addition to the properties, optionally adding a `message` element may benefit this check to make the warning easier
to understand. The message key depends on the value of the `mode` option. If `mode=required`, the message key
`regexp.filepath.required` is used. If `mode=illegal`, the message key `regexp.filepath.illegal` is used. The message
text can make use of placeholders `{0}` (the file name as used by the matcher) and `{1}` (the regular expression used
by the matcher).


### Examples

<script src="https://google-code-prettify.googlecode.com/svn/loader/run_prettify.js?lang=xml"></script>

To configure the check to detect leading and trailing spaces in file names:

<pre class="prettyprint">&lt;module name=&quot;RegexpOnFilename&quot;&gt;
 &lt;message key=&quot;regexp.filepath.illegal&quot; value=&quot;Filename ''{0}'' contains leading or trailing spaces.&quot;/&gt;
;/module&gt;</pre>

To configure the check to ensure that Java files reside in java folders, not resource folders:

<pre class="prettyprint">&lt;module name=&quot;RegexpOnFilename&quot;&gt;
 &lt;property name=&quot;fileExtensions&quot; value=&quot;java&quot;/&gt;
 &lt;property name=&quot;regexp&quot; value=&quot;[\\/]src[\\/](?:test|main)[\\/]java[\\/]&quot;/&gt;
 &lt;property name=&quot;mode&quot; value=&quot;required&quot;/&gt;
 &lt;property name=&quot;simple&quot; value=&quot;false&quot;/&gt;
 &lt;message key=&quot;regexp.filepath.required&quot; value=&quot;The Java file ''{0}'' must reside in a Java source folder.&quot;/&gt;
&lt;/module&gt;</pre>

To configure the check to enforce an HTML file naming convention on files in a certain folder:

<pre class="prettyprint">&lt;module name=&quot;RegexpOnFilename&quot;&gt;
 &lt;property name=&quot;fileExtensions&quot; value=&quot;html&quot;/&gt;
 &lt;property name=&quot;selection&quot; value=&quot;[\\/]src[\\/]main[\\/]resources[\\/]html[\\/]views[\\/]&quot;/&gt;
 &lt;property name=&quot;regexp&quot; value=&quot;^view_.*&quot;/&gt;
 &lt;property name=&quot;mode&quot; value=&quot;required&quot;/&gt;
 &lt;message key=&quot;regexp.filepath.required&quot; value=&quot;Name of ''{0}'' must start with ''view_''.&quot;/&gt;
&lt;/module&gt;</pre>

To configure the check to ban GIF files in favor of PNG:

```xml
<module name="RegexpOnFilename">
  <property name="fileExtensions" value="gif"/>
  <property name="regexp" value="."/>
  <message key="regexp.filepath.illegal" value="''{0}'' must be in PNG format, not GIF."/>
</module>
```

### Parent Module

[Checker](http://checkstyle.sourceforge.net/config.html#Checker)
