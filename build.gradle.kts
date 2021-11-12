import com.android.build.gradle.BaseExtension
import io.gitlab.arturbosch.detekt.Detekt

plugins {
    with(GradlePluginId) {
        id(DETEKT)
        id(KTLINT_GRADLE)
        id(ANDROID_APPLICATION) apply false
        id(ANDROID_DYNAMIC_FEATURE) apply false
        id(ANDROID_LIBRARY) apply false
        id(KOTLIN_ANDROID) apply false
        id(KOTLIN_KAPT) apply false
        id(KOTLIN_SERIALIZATION) apply false
        id(SAFE_ARGS) apply false
        id(JACOCO)
        id(ANDROID_JUNIT_5) apply false
    }
}

allprojects {
    apply(plugin = GradlePluginId.KTLINT_GRADLE)

    ktlint {
        verbose.set(true)
        android.set(true)

        reporters {
            reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
        }

        filter {
            exclude { element -> element.file.path.contains("generated/") }
        }
    }

    dependencyLocking {
        lockAllConfigurations()
    }
}

subprojects {
    apply(plugin = GradlePluginId.DETEKT)
    apply(plugin = GradlePluginId.JACOCO)

    detekt {
        config = files("$rootDir/detekt.yml")

        parallel = true

        // By default detekt does not check test source set and gradle specific files, so hey have to be added manually
        source = files(
            "$rootDir/buildSrc",
            "$rootDir/build.gradle.kts",
            "$rootDir/settings.gradle.kts",
            "src/main/kotlin",
            "src/test/kotlin"
        )
    }

    jacoco {
        val jacocoVersion: String by project
        toolVersion = jacocoVersion
    }

    tasks.withType<JacocoReport>().all {
        reports {
            html.isEnabled = true
            xml.isEnabled = false
            csv.isEnabled = false
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform {
            includeEngines.add("spek2")
        }

        maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1

        finalizedBy("jacocoTestReport")
    }

    afterEvaluate {
        configureAndroid()
    }
}

fun Project.configureAndroid() {
    (project.extensions.findByName("android") as? BaseExtension)?.run {
        sourceSets {
            map { it.java.srcDir("src/${it.name}/kotlin") }
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }

        lintOptions {
            isAbortOnError = false
        }

        testOptions {
            unitTests {
                isReturnDefaultValues = TestOptions.IS_RETURN_DEFAULT_VALUES
                isIncludeAndroidResources = TestOptions.IS_INCLUDE_ANDROID_RESOURCES
            }
        }
    }
}

tasks.withType<Detekt> {
    this.jvmTarget = JavaVersion.VERSION_1_8.toString()
}

task("staticCheck") {
    group = "verification"

    afterEvaluate {
        // Filter modules with "lintDebug" task (non-Android modules do not have lintDebug task)
        val lintTasks = subprojects.mapNotNull { "${it.name}:lintDebug" }

        // Get modules with "testDebugUnitTest" task (app module does not have it)
        val testTasks = subprojects.mapNotNull { "${it.name}:testDebugUnitTest" }
            .filter { it != "app:testDebugUnitTest" }

        // All task dependencies
        val taskDependencies =
            mutableListOf("app:assembleAndroidTest", "ktlintCheck", "detekt").also {
                it.addAll(lintTasks)
                it.addAll(testTasks)
            }

        dependsOn(taskDependencies)
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
