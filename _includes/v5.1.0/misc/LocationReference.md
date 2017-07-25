## LocationReference

The *LocationReference* check helps in cases where the name of the current method or class must be used as an argument to a method call or as initial value of a declared variable. It compares the actual argument to the current method or class name, and flags it if a mismatch is detected.


### Properties

<dl>
<dt><span class="propname">methodCalls</span>
    <span class="proptype"><a href="{{ site.link_cs_type_stringset }}">StringSet</a></span></dt>
<dd><span class="propdesc">Comma-separated list of method calls that should be covered by this check. Each element of
        the list must be the full method call as it occurs in the source file, ignoring whitespace and parentheses.
        For example, <code>LogManager.getLogger</code>. This property or <code>variableNames</code> must be set for
        this check to do anything.</span>
    <span class="propdefault">none (check is disabled)</span></dd>

<dt><span class="propname">variableNames</span>
    <span class="proptype"><a href="{{ site.link_cs_type_stringset }}">StringSet</a></span></dt>
<dd><span class="propdesc">Comma-separated list of variable names whose declarations should be covered by this check.
        Each element of the list is a variable name as it occurs in the source file, For example,
        <code>method</code>. If a variable of one of the names specified by this property is declared anywhere,
        and its initial value is a <code>String</code> or <code>Class</code> literal, that literal is checked. This
        property or <code>methodCalls</code> must be set for this check to do anything.</span>
    <span class="propdefault">none (check is disabled)</span></dd>

<dt><span class="propname">location</span>
    <span class="proptype"><a href="{{ site.baseurl }}/{{ page.check_version }}/apidocs/index.html?com/thomasjensen/checkstyle/addons/checks/misc/LocationReferenceOption.html">Location</a></span></dt>
<dd><span class="propdesc">The location information expected here. If set to <code>method</code>, the value is
        expected to be the exact name of the current method, or <code>&lt;init&gt;</code> in constructors, and
        <code>&lt;clinit&gt;</code> in static initializers.</span>
    <span class="propdefault"><code>method</code></span></dd>

<dt><span class="propname">argumentPosition</span>
    <span class="proptype"><a href="{{ site.link_cs_type_integer }}">Integer</a></span></dt>
<dd><span class="propdesc">The position of the location reference as an index of the list of arguments of a method
        call, starting at zero. Negative values count from the end of the list, <code>-1</code> being the last argument
        in the list.</span>
    <span class="propdefault"><code>0</code></span></dd>
</dl>


### Examples

The following example checks that the current method name is passed as the first argument to certain log methods, or given as a String literal in initializations of variables named `method`:

{% highlight xml %}
<module name="LocationReference">
  <property name="methodCalls" value="LOG.debug, LOG.enter, LOG.exit"/>
  <property name="variableNames" value="method"/>
</module>
{% endhighlight %}

In the next example, the `getLogger` method takes the current class as its first argument. The class shall be specified as a simple class object (e.g. `MyClass.class`). Just `getClass()` would not work. Calls where the argument is not a class object are ignored. Note the optional custom message, which allows tailoring the violation text to the particular use case:

{% highlight xml %}
<module name="LocationReference">
  <property name="methodCalls" value="LogManager.getLogger"/>
  <property name="location" value="classobject"/>
  <message key="locationreference.mismatch.classobject"
      value="Logger definition must reference the current class object, which is ''{0}.class''"/>
</module>
{% endhighlight %}


### Parent Module

[TreeWalker]({{ site.link_cs_treewalker }})
