## IllegalMethodCall

Flags calls to methods with certain names. Occurrences are flagged based on the name alone; the type of the object to which the method belongs is not taken into account.

This is a low-tech mechanism for certain types of code governance, such as preventing use of reflection through `Class.forName()` or `Constructor.newInstance()`.
The scattergun approach used by this check may get you some false positives, which may have to be suppressed. 


### Properties

This check must be configured explicitly for certain method names; it does nothing by default.

<dl>
<dt><span class="propname">illegalMethodNames</span>
    <span class="proptype"><a href="{{ site.link_cs_type_stringset }}">StringSet</a></span></dt>
<dd><span class="propdesc">Comma-separated list of plain method names, no parameters, no parentheses</span>
    <span class="propdefault">none</span></dd>
</dl>

#### Custom Messages

In addition to the properties, optionally adding a `message` element may benefit this check to make the warning easier to understand. The message key is `illegal.method.call`, and it features one optional placeholder (`{0}`), which is the name of the flagged method. The placeholder is useful when the list of illegal method names contains more than 1 entry.


### Examples

Configure the check like this:

{% highlight xml %}
<module name="IllegalMethodCall">
  <property name="illegalMethodNames" value="forName, newInstance"/>
</module>
{% endhighlight %}

Example using a custom message:

{% highlight xml %}
<module name="IllegalMethodCall">
  <property name="illegalMethodNames" value="finalize"/>
  <message key="illegal.method.call" value="Finalizer called explicitly"/>
</module>
{% endhighlight %}


### Parent Module

[TreeWalker]({{ site.link_cs_treewalker }})
