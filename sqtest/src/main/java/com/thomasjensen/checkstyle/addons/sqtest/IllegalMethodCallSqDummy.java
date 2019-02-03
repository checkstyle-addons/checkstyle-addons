package com.thomasjensen.checkstyle.addons.sqtest;
/*
 * Checkstyle-Addons - Additional Checkstyle checks
 * Copyright (c) 2015-2018, Thomas Jensen and the Checkstyle Addons contributors
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



/**
 * Class which contains issues for Checkstyle Addons to find.
 */
public class IllegalMethodCallSqDummy
{
    public void method1()
        throws ClassNotFoundException
    {
        long current_millis = System.currentTimeMillis();
        if (current_millis % 2 == 0) {
            Class.forName("com.thomasjensen.checkstyle.addons.sqtest.IllegalMethodCallSqDummy");
        }
    }
}
