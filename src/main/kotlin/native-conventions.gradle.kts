plugins {
    id("java-conventions")
    id("org.graalvm.buildtools.native")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

graalvmNative {
    toolchainDetection.set(true)

    binaries.configureEach {
        javaLauncher.set(javaToolchains.launcherFor {
            languageVersion.set(
                JavaLanguageVersion.of(libs.findVersion("jdk").get().toString())
            )
            vendor.set(JvmVendorSpec.matching("GraalVM"))
        })
    }
}
