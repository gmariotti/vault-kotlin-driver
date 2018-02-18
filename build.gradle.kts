import com.jfrog.bintray.gradle.BintrayExtension.PackageConfig
import java.io.File
import kotlin.collections.listOf
import org.gradle.api.tasks.wrapper.Wrapper.DistributionType.BIN
import org.gradle.kotlin.dsl.creating
import org.gradle.kotlin.dsl.getting
import org.gradle.kotlin.dsl.kotlin
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// -------------------------- Library Information ----------------------------

group = "com.olx.ps"
version = "0.1.0"
base {
    archivesBaseName = "vault-kotlin-driver"
}

// -------------------------- Dependencies -----------------------------------

val kotlinVersion = "1.2.21"

buildscript {
    repositories {
        jcenter()
        maven {
            setUrl("https://dl.bintray.com/kotlin/kotlin-eap")
        }
    }

    dependencies {
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:0.9.16-eap-3")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.2.21")
    }
}

apply {
    plugin("org.jetbrains.dokka")
}

plugins {
    kotlin("jvm") version "1.2.21"
    id("com.github.ben-manes.versions") version "0.17.0"
    id("com.jfrog.bintray") version "1.7.3"
    id("io.gitlab.arturbosch.detekt") version ("1.0.0.RC6-2")
    id("jacoco")
    id("java-library")
    id("maven")
    id("maven-publish")
    id("org.unbroken-dome.test-sets") version "1.4.2"
}

configurations {
    create("ktlint")
}

testSets.create("integrationTest")

repositories {
    mavenCentral()
    mavenLocal()
    jcenter()
}

val ktlint: Configuration = configurations["ktlint"]
val integrationTestCompile: Configuration = configurations["integrationTestCompile"]

dependencies {
    api(group = "com.bettercloud", name = "vault-java-driver", version = "3.0.0")
    api(group = "org.funktionale", name = "funktionale-try", version = "1.2")
    api(group = "org.slf4j", name = "slf4j-api", version = "1.7.25")

    implementation(kotlin(module = "stdlib-jdk8", version = kotlinVersion))
    implementation(kotlin(module = "reflect", version = kotlinVersion))
    implementation(group = "com.fasterxml.jackson.module", name = "jackson-module-kotlin", version = "2.9.+")

    testImplementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = "0.22.2")
    testImplementation(group = "junit", name = "junit", version = "4.12")
    testImplementation(group = "org.mockito", name = "mockito-core", version = "2.15.0")
    testImplementation(group = "com.nhaarman", name = "mockito-kotlin", version = "1.5.0")
    testImplementation(group = "io.kotlintest", name = "kotlintest", version = "2.0.7")
    testImplementation(group = "org.eclipse.jetty", name = "jetty-server", version = "9.4.8.v20171121")
    testImplementation(group = "ch.qos.logback", name = "logback-classic", version = "1.2.3")

    integrationTestCompile("org.bouncycastle:bcprov-jdk15on:1.59")
    integrationTestCompile("org.testcontainers:testcontainers:1.6.0")

    ktlint("com.github.shyiko:ktlint:0.15.1")
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
        gradleVersion = "4.5.1"
        distributionType = BIN
    }

    "ktlintCheck"(JavaExec::class) {
        description = "Runs ktlint on all kotlin sources in this project."
        group = "Ktlint"
        main = "com.github.shyiko.ktlint.Main"
        classpath = ktlint
        args = listOf("src/**/*.kt")
    }

    "ktlintFormat"(JavaExec::class) {
        description = "Runs the ktlint formatter on all kotlin sources in this project."
        group = "Ktlint"
        main = "com.github.shyiko.ktlint.Main"
        classpath = ktlint
        args = listOf("-F", "src/**/*.kt")
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

bintray {
    user = extra["bintray_user"] as String
    key = extra["bintray_key"] as String

    setPublications("maven")

    pkg(closureOf<PackageConfig> {
        repo = base.archivesBaseName
        name = base.archivesBaseName
        description = "Kotlin extension of java-vault-driver"
        vcsUrl = "https://github.com/gmariotti/vault-kotlin-driver.git"
        setLicenses("Apache-2.0")
        setLabels("kotlin", "vault", "olx")
    })
}