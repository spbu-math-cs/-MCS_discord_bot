import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.5.10"
    id("com.github.johnrengelman.shadow") version "5.1.0"
}

group = "me.artem"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("net.dv8tion:JDA:5.0.0-alpha.18")
    implementation("org.apache.logging.log4j:log4j-api:2.19.0")
    implementation ("org.apache.logging.log4j:log4j-core:2.19.0")
    implementation("org.slf4j:slf4j-simple:2.0.3")
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<ShadowJar>() {
    manifest {
        attributes["Main-Class"] = "MCSbotKt"
    }
}
