plugins {
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.serialization") version "1.9.21"

    alias(libs.plugins.detekt)
    alias(libs.plugins.dokka)
    alias(libs.plugins.nexus.publish)

    jacoco
    `maven-publish`
    signing
}

group = "tech.ixirsii"
version = "1.0.0"

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

java {
    withJavadocJar()
    withSourcesJar()
}

kotlin {
    explicitApi()
    jvmToolchain(21)
}

nexusPublishing {
    val sonatypeUsername: String? by project
    val sonatypePassword: String? by project

    repositories {
        sonatype {
            nexusUrl = uri("https://s01.oss.sonatype.org/service/local/")
            snapshotRepositoryUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            username = sonatypeUsername
            password = sonatypePassword
        }
    }
}

publishing {

    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            pom {
                groupId = "tech.ixirsii"
                name = "KlashAPI"
                description = "KlashAPI is a Kotlin library for the Clash of Clans API."
                url = "https://github.com/Ixirsii/KlashAPI"
                developers {
                    developer {
                        id = "Ixirsii"
                        name = "Ryan Porterfield"
                        email = "ixirsii@ixirsii.tech"
                    }
                }
                licenses {
                    license {
                        name = "BSD 3-Clause"
                        url = "https://opensource.org/license/bsd-3-clause/"
                    }
                }
                scm {
                    connection = "scm:git:git@github.com:Ixirsii/KlashAPI.git"
                    developerConnection = "scm:git:git@github.com:Ixirsii/KlashAPI.git"
                    url = "https://github.com/Ixirsii/KlashAPI.git"
                }
            }
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/ixirsii/KlashAPI")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)

    sign(publishing.publications)
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

tasks.javadoc {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)

    useJUnitPlatform()
}
