/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("kotlin-kapt")
    id("maven-publish")
    id("java-library")
}

apply(plugin = "java")
apply(plugin = "kotlin")

group = "com.icerockdev"
version = "0.5.1"

val sourcesJar by tasks.registering(Jar::class) {
    classifier = "sources"
    from(sourceSets.main.get().allSource)
}

dependencies {
    // kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${properties["kotlin_version"]}")
    // logging
    implementation("ch.qos.logback:logback-classic:${properties["logback_version"]}")
    // ktor
    api("io.ktor:ktor-server-core:${properties["ktor_version"]}")
    implementation("io.ktor:ktor-server-netty:${properties["ktor_version"]}")

    api("joda-time:joda-time:${properties["jodatime_version"]}")

    // json
    api("io.ktor:ktor-jackson:${properties["ktor_version"]}")
    api("io.ktor:ktor-client-jackson:${properties["ktor_version"]}")

    // javax
    api("javax.validation:validation-api:${properties["javax_validation"]}")
    api("org.hibernate.validator:hibernate-validator:${properties["hibernate_validator_version"]}")
    api("org.hibernate.validator:hibernate-validator-annotation-processor:${properties["hibernate_validator_annotation_processor_version"]}")
    api("javax.el:javax.el-api:${properties["javax_el_api_version"]}")
    api("org.glassfish.web:javax.el:${properties["javax_el_version"]}")
    api("commons-beanutils:commons-beanutils:${properties["beanutils_version"]}")

    // BCrypt
    implementation(group = "org.springframework.security", name = "spring-security-core", version = properties["spring_core_version"].toString())

    // i18n
    implementation("org.gnu.gettext:libintl:${properties["gnu_gettext_version"]}")

    // tests
    testImplementation("io.ktor:ktor-server-tests:${properties["ktor_version"]}")
    testImplementation("junit:junit:${properties["junit_version"]}")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

repositories {
    mavenCentral()
}

publishing {
    repositories.maven("https://api.bintray.com/maven/icerockdev/backend/web-utils/;publish=1") {
        name = "bintray"

        credentials {
            username = System.getProperty("BINTRAY_USER")
            password = System.getProperty("BINTRAY_KEY")
        }
    }
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
            artifact(sourcesJar.get())
        }
    }
}
