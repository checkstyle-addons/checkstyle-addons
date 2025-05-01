package com.thomasjensen.checkstyle.addons.build;

import javax.annotation.Nonnull;

import org.gradle.api.Action;
import org.gradle.api.JavaVersion;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.gradle.jvm.toolchain.JavaToolchainSpec;
import org.gradle.jvm.toolchain.JvmVendorSpec;


/**
 * A Gradle Action which modifies a given JavaToolchainSpec with respect to Java level and vendor.
 */
public class ToolchainSpecAction
    implements Action<JavaToolchainSpec>
{
    private final JavaVersion javaVersion;



    public ToolchainSpecAction(@Nonnull final JavaVersion pJavaVersion)
    {
        javaVersion = pJavaVersion;
    }



    @Override
    public void execute(@Nonnull final JavaToolchainSpec pSpec)
    {
        pSpec.getLanguageVersion().set(JavaLanguageVersion.of(javaVersion.getMajorVersion()));
        pSpec.getVendor().set(javaVersion.isJava7() ? JvmVendorSpec.AZUL : JvmVendorSpec.ADOPTIUM);
    }
}
