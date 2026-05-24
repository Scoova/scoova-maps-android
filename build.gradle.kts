plugins {
    kotlin("jvm") version "2.2.0"
    `maven-publish`
    signing
}

group = "info.scoo-va"
version = "1.1.0"

repositories { mavenCentral() }

dependencies {
    implementation("org.json:json:20231013")
    testImplementation(kotlin("test"))
}

kotlin { jvmToolchain(17) }
tasks.test { useJUnitPlatform() }

java {
    withSourcesJar()
    withJavadocJar()
}

// ─── Publishing ───
publishing {
    publications {
        create<MavenPublication>("library") {
            from(components["java"])

            groupId = project.group.toString()
            artifactId = "maps"
            version = project.version.toString()

            pom {
                name.set("Scoova Maps for Android")
                description.set("Scoova map SDK for Android — MapLibre helpers (style + route + marker JSON), plus standalone static-map URL builders and a blocking PNG fetcher.")
                url.set("https://github.com/Scoova/scoova-maps-android")

                licenses {
                    license {
                        name.set("Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }

                developers {
                    developer {
                        id.set("scoova")
                        name.set("Scoova")
                        email.set("info@scoo-va.info")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/Scoova/scoova-maps-android.git")
                    developerConnection.set("scm:git:ssh://github.com:Scoova/scoova-maps-android.git")
                    url.set("https://github.com/Scoova/scoova-maps-android")
                }
            }
        }
    }

    repositories {
        // GitHub Packages
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Scoova/scoova-maps-android")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: project.findProperty("gpr.user") as? String ?: ""
                password = System.getenv("GITHUB_TOKEN") ?: project.findProperty("gpr.key") as? String ?: ""
            }
        }

        // Maven Central (when Sonatype account is ready)
        maven {
            name = "MavenCentral"
            val releasesUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsUrl else releasesUrl
            credentials {
                username = System.getenv("OSSRH_USERNAME") ?: project.findProperty("ossrh.username") as? String ?: ""
                password = System.getenv("OSSRH_PASSWORD") ?: project.findProperty("ossrh.password") as? String ?: ""
            }
        }
    }
}

// GPG signing (required for Maven Central)
signing {
    isRequired = gradle.taskGraph.hasTask("publishLibraryPublicationToMavenCentralRepository")
    sign(publishing.publications["library"])
}
