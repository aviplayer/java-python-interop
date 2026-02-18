import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "2.1.10"
    id("org.graalvm.python") version "24.2.0"
    kotlin("plugin.serialization") version "2.1.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}

val graalVersion = "24.2.0"

group = "com.ml"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("com.ml.MainKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.json:json:20231013")
    implementation("com.akuleshov7:ktoml-core:0.5.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.8.0")

    // Pin every GraalVM artifact explicitly (no BOM)
    implementation("org.graalvm.sdk:graal-sdk:$graalVersion")
    implementation("org.graalvm.python:python-embedding:$graalVersion")
    implementation("org.graalvm.truffle:truffle-api:$graalVersion")
    implementation("org.graalvm.python:python-language:$graalVersion")
}

configurations.all {
    resolutionStrategy {
        eachDependency {
            val g = requested.group ?: return@eachDependency
            if (g.startsWith("org.graalvm") || g == "com.oracle.truffle") {
                useVersion(graalVersion)
            }
        }
    }
}

tasks {
    named<ShadowJar>("shadowJar") {
        isZip64 = true
        archiveBaseName.set("ml-svc")
        archiveClassifier.set("")
        archiveVersion.set("1.0-SNAPSHOT")
        manifest { attributes["Main-Class"] = "com.ml.MainKt" }
        mergeServiceFiles()
    }

    build {
        dependsOn(named("shadowJar"))
    }
}

graalPy {
    resourceDirectory.set("environment")
    packages.set(
        listOf(
            "torch==2.2.1",
            "transformers==4.33.3",
            "numpy==1.24.3",
            "huggingface_hub==0.30.2"
        )
    )
}