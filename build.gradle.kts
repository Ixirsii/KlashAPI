plugins {
    kotlin("jvm") version "1.9.21"
    kotlin("plugin.serialization") version "1.9.21"

    id("org.jetbrains.dokka") version "1.9.10"

    jacoco
}

group = "tech.ixirsii"
version = "0.0.1"

repositories {
    mavenCentral()
}

val arrowVersion: String by project
val dokkaVersion: String by project
val junitVersion: String by project
val kotlinxSerializationVersion: String by project
val logbackVersion: String by project
val okhttpVersion: String by project
val reactorKotlinExtensionVersion: String by project
val reactorVersion: String by project
val slf4JVersion: String by project

dependencies {
    // Arrow
    implementation("io.arrow-kt:arrow-core:$arrowVersion")
    // Kotlin serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:$okhttpVersion")
    // Reactor
    implementation(platform("io.projectreactor:reactor-bom:$reactorVersion"))
    implementation("io.projectreactor:reactor-core")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:reactorKotlinExtensionsVersion")
    // SLF4J
    implementation("org.slf4j:slf4j-api:$slf4JVersion")

    // JUnit testing framework
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    // Kotlin test
    testImplementation(kotlin("test"))
    // SLF4J implementation for tests
    testImplementation("ch.qos.logback:logback-classic:$logbackVersion")
}

jacoco {
    toolVersion = "0.8.11"
}

kotlin {
    jvmToolchain(21)
}

val excludePaths: List<String> = listOf("tech/ixirsii/types/**")

tasks.jacocoTestReport {
    classDirectories.setFrom(
        classDirectories.files.map {
            fileTree(it).apply {
                exclude(excludePaths)
            }
        }
    )
    reports {
        csv.required = false
        xml.required = true
        xml.outputLocation = file("${buildDir}/reports/jacoco/report.xml")
        html.outputLocation = file("${buildDir}/reports/jacoco")
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            excludes = excludePaths
            limit {
                minimum = 0.1.toBigDecimal()
            }
        }
    }
}

tasks.jacocoTestReport {
    description = "Generates Code coverage report."
    dependsOn(tasks.test)

    val reportExclusions = excludePaths.map {
        it.replace('.', '/')
    }

    classDirectories.setFrom(
        classDirectories.files.map {
            fileTree(it).apply {
                exclude(reportExclusions)
            }
        }
    )

    reports {
        csv.required.set(false)
        html.required.set(true)
        xml.required.set(true)
    }
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)

    useJUnitPlatform()
}
