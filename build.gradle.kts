import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.MetricType

plugins {
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.serialization") version "1.9.21"

    alias(libs.plugins.detekt)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kover)
    alias(libs.plugins.nexus.publish)

    `maven-publish`
    signing
}

group = "tech.ixirsii"
version = "1.0.1"

repositories {
    mavenCentral()
}

dependencies {
    // Detekt plugins
    detektPlugins(libs.bundles.detekt)

    // Arrow
    api(libs.arrow.core)
    // Kotlin serialization
    api(libs.kotlinx.serialization.json)
    // OkHttp
    api(libs.okhttp)
    // Reactor
    api(platform(libs.reactor.bom))
    api(libs.reactor.core)
    api(libs.reactor.kotlin.extensions)
    // SLF4J
    api(libs.slf4j.api)

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

java {
    withJavadocJar()
    withSourcesJar()
}

kotlin {
    explicitApi()
    jvmToolchain(21)
}

koverReport {
    defaults {
        xml {
            onCheck = true
        }

        html {
            onCheck = true
        }

        verify {
            onCheck = true
        }

        log {
            onCheck = true
        }
    }

    filters {
        excludes {
            packages(
                "tech.ixirsii.klash.client.internal",
                "tech.ixirsii.klash.error",
                "tech.ixirsii.klash.logging",
                "tech.ixirsii.klash.serialize",
                "tech.ixirsii.klash.types",
            )
        }
    }

    verify {
        rule("Line coverage") {
            isEnabled = true
            minBound(aggregation = AggregationType.COVERED_PERCENTAGE, metric = MetricType.LINE, minValue = 80)
        }

        rule("Branch coverage") {
            isEnabled = true
            minBound(aggregation = AggregationType.COVERED_PERCENTAGE, metric = MetricType.BRANCH, minValue = 70)
        }
    }
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
                name = "klash-api"
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

tasks.javadoc {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}

tasks.test {
    useJUnitPlatform()
}
