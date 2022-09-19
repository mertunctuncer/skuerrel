import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
}

group = "dev.peopo"
version = "1.0.0"
description = "A SQL serializer library for MySQL, MariaDB and PostgreSQL."

repositories {
    mavenCentral()
}

dependencies {
    // 4.0.3 is the latest version to support java 8
    implementation("com.zaxxer:HikariCP:4.0.3")

    // PostgreSQL JDBC driver
    implementation("org.postgresql:postgresql:42.5.0")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.7.10")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.7.10")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}