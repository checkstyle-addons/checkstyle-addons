---
layout: post
title: "Compatibility is key"
date: 2018-08-25 14:35:00 +0200
release: false
emphasis: false
---

Did you know that we check every single one of the frequent Checkstyle releases for compatibility with
{{ site.name }}?<!--break-->  
You can check for yourself in our commit history by searching for `Ensure support of Checkstyle`, a phrase which 
is present in the relevant commits
([link](https://github.com/{{ site.github }}/search?q=Ensure+support+of+Checkstyle&type=Commits)).  

Normally, {{ site.name }} is compatible with the new Checkstyle version, so you'd have no problems even if there
was no commit message for it. But the commit adds it to the automatic build, so that when changes are made in the
future, we can be sure that the version stays supported. The automatic build actually runs all unit tests against
every supported version of Checkstyle
([example on Travis CI](https://travis-ci.org/checkstyle-addons/checkstyle-addons/jobs/415118474#L1131)). Since we
have [99% test coverage](https://coveralls.io/github/{{ site.github }}), this makes us very confident about
compatibility.

Just wanted to let you know! {{ site.name }} is stable and not much is happening in terms of new development
at the moment. But what's there can be relied upon.