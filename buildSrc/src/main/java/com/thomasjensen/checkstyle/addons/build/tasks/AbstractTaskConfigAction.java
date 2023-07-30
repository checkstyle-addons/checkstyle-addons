package com.thomasjensen.checkstyle.addons.build.tasks;
/*
 * Checkstyle-Addons - Additional Checkstyle checks
 * Copyright (c) 2015-2023, the Checkstyle Addons contributors
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

import java.util.Objects;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;

import com.thomasjensen.checkstyle.addons.build.BuildUtil;
import com.thomasjensen.checkstyle.addons.build.DependencyConfig;


/**
 * Base class for all the task configurers that we have.
 *
 * @param <T> type of task being configured
 */
public abstract class AbstractTaskConfigAction<T extends Task>
    implements Action<T>
{
    private final DependencyConfig depConfig;

    private final Object[] extraParams;

    protected Project project;

    protected BuildUtil buildUtil;



    protected AbstractTaskConfigAction()
    {
        depConfig = null;
        extraParams = null;
    }



    protected AbstractTaskConfigAction(@Nonnull final DependencyConfig pDepConfig,
        @Nullable final Object... pExtraParams)
    {
        depConfig = Objects.requireNonNull(pDepConfig);
        extraParams = pExtraParams;
    }



    @CheckForNull
    protected Object[] getExtraParams()
    {
        return extraParams;
    }



    @Override
    public final void execute(@Nonnull final T pTask)
    {
        Objects.requireNonNull(pTask);
        if (pTask.getLogger().isInfoEnabled()) {
            String msg = "Configuring task '" + pTask.getPath() + "'";
            if (depConfig != null) {
                msg += " for depConfig '" + depConfig.getName() + "'" + getExtraLogInfo(pTask);
            }
            pTask.getLogger().info(msg);
        }
        project = pTask.getProject();
        buildUtil = new BuildUtil(pTask.getProject());
        configureTaskFor(pTask, depConfig);
    }



    @Nonnull
    protected String getExtraLogInfo(@Nonnull final T pTask)
    {
        return "";
    }



    /**
     * Configure the task instance for the dependency configuration.
     *
     * @param pTask the task to configure
     * @param pDepConfig the dependency configuration for which to configure. Can be <code>null</code> only when
     *     this config action was initialized without a dependency configuration
     */
    protected abstract void configureTaskFor(@Nonnull final T pTask, @Nullable DependencyConfig pDepConfig);
}
