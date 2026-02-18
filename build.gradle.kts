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
    implementation("org.graalvm.sdk:graal-sdk:$graalVersion")
    implementation("org.graalvm.python:python-embedding:$graalVersion")
}

configurations.all {
    resolutionStrategy {
        force(
            "org.graalvm.sdk:graal-sdk:$graalVersion",
            "org.graalvm.python:python-embedding:$graalVersion"
        )
    }
}



tasks {
    named<ShadowJar>("shadowJar") {
        isZip64 = true
        archiveBaseName.set("ml-svc")
        archiveClassifier.set("")
        archiveVersion.set("1.0-SNAPSHOT")

        manifest {
            attributes["Main-Class"] = "com.ml.MainKt"
        }

        // Do not bundle polyglot/truffle jars; use the GraalVM distribution at runtime.
        dependencies {
            exclude(dependency("org\\.graalvm\\..*:.*"))
            exclude(dependency("com\\.oracle\\.truffle:.*"))
        }
    }

    build {
        dependsOn(shadowJar)
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