allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
    }

    configurations {
        maybeCreate("runtimeClasspath")
        maybeCreate("provided")
    }

    val copyLibsCompileTask = tasks.create("copyLibsCompile", Copy::class.java) {
        from(configurations["runtimeClasspath"])
        into(File(project.rootDir, "build/libs"))
    }

    plugins.withId("org.jetbrains.kotlin.jvm") {
        tasks.withType(JavaCompile::class.java).all {
            dependsOn(copyLibsCompileTask)
            sourceCompatibility = JavaVersion.VERSION_11.toString()
            targetCompatibility = JavaVersion.VERSION_11.toString()
        }
    }
}

tasks.create("clean", Delete::class.java) {
    delete(rootProject.buildDir)
}
