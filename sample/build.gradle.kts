/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */
buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${properties["kotlin_version"]}")
    }
}
plugins {
    application
    id("org.jetbrains.kotlin.jvm")
    id("kotlin-kapt")
    id("idea")
}

group = "com.icerockdev"
version = "0.0.1"

apply(plugin = "kotlin")

repositories {
    maven { setUrl("https://dl.bintray.com/icerockdev/backend") }
}

application {
    mainClass.set("com.icerockdev.sample.Main")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${properties["kotlin_version"]}")
    implementation("biz.paluch.logging:logstash-gelf:1.13.0")

    implementation(project(":web-utils"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
}

val jar by tasks.getting(Jar::class) {
    archiveFileName.set("sample.jar")
    destinationDirectory.set(file("${project.rootDir}/build"))
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
        attributes["Class-Path"] =
            configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.joinToString { "libs/${it.name}" }
    }
}
