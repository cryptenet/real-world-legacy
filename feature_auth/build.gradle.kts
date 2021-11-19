import java.util.*
import kotlinx.kover.api.CoverageEngine.JACOCO
import kotlinx.kover.api.KoverTaskExtension

plugins {
    with(GradlePluginId) {
        id(ANDROID_DYNAMIC_FEATURE)
        id(KOTLIN_ANDROID)
        id(KOTLIN_KAPT)
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

    buildFeatures {
        dataBinding = true
        viewBinding = true
    }

    kotlinOptions {
        jvmTarget = JavaOptions.VERSION.toString()
    }

    kapt {
        correctErrorTypes = true
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
    implementation(project(ModuleDependency.APP))

    testImplementation(project(ModuleDependency.LIBRARY_TEST))
    testImplementation(libs.bundles.test.unit)
    testRuntimeOnly(libs.bundles.test.runtime.unit)

    androidTestImplementation(libs.bundles.test.platform)
    androidTestRuntimeOnly(libs.bundles.test.runtime.platform)
}
