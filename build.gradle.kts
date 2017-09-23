import org.jetbrains.dokka.gradle.DokkaTask

import org.gradle.api.tasks.wrapper.Wrapper.DistributionType.ALL
import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "com.olx.ps"
version = "0.1"
base {
    archivesBaseName = "vault-kotlin-driver"
}

// -------------------------- Dependencies -----------------------------------

val kotlinVersion = "1.2.20"

buildscript {
    repositories {
        jcenter()
        maven {
            setUrl("https://dl.bintray.com/kotlin/kotlin-eap")
        }
    }

    dependencies {
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:0.9.16-eap-2")
    }
}

apply {
    plugin("org.jetbrains.dokka")
}

plugins {
    kotlin("jvm") version "1.2.20"
    id("org.jlleitschuh.gradle.ktlint") version "2.3.0"
    id("org.unbroken-dome.test-sets") version "1.4.2"
    id("io.gitlab.arturbosch.detekt") version ("1.0.0.RC6-2")
    id("jacoco")
    id("java-library")
    id("maven")
    id("maven-publish")
}

testSets.create("integrationTest")

val integrationTestCompile = configurations["integrationTestCompile"]
    ?: throw GradleException("Error finding integrationTestCompile")

repositories {
    mavenCentral()
    mavenLocal()
    jcenter()
}

dependencies {
    api(group = "com.bettercloud", name = "vault-java-driver", version = "3.0.0")
    implementation(kotlin(module = "stdlib-jre8", version = kotlinVersion))
    implementation(kotlin(module = "reflect", version = kotlinVersion))
    implementation(group = "com.fasterxml.jackson.module", name = "jackson-module-kotlin", version = "2.9.2")
    implementation(group = "org.funktionale", name = "funktionale-try", version = "1.2")
    implementation(group = "org.slf4j", name = "slf4j-api", version = "1.7.25")
    testImplementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = "0.21")
    testImplementation(group = "junit", name = "junit", version = "4.12")
    testImplementation(group = "org.mockito", name = "mockito-core", version = "2.10.0")
    testImplementation(group = "com.nhaarman", name = "mockito-kotlin", version = "1.5.0")
    testImplementation(group = "io.kotlintest", name = "kotlintest", version = "2.0.7")
    testImplementation(group = "org.eclipse.jetty", name = "jetty-server", version = "9.4.7.v20170914")
    testImplementation(group = "ch.qos.logback", name = "logback-classic", version = "1.2.3")
    integrationTestCompile("org.bouncycastle:bcprov-jdk15on:1.58")
    integrationTestCompile("org.testcontainers:testcontainers:1.4.3")
}

// -------------------------- Tasks Setup -----------------------------------

kotlin {
    experimental.coroutines = Coroutines.ENABLE
}

tasks.withType<KotlinCompile>().all {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

detekt {
    profile("main", Action {
        this.input = "$projectDir/src"
        this.config = "$projectDir/detekt.yml"
    })
}

tasks {
    "wrapper"(Wrapper::class) {
        gradleVersion = "4.4.1"
        distributionType = ALL
    }
}

tasks.withType<JacocoReport> {
    reports {
        xml.apply {
            isEnabled = true
            destination = File("$buildDir/reports/jacoco/jacoco.xml")
        }
    }
}

// -------------------------- Packages creation -----------------------------------

val dokka by tasks.getting(DokkaTask::class) {
    outputFormat = "html"
    outputDirectory = "$buildDir/javadoc"

    dependsOn("javadoc")
}

val sourceJar by tasks.creating(Jar::class) {
    classifier = "sources"
    from(java.sourceSets["main"].allSource)
    dependsOn("classes")
}

val javadocJar by tasks.creating(Jar::class) {
    classifier = "javadoc"
    from(dokka.outputDirectory)
    dependsOn("dokka")
}

artifacts {
    add("archives", sourceJar)
    add("archives", javadocJar)
}

publishing {
    publications.create("maven", MavenPublication::class.java) {
        groupId = group as String
        artifactId = base.archivesBaseName
        version = project.version as String

        from(components.getByName("java"))
        artifact(sourceJar)
        artifact(javadocJar)
    }
}