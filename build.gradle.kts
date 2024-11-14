plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    id("org.jetbrains.intellij") version "1.17.4"
}

group = "tech.codemajesty"
version = "1.0.1"

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2023.2.6")
    type.set("IC") // Target IDE Platform

    plugins.set(listOf(/* Plugin Dependencies */))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("232")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN") ?: findProperty("CERTIFICATE_CHAIN").toString())
        privateKey.set(System.getenv("PRIVATE_KEY") ?: findProperty("PRIVATE_KEY").toString())
        password.set(System.getenv("PRIVATE_KEY_PASSWORD") ?: findProperty("PRIVATE_KEY_PASSWORD").toString())
    }

    publishPlugin {
        token.set(System.getenv("TOKEN") ?: findProperty("TOKEN").toString())
    }
}
