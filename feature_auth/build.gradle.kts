plugins {
    with(GradlePluginId) {
        id(ANDROID_DYNAMIC_FEATURE)
        id(KOTLIN_ANDROID)
        id(KOTLIN_PARCELIZE)
        id(KOTLIN_SERIALIZATION)
        id(SAFE_ARGS)
        id(ANDROID_JUNIT_5)
    }
}

android {
    compileSdk = AndroidConfig.COMPILE_SDK_VERSION

    defaultConfig {
        minSdk = AndroidConfig.MIN_SDK_VERSION
        buildToolsVersion = AndroidConfig.BUILD_TOOLS_VERSION

        testInstrumentationRunner = AndroidConfig.TEST_INSTRUMENTATION_RUNNER
    }

    buildTypes {
        getByName(BuildType.DEBUG) {
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        getByName(BuildType.RELEASE) {
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        dataBinding = true
        viewBinding = true
    }

    kotlinOptions {
        jvmTarget = JavaOptions.VERSION.toString()
    }
}

dependencies {
    implementation(project(ModuleDependency.APP))

    testImplementation(project(ModuleDependency.LIBRARY_TEST))
    testImplementation(libs.bundles.test.unit)
    testRuntimeOnly(libs.bundles.test.runtime.unit)

    androidTestImplementation(libs.bundles.test.platform)
    androidTestRuntimeOnly(libs.bundles.test.runtime.platform)
}