import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "dev.minjae.loginpacketspammer"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        name = "opencollab-releases"
        url = uri("https://repo.opencollab.dev/maven-releases/")
    }
    maven {
        name = "opencollab-snapshots"
        url = uri("https://repo.opencollab.dev/maven-snapshots/")
    }
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.nukkitx.protocol:bedrock-v560:2.9.15-SNAPSHOT")
    implementation("ch.qos.logback:logback-classic:1.4.5")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        manifest {
            attributes(mapOf("Main-Class" to "dev.minjae.loginpacketspammer.MainKt"))
        }
    }
}
