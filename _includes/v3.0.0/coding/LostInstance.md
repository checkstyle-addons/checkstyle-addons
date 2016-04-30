## LostInstance

Checks that object instances created explicitly with `new` are actually used for something. Just being assigned to a variable or passed as a parameter is enough. A full data flow analysis is not performed.

This helps discover cases like the following:

{% highlight java %}
if (x < 0)
    new IllegalArgumentException("x must be nonnegative");
{% endhighlight %}

It was probably the intent of the programmer to *throw* the created exception:

{% highlight java %}
if (x < 0)
    throw new IllegalArgumentException("x must be nonnegative");
{% endhighlight %}

The instance might have been created in order to make use of a constructor side effect, but such a case would be a bug in its own right.

This check was inspired by the FindBugs detector [RV_EXCEPTION_NOT_THROWN](http://findbugs.sourceforge.net/bugDescriptions.html#RV_EXCEPTION_NOT_THROWN). However, this check is not restricted to exceptions.


### Properties

None.


### Examples

Configure the check like this:

{% highlight xml %}
<module name="LostInstance"/>
{% endhighlight %}
 

### Parent Module

[TreeWalker]({{ site.link_cs_treewalker }})
