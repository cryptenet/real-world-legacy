import java.util.*
import kotlinx.kover.api.CoverageEngine.JACOCO
import kotlinx.kover.api.KoverTaskExtension

plugins {
    with(GradlePluginId) {
        id(ANDROID_LIBRARY)
        id(KOTLIN_ANDROID)
        id(ANDROID_JUNIT_5)
    }
}

android {
    compileSdk = AndroidConfig.COMPILE_SDK_VERSION

    defaultConfig {
        minSdk = AndroidConfig.MIN_SDK_VERSION
        targetSdk = AndroidConfig.TARGET_SDK_VERSION
        buildToolsVersion = AndroidConfig.BUILD_TOOLS_VERSION

        testInstrumentationRunner = AndroidConfig.TEST_INSTRUMENTATION_RUNNER
    }

    buildTypes {
        getByName(BuildType.DEBUG) {
            isMinifyEnabled = BuildTypeDebug.isMinifyEnabled
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        getByName(BuildType.RELEASE) {
            isMinifyEnabled = BuildTypeRelease.isMinifyEnabled
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    flavorDimensions.add(FlavorType.DIMENSION)
    productFlavors {
        create(FlavorType.DEVELOPMENT) {
            val devProps = Properties().apply {
                File(project.rootDir, FlavorTypeDevelopment.propFile).inputStream()
                    .use { inputStream ->
                        load(inputStream)
                    }
            }
            for (key in devProps.keys()) {
                buildConfigField("String", key as String, devProps[key] as String)
            }
        }

        create(FlavorType.PRODUCTION) {
            val prodProps = Properties().apply {
                File(project.rootDir, FlavorTypeDevelopment.propFile).inputStream()
                    .use { inputStream ->
                        load(inputStream)
                    }
            }
            for (key in prodProps.keys()) {
                buildConfigField("String", key as String, prodProps[key] as String)
            }
        }
    }

    kotlinOptions {
        jvmTarget = JavaOptions.VERSION.toString()
    }

    lint {
        isIgnoreTestSources = true
    }

    testOptions {
        unitTests {
            all {
                if (it.name == "testDebugUnitTest") {
                    extensions.configure(KoverTaskExtension::class) {
                        isEnabled = true
                        binaryReportFile.set(file("$buildDir/reports/kover/debug-report.bin"))
                        includes = listOf("com\\.cryptenet\\.rwl_rest\\..*")
                        excludes =
                            listOf("com\\.cryptenet\\.rwl_rest\\.databinding\\..*", "androidx\\..*")
                    }
                }
            }
        }
    }

    kover {
        isEnabled = true
        coverageEngine.set(JACOCO)
        jacocoEngineVersion.set("0.8.7")
        generateReportOnCheck.set(true)
    }
}

dependencies {
    implementation(libs.bundles.kotlin)

    implementation(libs.bundles.test.unit)
    runtimeOnly(libs.bundles.test.runtime.unit)

    androidTestImplementation(libs.bundles.test.platform)
    runtimeOnly(libs.bundles.test.runtime.platform)
}
