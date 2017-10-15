/*
 * Checkstyle Addons
 * (c) 2017 Checkstyle Addons contributors
 * Compatibility Matrix highlighting
 */

var MAX_COL = 'q';   // currently the right-most column; cannot go beyond 'z'
var MAX_HILITE_CELLS = (getOffsetFromColumn(MAX_COL) + 1) * 18 - 1;   // highlight at most this many cells, mark the last one

function main() {
    var urlParam = getHiliteSpec();
    if (urlParam === undefined || urlParam === null) {
        // nothing to highlight
        return;
    }
    var specs = urlParam.split(/\s*;\s*/);
    for (var i = 0; i < specs.length; i++) {
        csaHighlight(specs[i]);
    }
}


function getHiliteSpec() {
    var searchParams = new URLSearchParams(window.location.search);
    return searchParams.get("hl");
}


function csaHighlight(pRawSpec) {
    var spec = parseHiliteSpec(pRawSpec);
    if (spec === null) {
        return;  // invalid hilite spec
    }

    var selected = [];
    if (isCell(spec.spec1)) {
        // A8.3 or A8.3-D8.0
        selected = findElemsForCellSpec(spec);
    }
    else if (isColumn(spec.spec1)) {
        // A or A-D
        selected = findElemsForColumnSpec(spec);
    }
    else {
        // 8.3 or 8.3-8.0
        selected = findElemsForRowSpec(spec);
    }
    
    colorizeElems(selected, spec.css);
}


function parseHiliteSpec(str) {
    var single = '([a-' + MAX_COL + '](?:[0-9\\.]+)?|(?:[a-' + MAX_COL + '])?[0-9\\.]+)';
    var regex = new RegExp('^' + single + '(?:-' + single + ')?(?:\\[(.)\\])?$', 'i');
    var match = regex.exec(str);
    var result = null;
    if (match !== undefined && match !== null) {
        result = {};
        result.spec1 = match[1];
        result.spec2 = match[2];
        result.css = getCssClass(match[3]);
        result.isRange = result.spec2 !== undefined && result.spec2 !== null;
        if (!validRange(result)) {
            result = null;
        }
        result.hasMultipleRows = result.isRange && (result.spec1.substring(1) !== result.spec2.substring(1));
    }
    return result;
}


function getCssClass(letter) {
    var result = 'warning';
    if (typeof(letter) !== 'undefined' && letter != null) {
        switch (letter.toLowerCase()) {
            case 'b': result = 'info';    break;
            case 'g': result = 'success'; break;
            case 'r': result = 'danger';  break;
            default:  result = 'warning';
        }
    }
    return result;
}


function validRange(spec) {
    var result = true;
    if (spec.isRange) {
        result = (isRow(spec.spec1) && isRow(spec.spec2))
            || (isColumn(spec.spec1) && isColumn(spec.spec2))
            || (isCell(spec.spec1) && isCell(spec.spec2));
    }
    return result;
}


function isColumn(str) {
    var result = false;
    if (str.length === 1) {
        var regex = new RegExp('[a-' + MAX_COL + ']', 'i');
        result = regex.test(str);
    }
    return result;
}

function isRow(str) {
    return str.length > 2 && str.match(/[0-9\.]+/);
}

function isCell(str) {
    var result = false;
    if (str.length > 3) {
        var regex = new RegExp('^[a-' + MAX_COL + '][0-9\\.]+', 'i');
        result = regex.test(str);
    }
    return result;
}


// Find elements to highlight for a cell spec such as "A8.3" (single cell) or "A8.3-D8.0" (2D range)
function findElemsForCellSpec(spec) {
    var inRange = false;
    var foundSpec2 = false;
    var rowHeaders = $('table.csa-hilite .row-header');
    var ofs1 = getOffsetFromColumn(spec.spec1);
    var ofs2 = spec.isRange ? getOffsetFromColumn(spec.spec2) : ofs1;
    var selected = [];
    outer:
    for (var i = 0; i < rowHeaders.length; i++) {
        if (inRange) {
            for (var c = ofs1; c <= ofs2; c++) {
                if (!boundedPush(selected, rowHeaders[i].parentNode.children[c])) {
                    break outer;
                }
            }
            if (spec.spec2.substring(1) == rowHeaders[i].innerHTML) {
                foundSpec2 = true;
                break;
            }
        }
        else if (spec.spec1.substring(1) == rowHeaders[i].innerHTML) {
            for (var c = ofs1; c <= ofs2; c++) {
                if (!boundedPush(selected, rowHeaders[i].parentNode.children[c])) {
                    break outer;
                }
            }
            if (!spec.hasMultipleRows) {
                foundSpec2 = true;
                break;
            }
            inRange = true;
        }
    }
    if (spec.isRange && !foundSpec2) {
        selected = [];  // invalid range
    }
    return selected;
}


// Find elements to highlight for a column spec such as "A" (single column) or "A-D" (range)
function findElemsForColumnSpec(spec) {
    var ofs1 = getOffsetFromColumn(spec.spec1);
    var ofs2 = spec.isRange ? getOffsetFromColumn(spec.spec2) : ofs1;
    var rows = $('table.csa-hilite tr:not(:first)');
    var selected = [];
    outer:
    for (var i = 0; i < rows.length; i++) {
        for (var c = ofs1; c <= ofs2; c++) {
            if (!boundedPush(selected, rows[i].children[c])) {
                break outer;
            }
        }
    }
    return selected;
}


// Find elements to highlight for a row spec such as "8.3" (single row) or "8.3-8.0" (range)
function findElemsForRowSpec(spec) {
    var inRange = false;
    var foundSpec2 = false;
    var rowHeaders = $('table.csa-hilite .row-header');
    var selected = [];
    for (var i = 0; i < rowHeaders.length; i++) {
        if (inRange) {
            if (!boundedPush(selected, rowHeaders[i].parentNode)) {
                break;
            }
            if (spec.spec2 == rowHeaders[i].innerHTML) {
                foundSpec2 = true;
                break;
            }
        }
        else if (spec.spec1 == rowHeaders[i].innerHTML) {
            if (!boundedPush(selected, rowHeaders[i].parentNode)) {
                break;
            }
            if (!spec.isRange) {
                break;
            }
            inRange = true;
        }
    }
    if (spec.isRange && !foundSpec2) {
        selected = [];  // invalid range
    }
    return selected;
}


function getOffsetFromColumn(col) {
    var c = col.toLowerCase();
    return c.charCodeAt(0) - 'a'.charCodeAt(0);
}


function boundedPush(selected, elem) {
    var spaceLeft = false;
    if (selected.length <= MAX_HILITE_CELLS) {   // allow 1 more
        selected.push(elem);
        spaceLeft = true;
    }
    return spaceLeft;
}


function colorizeElems(selectedElems, cssClass) {
    for (var i = 0; i < selectedElems.length; i++) {
        if (i < MAX_HILITE_CELLS) {
            setCssClass(selectedElems[i], cssClass);
        } else {
            // mark last cell indicating that highlighting was aborted due to length
            setCssClass(selectedElems[i], 'csa-hilite-exceeded');
        }
    }
}


function setCssClass(node, cssClass) {
    if (!node.classList.contains(cssClass)) {
        node.classList.add(cssClass);
    }
}


$(document).ready(main());
