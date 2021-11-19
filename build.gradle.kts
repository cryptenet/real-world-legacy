import com.android.build.gradle.BaseExtension
import io.gitlab.arturbosch.detekt.Detekt
import kotlinx.kover.api.KoverTaskExtension
import kotlinx.kover.tasks.KoverHtmlReportTask

plugins {
    with(GradlePluginId) {
        id(DETEKT)
        id(KTLINT_GRADLE)
        id(KOVER) apply false
        id(ANDROID_APPLICATION) apply false
        id(ANDROID_DYNAMIC_FEATURE) apply false
        id(ANDROID_LIBRARY) apply false
        id(KOTLIN_ANDROID) apply false
        id(KOTLIN_KAPT) apply false
        id(KOTLIN_PARCELIZE) apply false
        id(KOTLIN_SERIALIZATION) apply false
        id(SAFE_ARGS) apply false
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

    configurations.all {
        resolutionStrategy.force("org.objenesis:objenesis:2.6")
    }

    dependencyLocking {
        lockAllConfigurations()
    }
}

subprojects {
    apply(plugin = GradlePluginId.DETEKT)
    apply(plugin = GradlePluginId.KOVER)

    detekt {
        config = files("$rootDir/detekt.yml")

        parallel = true

        // By default detekt does not check test source set and gradle specific files,
        // so hey have to be added manually
        source = files(
            "$rootDir/buildSrc",
            "$rootDir/build.gradle.kts",
            "$rootDir/settings.gradle.kts",
            "src/main/kotlin",
            "src/test/kotlin"
        )
    }

    tasks.withType<Test> {
        useJUnitPlatform {
            includeEngines.add("spek2")
        }

        extensions.configure(KoverTaskExtension::class) {
            isEnabled = true
            binaryReportFile.set(file("$buildDir/reports/kover/debug-report.bin"))
            includes = listOf("com\\.cryptenet\\.rwl_rest\\..*")
            excludes = listOf("com\\.cryptenet\\.rwl_rest\\.databinding\\..*", "androidx\\..*")
        }

        maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1

        finalizedBy("koverHtmlReport")
    }

    tasks.withType<KoverHtmlReportTask> {
        isEnabled = true
        htmlReportDir.set(layout.buildDirectory.dir("reports/kover/html-result"))
    }

    tasks.withType<KoverHtmlReportTask> {
        isEnabled = false
    }

    afterEvaluate {
        configureAndroid()
    }

    configurations.all {
        resolutionStrategy.componentSelection {
            all {
                val detektExceptions = listOf(
                    "io.gitlab.arturbosch.detekt",
                    "com.fasterxml.jackson",
                    "com.fasterxml.jackson.core",
                    "com.fasterxml.jackson"
                )

                if (detektExceptions.any { it == candidate.group }) {
                    return@all
                }

                val androidLintExceptions = listOf("com.android.tools.build")

                if (androidLintExceptions.any { it == candidate.group }) {
                    return@all
                }
            }
        }
    }
}

fun Project.configureAndroid() {
    (project.extensions.findByName("android") as? BaseExtension)?.run {
        defaultConfig {
            javaCompileOptions {
                annotationProcessorOptions {
                    arguments += mapOf(
                        "room.schemaLocation" to "$projectDir/schemas",
                        "room.incremental" to "true",
                        "room.expandProjection" to "true"
                    )
                }
            }
        }

        sourceSets {
            map { it.java.srcDir("src/${it.name}/kotlin") }
        }

        compileOptions {
            sourceCompatibility = JavaOptions.VERSION
            targetCompatibility = JavaOptions.VERSION
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
    this.jvmTarget = JavaOptions.VERSION.toString()
}

task("staticCheck") {
    group = "verification"

    afterEvaluate {
        // Filter modules with "lintDebug" task (non-Android modules do not have lintDebug task)
        val lintTasks = subprojects.mapNotNull {
            if (it.name.startsWith("feature_")) {
                "${it.name}:lintAnalyzeDebug"
            } else {
                "${it.name}:lintDebug"
            }
        }.filterNot { it.startsWith("library_") }

        // Get modules with "testDebugUnitTest" task (app module does not have it)
        val testTasks = subprojects.mapNotNull { "${it.name}:testDebugUnitTest" }
            .filterNot { it == "app:testDebugUnitTest" || it.startsWith("library_") }

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
