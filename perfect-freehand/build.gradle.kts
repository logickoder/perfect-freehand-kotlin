@file:Suppress("DSL_SCOPE_VIOLATION")

plugins {
    id("java-library")
    id("maven-publish")
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

// Define the sources Jar task
val sourcesJar = tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets.getByName("main").allSource)
}

// Define the Javadoc Jar task
val javadocJar = tasks.register<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    from(tasks.getByName("javadoc").outputs.files)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            groupId = "com.github.logickoder"
            artifactId = "perfect-freehand"
            version = "1.0.0"

            artifact(sourcesJar.get())
            artifact(javadocJar.get())
        }
    }
}

dependencies {
//    implementation(libs.perfect.freehand)
}