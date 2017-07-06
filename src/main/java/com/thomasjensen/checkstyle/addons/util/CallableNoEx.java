package com.thomasjensen.checkstyle.addons.util;
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

import java.util.concurrent.Callable;
import javax.annotation.Nonnull;


/**
 * The same as {@link Callable}, but throws no checked exceptions.
 *
 * @param <R> type of the result
 */
public interface CallableNoEx<R>
    extends Callable<R>
{
    @Override
    @Nonnull
    R call();
}
