plugins {
    id("com.github.spacialcircumstances.gradle-cucumber-reporting")
}

cucumberReports {
    outputDir = file("${layout.buildDirectory}/reports/cucumber")
    reports = files("${layout.buildDirectory}/reports/cucumber/cucumber.json")
    expandAllSteps = true
    testTasksFinalizedByReport = false
}