import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED

plugins {
    id("java-conventions")
    id("jacoco")
    id("com.github.spotbugs")
    id("org.barfuin.gradle.jacocolog")
    id("com.diffplug.spotless")
    id("com.adarshr.test-logger")
    id("kotlin-conventions")
    id("idea")
}
val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

tasks.test {
    useJUnitPlatform()
    testLogging {
        events = setOf(FAILED)
        exceptionFormat = FULL
    }
}

sourceSets {
    create("integrationTest") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
    create("acceptanceTest") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
    create("contractTest") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}
idea {
    module {

        testSources.from.addAll(
            sourceSets["integrationTest"].allSource.srcDirs
        )
        testSources.from.addAll(
            sourceSets["acceptanceTest"].allSource.srcDirs
        )
        testSources.from.addAll(
            sourceSets["contractTest"].allSource.srcDirs
        )

    }
}

//configurations["integrationTestImplementation"].extendsFrom(configurations.testImplementation.get())
//configurations["integrationTestRuntimeOnly"].extendsFrom(configurations.testRuntimeOnly.get())

val integrationTestTask = tasks.register<Test>("integrationTest") {
    description = "Runs integration tests."
    group = JavaBasePlugin.VERIFICATION_GROUP

    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath

    useJUnitPlatform()
    testLogging {
        events = setOf(FAILED)
        exceptionFormat = FULL
    }

    mustRunAfter(tasks["test"])
}
configurations["integrationTestImplementation"].extendsFrom(configurations.testImplementation.get())
configurations["integrationTestRuntimeOnly"].extendsFrom(configurations.testRuntimeOnly.get())


val acceptanceTestTask = tasks.register<Test>("acceptanceTest") {
    description = "Runs all tests."
    group = JavaBasePlugin.VERIFICATION_GROUP
    testClassesDirs = sourceSets["acceptanceTest"].output.classesDirs
    classpath = sourceSets["acceptanceTest"].runtimeClasspath
    useJUnitPlatform()
    testLogging {
        events = setOf(FAILED)
        exceptionFormat = FULL
    }
    mustRunAfter(integrationTestTask)
}
configurations["acceptanceTestImplementation"].extendsFrom(configurations.testImplementation.get())
configurations["acceptanceTestRuntimeOnly"].extendsFrom(configurations.testRuntimeOnly.get())

val contractTestTask = tasks.register<Test>("contractTest") {
    description = "Runs contract tests."
    group = JavaBasePlugin.VERIFICATION_GROUP
    testClassesDirs = sourceSets["contractTest"].output.classesDirs
    classpath = sourceSets["contractTest"].runtimeClasspath
    useJUnitPlatform()
    testLogging {
        events = setOf(FAILED)
        exceptionFormat = FULL
    }
//    mustRunAfter(contractTestTask)
}

configurations["contractTestImplementation"].extendsFrom(configurations.testImplementation.get())
configurations["contractTestRuntimeOnly"].extendsFrom(configurations.testRuntimeOnly.get())

jacoco {
    toolVersion = libs.findVersion("jacocoVersion").get().toString()
}
tasks.jacocoTestReport {
    executionData(fileTree(projectDir) {
        include("/build/**/jacoco/*.exec")
    })
    reports {
        xml.required.set(false)
        csv.required.set(false)
        html.required.set(false)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco"))
    }
    mustRunAfter(tasks["test"])
}
tasks.jacocoTestCoverageVerification {
    executionData(fileTree(projectDir) {
        include("/build/**/jacoco/*.exec")
    })
    violationRules {
        rule {
            limit {
                minimum = BigDecimal.valueOf(0.8)
            }
        }
    }
    classDirectories.setFrom(sourceSets.main.get().output.asFileTree.matching {

    })
    mustRunAfter(tasks.jacocoTestReport)
}

val buildWithCoverage = tasks.register("buildWithCoverage") {
    group = JavaBasePlugin.VERIFICATION_GROUP
    dependsOn("build", "jacocoTestReport", "jacocoTestCoverageVerification")
    description = "Runs all tests with coverage."
    val jacocoTestReport = tasks.findByName("jacocoTestReport")
    jacocoTestReport?.mustRunAfter(tasks.build)
}

spotbugs {
    ignoreFailures.set(false)
    effort.set(com.github.spotbugs.snom.Effort.DEFAULT)
    visitors.set(setOf("FindSqlInjection", "SwitchFallthrough"))
    excludeFilter.set(file("$rootDir/spotbugs-exclude.xml"))
    includeFilter.set(file("$rootDir/spotbugs-include.xml"))
}

tasks.withType<com.github.spotbugs.snom.SpotBugsTask>().configureEach {
    reports{
        maybeCreate("xml").required.set(false)
        maybeCreate("html").required.set(false)
    }
}

spotless {
    sql {
        target("**/*.sql")
        dbeaver()
    }
}



dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    spotbugs(libs.findLibrary("spotbugs").get())
    spotbugsPlugins(libs.findLibrary("findsecbugs-plugin").get())

    "integrationTestImplementation"(project)
    "acceptanceTestImplementation"(project)
    "contractTestImplementation"(project)

}
