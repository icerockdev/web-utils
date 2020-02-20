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
    mainClassName = "com.icerockdev.sample.Main"
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${properties["kotlin_version"]}")

    implementation("com.icerockdev:web-utils:0.0.1")
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

val jar by tasks.getting(Jar::class) {
    archiveName = "sample.jar"
    destinationDir = file("${project.rootDir}/build")
    manifest {
        attributes["Main-Class"] = application.mainClassName
        attributes["Class-Path"] = configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.joinToString { "libs/${it.name}" }
    }
}
