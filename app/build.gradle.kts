import com.android.build.api.dsl.VariantDimension
import com.android.build.gradle.internal.dsl.BaseFlavor

plugins {
    with(GradlePluginId) {
        id(ANDROID_APPLICATION)
        id(KOTLIN_ANDROID)
        id(KOTLIN_KAPT)
        id(KOTLIN_PARCELIZE)
        id(KOTLIN_SERIALIZATION)
        id(SAFE_ARGS)
        id(KSP)
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

        buildConfigField("FEATURE_MODULE_NAMES", getFeatureNames())
    }

    buildTypes {
        getByName("debug") {
            isShrinkResources = BuildTypeDebug.isShrinkResources
            isMinifyEnabled = BuildTypeDebug.isMinifyEnabled
            isDebuggable = BuildTypeDebug.isDebuggable
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        getByName("release") {
            isShrinkResources = BuildTypeRelease.isShrinkResources
            isMinifyEnabled = BuildTypeRelease.isMinifyEnabled
            isDebuggable = BuildTypeRelease.isDebuggable
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

    dynamicFeatures.addAll(ModuleDependency.getFeatureModules().toMutableSet())

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }

    kapt {
        correctErrorTypes = true
    }

    lint {
        isIgnoreTestSources = true
    }
}

dependencies {
    api(libs.bundles.kotlin)
    api(libs.bundles.commons)
    api(libs.multidex)

    api(libs.bundles.components)

    api(libs.bundles.layouts)

    api(libs.bundles.lifecycle)
    api(libs.bundles.navigation)

    api(libs.bundles.localstore)

    api(libs.bundles.database)
    ksp(libs.room.ksp)

    api(libs.bundles.network)

    api(libs.play.core)

    api(libs.bundles.kodein)

    api(libs.timber)

    debugApi(libs.leakcanary)

    testApi(libs.bundles.test.unit)
    testRuntimeOnly(libs.bundles.test.runtime.unit)
    androidTestApi(libs.bundles.test.platform)
    androidTestRuntimeOnly(libs.bundles.test.runtime.platform)
}

fun BaseFlavor.buildConfigFieldFromGradleProperty(gradlePropertyName: String) {
    val propertyValue = project.properties[gradlePropertyName] as? String
    checkNotNull(propertyValue) { "Gradle property $gradlePropertyName is null" }

    val androidResourceName = "GRADLE_${gradlePropertyName.toSnakeCase()}".toUpperCase()
    buildConfigField("String", androidResourceName, propertyValue)
}

fun getFeatureNames() = ModuleDependency
    .getFeatureModules()
    .map { it.replace(":feature_", "") }
    .toTypedArray()

fun String.toSnakeCase() = this.split(Regex("(?=[A-Z])"))
    .joinToString("_") {
        it.toLowerCase()
    }

fun VariantDimension.buildConfigField(name: String, value: Array<String>) {
    val strValue = value.joinToString(
        prefix = "{", separator = ",", postfix = "}", transform = { "\"$it\"" }
    )
    buildConfigField("String[]", name, strValue)
}
