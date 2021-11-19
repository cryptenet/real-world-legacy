import kotlinx.kover.api.CoverageEngine.JACOCO

plugins {
    with(GradlePluginId) {
        id(JAVA)
        id(KOTLIN)
    }
}

java {
    sourceCompatibility = JavaOptions.VERSION
    targetCompatibility = JavaOptions.VERSION
}

kover {
    isEnabled = true
    coverageEngine.set(JACOCO)
    jacocoEngineVersion.set("0.8.7")
    generateReportOnCheck.set(true)
}

dependencies {
    api(libs.bundles.kotlin)
}
