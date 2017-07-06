package com.thomasjensen.checkstyle.addons.checks.misc;
/*
 * Checkstyle-Addons - Additional Checkstyle checks
 * Copyright (C) 2015 Thomas Jensen
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 3, as published by the Free
 * Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program.  If not, see <http://www.gnu.org/licenses/>.
 */

import javax.annotation.Nonnull;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import net.jcip.annotations.Immutable;


/**
 * Stores a syntactically valid property catalog entry extracted from the source file. Instances of this type are always
 * case sensitive.
 *
 * @see PropertyCatalogCheck
 */
@Immutable
final class CatalogEntry
    implements Comparable<CatalogEntry>
{
    /** the name of the constant in the Java file */
    private final String constantName;

    /** the key that should appear in the property file */
    private final String key;

    /** AST to highlight if anything is wrong with this entry */
    private final DetailAST ast;



    /**
     * Constructor.
     *
     * @param pConstantName the name of the constant in the Java file
     * @param pKey the key that should appear in the property file
     * @param pAst AST to highlight if anything is wrong with this entry
     */
    public CatalogEntry(@Nonnull final String pConstantName, @Nonnull final String pKey, @Nonnull final DetailAST pAst)
    {
        constantName = pConstantName;
        key = pKey;
        ast = pAst;
    }



    @Override
    public boolean equals(final Object pOther)
    {
        if (this == pOther) {
            return true;
        }
        if (pOther == null || getClass() != pOther.getClass()) {
            return false;
        }

        CatalogEntry other = (CatalogEntry) pOther;
        return compareTo(other) == 0;
    }



    @Override
    public int hashCode()
    {
        int result = ast.getLineNo();
        result = 31 * result + constantName.hashCode();
        return result;
    }



    @Override
    public int compareTo(@Nonnull final CatalogEntry pOther)
    {
        int result = Integer.valueOf(ast.getLineNo()).compareTo(Integer.valueOf(pOther.getAst().getLineNo()));
        if (result == 0) {
            result = constantName.compareTo(pOther.getConstantName());
        }
        return result;
    }



    @Nonnull
    public String getConstantName()
    {
        return constantName;
    }



    @Nonnull
    public String getKey()
    {
        return key;
    }



    @Nonnull
    public DetailAST getAst()
    {
        return ast;
    }



    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append("CatalogEntry{");
        sb.append(ast.getLineNo());
        sb.append(':');
        sb.append(ast.getColumnNo());
        sb.append(", key=\"");
        sb.append(key).append('\"');
        sb.append(", constantName=\"").append(constantName).append('\"');
        sb.append('}');
        return sb.toString();
    }
}
