import java.util.Base64
import kotlin.text.String

/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("kotlin-kapt")
    id("maven-publish")
    id("java-library")
    id("signing")
}

apply(plugin = "java")
apply(plugin = "kotlin")

group = "com.icerockdev"
version = "0.10.0"

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

dependencies {
    // logging
    implementation("ch.qos.logback:logback-classic:${properties["logback_version"]}")
    // ktor
    api("io.ktor:ktor-server-core:${properties["ktor_version"]}")
    implementation("io.ktor:ktor-server-netty:${properties["ktor_version"]}")

    api("joda-time:joda-time:${properties["jodatime_version"]}")

    // json
    api("io.ktor:ktor-jackson:${properties["ktor_version"]}")

    // javax
    api("javax.validation:validation-api:${properties["javax_validation"]}")
    api("org.hibernate.validator:hibernate-validator:${properties["hibernate_validator_version"]}")
    api("org.hibernate.validator:hibernate-validator-annotation-processor:${properties["hibernate_validator_annotation_processor_version"]}")
    api("javax.el:javax.el-api:${properties["javax_el_api_version"]}")
    api("org.glassfish.web:javax.el:${properties["javax_el_version"]}")
    api("commons-beanutils:commons-beanutils:${properties["beanutils_version"]}")

    // BCrypt
    implementation("at.favre.lib:bcrypt:${properties["bcrypt_version"]}")

    // i18n
    implementation("org.gnu.gettext:libintl:${properties["gnu_gettext_version"]}")

    // tests
    testImplementation("io.ktor:ktor-server-tests:${properties["ktor_version"]}")
    testImplementation("junit:junit:${properties["junit_version"]}")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    withJavadocJar()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
}

repositories {
    mavenCentral()
}

publishing {
    repositories.maven("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/") {
        name = "OSSRH"

        credentials {
            username = System.getenv("OSSRH_USER")
            password = System.getenv("OSSRH_KEY")
        }
    }
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
            artifact(sourcesJar.get())
            pom {
                name.set("Web utils")
                description.set("Ktor based web-server with validation and internationalization")
                url.set("https://github.com/icerockdev/web-utils")
                licenses {
                    license {
                        url.set("https://github.com/icerockdev/web-utils/blob/master/LICENSE.md")
                    }
                }

                developers {
                    developer {
                        id.set("YokiToki")
                        name.set("Stanislav")
                        email.set("skarakovski@icerockdev.com")
                    }

                    developer {
                        id.set("AlexeiiShvedov")
                        name.set("Alex Shvedov")
                        email.set("ashvedov@icerockdev.com")
                    }

                    developer {
                        id.set("oyakovlev")
                        name.set("Oleg Yakovlev")
                        email.set("oyakovlev@icerockdev.com")
                    }
                }

                scm {
                    connection.set("scm:git:ssh://github.com/icerockdev/web-utils.git")
                    developerConnection.set("scm:git:ssh://github.com/icerockdev/web-utils.git")
                    url.set("https://github.com/icerockdev/web-utils")
                }
            }
        }

        signing {
            val signingKeyId: String? = System.getenv("SIGNING_KEY_ID")
            val signingPassword: String? = System.getenv("SIGNING_PASSWORD")
            val signingKey: String? = System.getenv("SIGNING_KEY")?.let { base64Key ->
                String(Base64.getDecoder().decode(base64Key))
            }
            useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
            sign(publishing.publications["mavenJava"])
        }
    }
}
