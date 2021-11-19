import com.android.build.api.dsl.VariantDimension
import com.android.build.gradle.internal.dsl.BaseFlavor
import java.util.*
import kotlinx.kover.api.CoverageEngine.JACOCO
import kotlinx.kover.api.KoverTaskExtension

plugins {
    with(GradlePluginId) {
        id(ANDROID_APPLICATION)
        id(KOTLIN_ANDROID)
        id(KOTLIN_KAPT)
        id(KOTLIN_PARCELIZE)
        id(KOTLIN_SERIALIZATION)
        id(KTLINT_GRADLE)
        id(SAFE_ARGS)
        id(ANDROID_JUNIT_5)
    }
}

android {
    compileSdk = AndroidConfig.COMPILE_SDK_VERSION

    defaultConfig {
        applicationId = AndroidConfig.ID
        minSdk = AndroidConfig.MIN_SDK_VERSION
        targetSdk = AndroidConfig.TARGET_SDK_VERSION
        buildToolsVersion = AndroidConfig.BUILD_TOOLS_VERSION
        versionCode = AndroidConfig.VERSION_CODE
        versionName = AndroidConfig.VERSION_NAME
        vectorDrawables.useSupportLibrary = AndroidConfig.VECTOR_DRAWABLES_SUPPORT
        multiDexEnabled = AndroidConfig.MULTIDEX_ENABLED

        testInstrumentationRunner = AndroidConfig.TEST_INSTRUMENTATION_RUNNER
    }

    buildTypes {
        getByName(BuildType.DEBUG) {
            isShrinkResources = BuildTypeDebug.isShrinkResources
            isMinifyEnabled = BuildTypeDebug.isMinifyEnabled
            isDebuggable = BuildTypeDebug.isDebuggable
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        getByName(BuildType.RELEASE) {
            isShrinkResources = BuildTypeRelease.isShrinkResources
            isMinifyEnabled = BuildTypeRelease.isMinifyEnabled
            isDebuggable = BuildTypeRelease.isDebuggable
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    flavorDimensions.add(FlavorType.DIMENSION)
    productFlavors {
        create(FlavorType.DEVELOPMENT) {
            applicationIdSuffix = FlavorTypeDevelopment.suffix

            val devProps = Properties().apply {
                File(project.rootDir, FlavorTypeDevelopment.propFile).inputStream()
                    .use { inputStream ->
                        load(inputStream)
                    }
            }
            buildConfigField("FEATURE_MODULE_NAMES", getFeatureNames())
            for (key in devProps.keys()) {
                buildConfigField("String", key as String, devProps[key] as String)
            }
        }

        create(FlavorType.PRODUCTION) {
            applicationIdSuffix = FlavorTypeProduction.suffix

            val prodProps = Properties().apply {
                File(project.rootDir, FlavorTypeDevelopment.propFile).inputStream()
                    .use { inputStream ->
                        load(inputStream)
                    }
            }
            buildConfigField("FEATURE_MODULE_NAMES", getFeatureNames())
            for (key in prodProps.keys()) {
                buildConfigField("String", key as String, prodProps[key] as String)
            }
        }
    }

    buildFeatures {
        dataBinding = true
        viewBinding = true
    }

    dynamicFeatures.addAll(ModuleDependency.getFeatureModules().toMutableSet())

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
    api(libs.kotlin.coroutines)
    api(libs.kotlin.serialization)
    api(libs.bundles.commons)
    implementation(libs.android.splash)
    api(libs.multidex)

    api(libs.bundles.components)

    api(libs.bundles.layouts)

    api(libs.bundles.lifecycle)
    api(libs.bundles.navigation)

    api(libs.bundles.localstore)

    api(libs.bundles.database)
    kapt(libs.room.kapt)

    api(libs.bundles.network)

    api(libs.play.core)

    api(libs.bundles.kodein)

    api(libs.timber)
    implementation(libs.bundles.stetho)

    debugApi(libs.leakcanary)

    testImplementation(project(ModuleDependency.LIBRARY_TEST))

    testImplementation(libs.bundles.test.unit)
    testRuntimeOnly(libs.bundles.test.runtime.unit)

    androidTestImplementation(libs.bundles.test.platform)
    androidTestRuntimeOnly(libs.bundles.test.runtime.platform)
}

fun BaseFlavor.buildConfigFieldFromGradleProperty(gradlePropertyName: String) {
    val propertyValue = project.properties[gradlePropertyName] as? String
    checkNotNull(propertyValue) { "Gradle property $gradlePropertyName is null" }

    val androidResourceName = "GRADLE_${gradlePropertyName.toSnakeCase()}".toUpperCase()
    buildConfigField("String", androidResourceName, propertyValue)
}

fun getFeatureNames(): Array<String> = ModuleDependency
    .getFeatureModules()
    .map { it.replace(":feature_", "") }
    .toTypedArray()

fun String.toSnakeCase(): String = this.split(Regex("(?=[A-Z])"))
    .joinToString("_") {
        it.toLowerCase()
    }

fun VariantDimension.buildConfigField(name: String, value: Array<String>) {
    val strValue = value.joinToString(
        prefix = "{", separator = ",", postfix = "}", transform = { "\"$it\"" }
    )
    buildConfigField("String[]", name, strValue)
}
