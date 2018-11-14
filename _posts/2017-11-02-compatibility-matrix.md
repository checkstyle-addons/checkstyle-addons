---
layout: post
title: "Cell highlighting for Checkstyle compatibility matrix"
date: 2017-11-02 22:14:00 +0100
release: false
emphasis: false
---

The [compatibility matrix]({{ site.baseurl }}/checkstyle-compatibility-matrix.html) of the Checkstyle ecosystem
has been improved and now offers a highlight feature to help you point out specific columns, rows, or
cells.<!--break-->

You may now append a query parameter `hl` to the compatibility matrix URL to specify the cells to highlight,
for example:

    /checkstyle-compatibility-matrix.html?hl=8.4

The above would highlight the row about Checkstyle 8.4.

A more complex example would be this one:

    /checkstyle-compatibility-matrix.html?hl=7.8.2-7.7[g];c-d

resulting in multiple rows and columns to be highlighted in differing colors:

<div class="csa-largeImageWrapper">
<img src="{{ site.baseurl }}/images/comp-matrix-hilite.png" alt="Highlight example" width="611" height="337" />
</div>

{:.table .table-striped .csa-bordered-table}
| Type   | Syntax  | Range       |
|--------|---------|-------------|
| Row    | `8.4`   | `8.4-8.0`   |
| Column | `C`     | `C-G`       |
| Cell   | `C8.4`  | `C8.4-G8.0` |
| Multi  | `C;8.4` | `C;8.4-8.0` |

The type *Multi* allows a semicolon-separated list of any of the above (row, column, or cell; either single or ranged).

When specifying a range, take care that

- both elements of the range are of the same type (e.g. `C-G` is good, `8.4-G` is mixed up), and
- the element that comes first in the table comes first in the range (e.g. `C-G` or `8.4-8.0` are good, but `G-C` or `8.0-8.4` are wrong).

The color of each row, column, or cell (range) may be selected by appending the color in square brackets.

{:.table .table-striped .csa-bordered-table .csa-hilite-colors}
| Indicator | Color  | Precedence  |
|-----------|--------|-------------|
| `[R]`     | red    | 1           |
| `[Y]`     | yellow | 2 (default) |
| `[B]`     | blue   | 3           |
| `[G]`     | green  | 4           |

Yellow is also the default, when no color is given. When areas with different colors overlap, the higher-precendence color is used on the overlap.
