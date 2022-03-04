package com.thomasjensen.checkstyle.addons.build;
/*
 * Checkstyle-Addons - Additional Checkstyle checks
 * Copyright (c) 2015-2020, the Checkstyle Addons contributors
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

import java.util.Set;

import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.tasks.testing.Test;
import org.gradle.api.tasks.testing.TestDescriptor;
import org.gradle.api.tasks.testing.TestOutputEvent;
import org.gradle.api.tasks.testing.TestResult;
import org.gradle.api.tasks.testing.logging.TestExceptionFormat;
import org.gradle.api.tasks.testing.logging.TestLogEvent;
import org.gradle.api.tasks.testing.logging.TestLoggingContainer;


/**
 * Common configuration of all our test tasks.
 */
public class TestConfigAction
    implements Action<Test>
{
    @Override
    public void execute(Test pTestTask)
    {
        pTestTask.testLogging((TestLoggingContainer tlc) -> {
            tlc.setEvents(Set.of(TestLogEvent.FAILED));
            tlc.setShowStackTraces(true);
            tlc.setShowExceptions(true);
            tlc.setShowCauses(true);
            tlc.setExceptionFormat(TestExceptionFormat.FULL);
        });

        pTestTask.onOutput(new Closure<Void>(pTestTask)
        {
            @Override
            public Void call(final Object... args)
            {
                TestDescriptor descriptor = (TestDescriptor) args[0];
                TestOutputEvent event = (TestOutputEvent) args[1];
                if (!"com.thomasjensen.checkstyle.addons.checks.misc.MdlJsonConfigValidatorTest".equals(
                    descriptor.getClassName())
                    || event.getDestination() == TestOutputEvent.Destination.StdErr) //
                {
                    pTestTask.getLogger().lifecycle(event.getMessage());
                }
                return null;
            }
        });

        pTestTask.afterSuite(new Closure<Void>(pTestTask)
        {
            @Override
            public Void call(final Object... args)
            {
                TestDescriptor descriptor = (TestDescriptor) args[0];
                TestResult testResult = (TestResult) args[1];
                if (descriptor.getParent() == null) {
                    pTestTask.getLogger().lifecycle(System.lineSeparator()
                        + testResult.getTestCount() + " tests executed, "
                        + testResult.getSuccessfulTestCount() + " successful, "
                        + (testResult.getTestCount() - testResult.getSuccessfulTestCount()
                        - testResult.getSkippedTestCount()) + " failed, "
                        + testResult.getSkippedTestCount() + " skipped.");
                }
                return null;
            }
        });
    }
}
