package com.thomasjensen.checkstyle.addons;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

import com.google.common.collect.Lists;
import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.DefaultLogger;
import com.puppycrawl.tools.checkstyle.TreeWalker;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import junit.framework.AssertionFailedError;
import org.junit.Assert;

import com.thomasjensen.checkstyle.addons.util.Util;

// @formatter:off
/**
 * This class was copied from the Checkstyle project. The original source is <a
 * href="https://github.com/checkstyle/checkstyle/blob/checkstyle-5
 * .8/src/test/java/com/puppycrawl/tools/checkstyle/BaseCheckTestSupport.java"
 * target="_blank">on GitHub</a>. <p/>Credit goes to Oliver Burn, Ivan Sopov, et al.
 * <p/>
 * Used under the terms of the GNU LESSER GENERAL PUBLIC LICENSE, Version 2.1.
 *
 * @author Oliver Burn, Ivan Sopov, et al.
 */
// @formatter:on
public abstract class BaseCheckTestSupport
{
    /** A brief logger that only displays info about errors. */
    protected static class BriefLogger
        implements AuditListener
    {
        private DefaultLogger delegate;



        @SuppressWarnings({"JavaReflectionInvocation", "JavaReflectionMemberAccess", "RedundantSuppression"})
        public BriefLogger(final OutputStream pOut)
        {
            try {
                Constructor<DefaultLogger> constructor;
                try {
                    constructor = DefaultLogger.class.getConstructor(OutputStream.class, boolean.class);
                    delegate = constructor.newInstance(pOut, Boolean.TRUE);
                }
                catch (NoSuchMethodException e) {
                    // this is normal for Checkstyle << 8.25
                    Class<?> osoClass = Class.forName(
                        "com.puppycrawl.tools.checkstyle.api.AutomaticBean$OutputStreamOptions");
                    Object enumConstant = osoClass.getEnumConstants()[0]; // CLOSE
                    try {
                        constructor = DefaultLogger.class.getConstructor(OutputStream.class, osoClass);
                        delegate = constructor.newInstance(pOut, enumConstant);
                    }
                    catch (NoSuchMethodException pE) {
                        throw new AssertionFailedError(e.getMessage());
                    }
                }
            }
            catch (ReflectiveOperationException | RuntimeException e) {
                throw new AssertionFailedError(e.getMessage());
            }
        }



        @Override
        public void auditStarted(final AuditEvent pEvent)
        {
            // empty
        }



        @Override
        public void auditFinished(final AuditEvent pEvent)
        {
            delegate.auditFinished(pEvent);
        }



        @Override
        public void fileFinished(final AuditEvent pEvent)
        {
            // empty
        }



        @Override
        public void addError(final AuditEvent pEvent)
        {
            delegate.addError(pEvent);
        }



        @Override
        public void addException(final AuditEvent pEvent, final Throwable pThrowable)
        {
            delegate.addException(pEvent, pThrowable);
        }



        @Override
        public void fileStarted(final AuditEvent pEvent)
        {
            // empty
        }
    }



    protected final ByteArrayOutputStream mBAOS = new ByteArrayOutputStream();

    protected PrintStream mStream;

    private String checkShortname = null;



    protected BaseCheckTestSupport()
    {
        try {
            mStream = new PrintStream(mBAOS, false, Util.UTF8.name());
        }
        catch (UnsupportedEncodingException e) {
            // cannot happen because we use an existing Charset object
        }
    }



    protected final void setCheckShortname(@Nonnull final Class<?> pCheck)
    {
        String result = pCheck.getSimpleName();
        if (result.endsWith("Check")) {
            result = result.substring(0, result.length() - "Check".length());
        }
        checkShortname = result;
    }



    public static DefaultConfiguration createCheckConfig(final Class<?> pClazz)
    {
        return new DefaultConfiguration(pClazz.getName());
    }



    @SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
    protected Checker createChecker(final Configuration pCheckConfig)
        throws Exception
    {
        final DefaultConfiguration dc = createCheckerConfig(pCheckConfig);
        final Checker c = new Checker();
        // make sure the tests always run with english error messages
        // so the tests don't fail in supported locales like german
        final Locale locale = Locale.ENGLISH;
        c.setLocaleCountry(locale.getCountry());
        c.setLocaleLanguage(locale.getLanguage());
        c.setModuleClassLoader(Thread.currentThread().getContextClassLoader());
        c.configure(dc);
        c.addListener(new BriefLogger(mStream));
        return c;
    }



    protected DefaultConfiguration createCheckerConfig(final Configuration pConfig)
    {
        final DefaultConfiguration dc = new DefaultConfiguration("configuration");
        final DefaultConfiguration twConf = createCheckConfig(TreeWalker.class);
        // make sure that the tests always run with this charset
        dc.addAttribute("charset", "UTF-8");
        dc.addChild(twConf);
        twConf.addChild(pConfig);
        return dc;
    }



    protected static String getPath(final String pFilename)
        throws IOException
    {
        return new File("src/test/resources/com/thomasjensen/checkstyle/addons/checks/" + pFilename).getCanonicalPath();
    }



    @SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
    protected void verify(final Configuration pConfig, final String pFileName, final String[] pExpected)
        throws Exception
    {
        verify(createChecker(pConfig), pFileName, pFileName, pExpected);
    }



    @SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
    protected void verify(final Checker pChecker, final String pProcessedFilename, final String pMessageFileName,
        final String[] pExpected)
        throws Exception
    {
        verify(pChecker, new File[]{new File(pProcessedFilename)}, pMessageFileName, pExpected);
    }



    @SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
    protected void verify(final Checker pChecker, final File[] pProcessedFiles, final String pMessageFileName,
        final String[] pExpected)
        throws Exception
    {
        try {
            mStream.flush();
            final List<File> theFiles = Lists.newArrayList();
            Collections.addAll(theFiles, pProcessedFiles);
            final int errs = pChecker.process(theFiles);

            // process each of the lines
            final ByteArrayInputStream bais = new ByteArrayInputStream(mBAOS.toByteArray());
            final LineNumberReader lnr = new LineNumberReader(new InputStreamReader(bais, Util.UTF8));

            for (int i = 0; i < pExpected.length; i++) {
                final String expected = pMessageFileName + ":" + pExpected[i];
                String actual = lnr.readLine();
                if (actual != null) {
                    actual = actual.replaceFirst(Pattern.quote("error: "), "");   // fix message format changed in 6.11

                    // fix message format changed in 6.14 (Checkstyle Issue #2666)
                    actual = actual.replaceFirst(Pattern.quote("[ERROR] "), "");
                    final String moduleHint = " [" + checkShortname + "]";
                    if (actual.endsWith(moduleHint)) {
                        actual = actual.substring(0, actual.length() - moduleHint.length());
                    }
                }
                Assert.assertEquals("error message " + i, expected, actual);
            }

            Assert.assertEquals("unexpected output: " + lnr.readLine(), pExpected.length, errs);
        }
        finally {
            pChecker.destroy();
        }
    }



    public static boolean isJava6()
    {
        try {
            Assert.assertEquals(1, Integer.parseInt("+1"));
            return false;
        }
        catch (NumberFormatException e) {
            return true;
        }
    }
}
