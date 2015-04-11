## LostInstance

Checks that object instances created explicitly with `new` are actually used for something. Just being
assigned to a variable or passed as a parameter is enough. A full data flow analysis is not performed.

This helps discover cases like the following:

    if (x &lt; 0)
        new IllegalArgumentException("x must be nonnegative");

It was probably the intent of the programmer to *throw* the created exception:

    if (x &lt; 0)
        throw new IllegalArgumentException("x must be nonnegative");

The instance might have been created to make use of a constructor side effect, but such a case would be a bug in its own right.

Inspired by FindBugs, http://findbugs.sourceforge.net/publications.html


### Properties

None.


### Examples

Configure the check like this:

```xml
<module name="LostInstance"/>
```
 

### Parent Module

[TreeWalker](http://checkstyle.sourceforge.net/config.html#TreeWalker)
