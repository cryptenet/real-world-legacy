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

dependencies {
    api(libs.bundles.kotlin)
}
