import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    `maven-publish`
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

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.7.+")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.+")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.7.+")
}



tasks {
    compileKotlin{
        kotlinOptions.jvmTarget = "1.8"
    }

}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "${project.group}"
            artifactId = rootProject.name
            version = "${project.version}"

            from(components["java"])
        }
    }
}

tasks.wrapper {
    gradleVersion = "7.4"
    distributionType = Wrapper.DistributionType.ALL
}