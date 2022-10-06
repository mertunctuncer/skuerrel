plugins {
    java
    `maven-publish`
    kotlin("jvm") version "1.7.10"
}

group = "dev.peopo"
version = "1.4.0"
description = "A lightweight SQL serializer library for kotlin."

repositories {
    mavenCentral()
}

dependencies {
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.7.+")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.+")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.7.+")
}



tasks {
    compileKotlin{
        kotlinOptions.jvmTarget = "1.8"
    }
    wrapper {
        gradleVersion = "7.4"
        distributionType = Wrapper.DistributionType.ALL
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