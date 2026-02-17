import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "2.1.10"
    id("org.graalvm.python") version "24.2.0"
    kotlin("plugin.serialization") version "2.1.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.ml"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.json:json:20231013")
    implementation("com.akuleshov7:ktoml-core:0.5.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.8.0")
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
    }

    build {
        dependsOn(shadowJar)
    }
}


graalPy {
    resourceDirectory.set("GRAALPY-VFS/com.ml/ml-svc")
    packages.set(
        listOf(
            "torch==2.2.1",
            "transformers==4.33.3",
            "numpy==1.24.3"
        )
    )
}