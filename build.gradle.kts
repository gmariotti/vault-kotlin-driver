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

// -------------------------- Dependencies -----------------------------------

buildscript {
    repositories {
        jcenter()
    }

    dependencies {
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:0.9.16")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.2.30")
    }
}

plugins {
    kotlin("jvm") version "1.2.30"
    id("com.github.ben-manes.versions") version "0.17.0"
    id("com.jfrog.bintray") version "1.8.0"
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

repositories {
    jcenter()
}

val ktlint: Configuration = configurations["ktlint"]

dependencies {
    ktlint("com.github.shyiko:ktlint:0.18.0")
}

// -------------------------- Tasks Setup -----------------------------------

detekt {
    profile("main", Action {
        this.input = "$projectDir/src"
        this.config = "$projectDir/detekt.yml"
    })
}

tasks {
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

subprojects {
    // -------------------------- Library Information ----------------------------

    group = "com.olx.ps"
    version = "0.2.0"

    // -------------------------- Dependencies -----------------------------------

    apply {
        plugin("kotlin")
        plugin("java-library")
    }

    repositories {
        mavenCentral()
        mavenLocal()
        jcenter()
    }

    val kotlinVersion = "1.2.30"

    dependencies {
        implementation(kotlin(module = "stdlib-jdk8", version = kotlinVersion))
        implementation(kotlin(module = "reflect", version = kotlinVersion))

        testImplementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = "0.22.3")
        testImplementation(group = "junit", name = "junit", version = "4.12")
        testImplementation(group = "org.mockito", name = "mockito-core", version = "2.15.0")
        testImplementation(group = "com.nhaarman", name = "mockito-kotlin", version = "1.5.0")
        testImplementation(group = "io.kotlintest", name = "kotlintest", version = "2.0.7")
        testImplementation(group = "org.eclipse.jetty", name = "jetty-server", version = "9.4.8.v20171121")
        testImplementation(group = "ch.qos.logback", name = "logback-classic", version = "1.2.3")
    }

    // -------------------------- Tasks Setup -----------------------------------

    kotlin {
        experimental.coroutines = Coroutines.ENABLE
    }

    tasks.withType<KotlinCompile>().all {
        kotlinOptions.jvmTarget = "1.8"
    }
}