---
layout: post
title: "Checkstyle 8.21 Compatibility"
date: 2019-05-27 22:01:00 +0100
release: false
emphasis: true
---

With [recent changes](https://github.com/checkstyle/checkstyle/issues/3417) in Checkstyle 8.21, the API for custom
checks was so thoroughly broken, that we can't immediately adjust. Stay tuned for a new major release.<!--break-->

We will have to wait for a bit until the Checkstyle components maintained by the core team have been adapted
(SonarQube integration and SevNTU Checks mostly). Then we can release a new major version ({{ site.name }} 6.0.0)
which will support Checkstyle 8.21 and above.

Also, we wait a little in order to see if maybe the change gets reverted, in which case all would be fine again.

Stay tuned, keep using Checkstyle 8.20 or earlier with {{ site.name }}, and please bear with us for a little while.
Thank you!
