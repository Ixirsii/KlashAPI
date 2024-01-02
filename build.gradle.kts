plugins {
    kotlin("jvm") version "1.9.21"
    kotlin("plugin.serialization") version "1.9.21"

    alias(libs.plugins.detekt)
    alias(libs.plugins.dokka)

    jacoco
}

group = "tech.ixirsii"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    // Detekt plugins
    detektPlugins(libs.bundles.detekt)

    // Arrow
    implementation(libs.arrow.core)
    // Kotlin serialization
    implementation(libs.kotlinx.serialization.json)
    // OkHttp
    implementation(libs.okhttp)
    // Reactor
    implementation(platform(libs.reactor.bom))
    implementation(libs.reactor.core)
    implementation(libs.reactor.kotlin.extensions)
    // SLF4J
    implementation(libs.slf4j.api)

    // Kotlin test
    testImplementation(kotlin("test"))
    // JUnit testing framework
    testImplementation(libs.bundles.junit)
    testRuntimeOnly(libs.junit.platform.launcher)
    // SLF4J implementation for tests
    testImplementation(libs.logback.classic)
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom("$projectDir/config/detekt.yml")
}

jacoco {
    toolVersion = "0.8.11"
}

kotlin {
    jvmToolchain(21)
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}

tasks.detekt {
    reports {
        xml.required = true
        html.required = true
    }
}

val excludePaths: List<String> = listOf(
    "tech/ixirsii/klash/client/internal/**",
    "tech/ixirsii/klash/error/**",
    "tech/ixirsii/klash/logging/**",
    "tech/ixirsii/klash/serialize/**",
    "tech/ixirsii/klash/types/**",
)

tasks.jacocoTestCoverageVerification {
    classDirectories.setFrom(
        classDirectories.files.map {
            fileTree(it).apply {
                exclude(excludePaths)
            }
        }
    )

    violationRules {
        rule {
            element = "BUNDLE"
            limit {
                counter = "INSTRUCTION"
                value = "COVEREDRATIO"
                minimum = 0.7.toBigDecimal()
            }
        }
    }
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)

    classDirectories.setFrom(
        classDirectories.files.map {
            fileTree(it).apply {
                exclude(excludePaths)
            }
        }
    )

    reports {
        csv.required = false
        html.required = true
        xml.required = true
    }
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)

    useJUnitPlatform()
}
