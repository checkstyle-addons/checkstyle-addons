package com.thomasjensen.checkstyle.addons.checks;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.jcip.annotations.Immutable;


/**
 * Represents a Java binary class name for reference types, in the form of its fragments. This is the only way to tell
 * the difference between a class called <code>A$B</code> and a class called <code>A</code> that has an inner class
 * <code>B</code>.
 */
@Immutable
public final class BinaryName
{
    private final String pkg;

    private final List<String> cls;



    /**
     * Constructor.
     *
     * @param pPkg package name
     * @param pOuterCls outer class simple name
     * @param pInnerCls inner class simple names in descending order of their nesting
     */
    public BinaryName(@Nullable final String pPkg, @Nonnull final String pOuterCls, @Nullable final String... pInnerCls)
    {
        pkg = pPkg;

        List<String> nameList = new ArrayList<String>();
        if (pOuterCls != null) {
            nameList.add(pOuterCls);
        }
        else {
            throw new IllegalArgumentException("pOuterCls was null");
        }
        if (pInnerCls != null) {
            for (final String inner : pInnerCls) {
                nameList.add(inner);
            }
        }
        cls = Collections.unmodifiableList(nameList);
    }



    /**
     * Constructor.
     *
     * @param pPkg package name
     * @param pClsNames class simple names in descending order of their nesting
     */
    public BinaryName(@Nullable final String pPkg, @Nonnull final Collection<String> pClsNames)
    {
        pkg = pPkg;
        if (pClsNames.size() == 0) {
            throw new IllegalArgumentException("pClsNames is empty");
        }
        cls = Collections.unmodifiableList(new ArrayList<String>(pClsNames));
    }



    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        if (pkg != null) {
            sb.append(pkg);
            sb.append('.');
        }
        for (final Iterator<String> iter = cls.iterator(); iter.hasNext();) {
            sb.append(iter.next());
            if (iter.hasNext()) {
                sb.append('$');
            }
        }
        return sb.toString();
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

        BinaryName other = (BinaryName) pOther;

        if (pkg != null ? !pkg.equals(other.pkg) : other.pkg != null) {
            return false;
        }
        if (!cls.equals(other.cls)) {
            return false;
        }

        return true;
    }



    @Override
    public int hashCode()
    {
        int result = pkg != null ? pkg.hashCode() : 0;
        result = 31 * result + cls.hashCode();
        return result;
    }



    public String getPackage()
    {
        return pkg;
    }



    /**
     * Getter.
     *
     * @return the simple name of the outer class (even if this binary name represents an inner class)
     */
    public String getOuterSimpleName()
    {
        return cls.get(0);
    }



    /**
     * Getter.
     *
     * @return the simple name of the inner class represented by this binary name. <code>null</code> if this binary name
     * does not represent an inner class
     */
    public String getInnerSimpleName()
    {
        return cls.size() > 1 ? cls.get(cls.size() - 1) : null;
    }



    /**
     * The fully qualified name of the outer class.
     *
     * @return that, or <code>null</code> if the simple name of the outer class is unknown
     */
    @CheckForNull
    public String getOuterFqcn()
    {
        return (pkg != null ? (pkg + ".") : "") + getOuterSimpleName();
    }
}
