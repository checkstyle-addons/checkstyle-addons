package com.thomasjensen.checkstyle.addons.checks.misc;
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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import com.thomasjensen.checkstyle.addons.util.Util;


/**
 * Check that checks the Checkstyle setup by providing classloader information. Mainly useful for troubleshooting a
 * Checkstyle setup.
 */
public class DebugMetaCheck
    extends AbstractCheck
{
    private static final int[] TOKENS = new int[]{TokenTypes.EOF};

    private static final int FILE_BUFFER_SIZE_BYTES = 20 * 1024;

    private static final String LIST_BULLET = "\t- ";

    /** <img src="doc-files/DebugMetaCheck-1.png" alt="Regex explanation"> */
    private static final Pattern VERSION_PATTERN = Pattern.compile(
        "(?:^" + Pattern.quote(LIST_BULLET) + "|[/\\\\])checkstyle-(\\d+\\.\\d+(?:\\.\\d+)?)(?:-all)?\\.jar$",
        Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

    private static final String DEFAULT_OUTPUT = "stdout";

    private String outputFile = DEFAULT_OUTPUT;

    private boolean append = false;

    private boolean trigger = true;



    @Override
    public int[] getDefaultTokens()
    {
        return Arrays.copyOf(TOKENS, TOKENS.length);
    }



    @Override
    public int[] getAcceptableTokens()
    {
        return Arrays.copyOf(TOKENS, TOKENS.length);
    }



    @Override
    public int[] getRequiredTokens()
    {
        return Arrays.copyOf(TOKENS, TOKENS.length);
    }



    @Override
    public void init()
    {
        super.init();
        trigger = true;
    }



    @Override
    public void beginTree(final DetailAST pRootAST)
    {
        if (!trigger) {
            return;    // only do this once
        }
        trigger = false;

        Writer writer = null;
        BufferedWriter bw = null;
        PrintWriter pw = null;
        try {
            if ("stdout".equalsIgnoreCase(outputFile)) {
                writer = new OutputStreamWriter(System.out, StandardCharsets.ISO_8859_1);
            }
            else if ("stderr".equalsIgnoreCase(outputFile)) {
                writer = new OutputStreamWriter(System.err, StandardCharsets.ISO_8859_1);
            }
            else {
                writer = new FileWriter(outputFile, append);
            }
            bw = new BufferedWriter(writer, FILE_BUFFER_SIZE_BYTES);
            pw = new PrintWriter(bw);
            execute(pw);
        }
        catch (IOException e) {
            log(pRootAST, "debugmeta.ioerror", outputFile, e.getMessage());
        }
        finally {
            Util.closeQuietly(pw);
            Util.closeQuietly(bw);
            if (writer instanceof FileWriter) {
                Util.closeQuietly(writer);
            }
        }
    }



    private void execute(final PrintWriter pOut)
    {
        String s = DebugMetaCheck.class.getSimpleName() + " Output Log";
        pOut.println(s);
        pOut.println(nDashes(s.length()));
        final DateFormat df = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss ZZZZ");
        pOut.println("Timestamp: " + df.format(new Date()));
        pOut.println();
        pOut.println("Class Loader Hierarchy from lowest to Bootstrap:");
        pOut.println();

        final List<String> checkstyleVersions = new ArrayList<>();
        for (ClassLoader cl : getClassLoaders(DebugMetaCheck.class.getClassLoader())) {
            pOut.println("Class Loader: " + cl.getClass().getName());
            String urls = getClassPath(cl);
            pOut.println(urls);

            Matcher matcher = VERSION_PATTERN.matcher(urls);
            while (matcher.find()) {
                checkstyleVersions.add(matcher.group(1));
            }
        }

        if (checkstyleVersions.size() == 1) {
            pOut.println("Checkstyle version detected: " + checkstyleVersions.get(0));
        }
        else if (checkstyleVersions.size() > 1) {
            pOut.println("WARNING: It appears that multiple versions of Checkstyle are loaded at the same time: "
                + checkstyleVersions);
        }
        else {
            pOut.println("Checkstyle version detected: NONE (this is probably an error!)");
        }
    }



    @Nonnull
    private String nDashes(final int pLength)
    {
        final String s = "------------------------------------------------------------------------------------------"
            + "-----------------------------------------------------------------------------------------------------";
        return s.substring(0, Math.min(pLength, s.length()));
    }



    @Nonnull
    private List<ClassLoader> getClassLoaders(@Nonnull final ClassLoader pCurrentClassLoader)
    {
        final List<ClassLoader> result = new ArrayList<>();
        for (ClassLoader cl = pCurrentClassLoader; cl != null && !result.contains(cl); cl = cl.getParent()) {
            result.add(cl);
        }
        return result;
    }



    @Nonnull
    @SuppressWarnings("unchecked")
    private String getClassPath(@Nonnull final ClassLoader pClassLoader)
    {
        StringBuilder sb = new StringBuilder();
        if ("com.intellij.util.lang.UrlClassLoader".equals(pClassLoader.getClass().getName())) {
            List<URL> urls = Collections.emptyList();
            try {
                Method getUrls = pClassLoader.getClass().getMethod("getUrls");
                urls = (List<URL>) getUrls.invoke(pClassLoader);
            }
            catch (ReflectiveOperationException | RuntimeException e) {
                sb.append(LIST_BULLET);
                sb.append("(class loader type unknown)");
                sb.append(System.lineSeparator());
            }
            if (urls != null) {
                for (final URL url : urls) {
                    sb.append(LIST_BULLET);
                    sb.append(url);
                    sb.append(System.lineSeparator());
                }
            }
        }
        else if (pClassLoader instanceof URLClassLoader) {
            final URLClassLoader urlClassLoader = (URLClassLoader) pClassLoader;
            for (final URL url : urlClassLoader.getURLs()) {
                sb.append(LIST_BULLET);
                sb.append(url);
                sb.append(System.lineSeparator());
            }
        }
        return sb.toString();
    }



    public void setAppend(final boolean pAppend)
    {
        append = pAppend;
    }



    public void setOutputFile(final String pOutputFile)
    {
        outputFile = pOutputFile != null ? pOutputFile : DEFAULT_OUTPUT;
    }
}
