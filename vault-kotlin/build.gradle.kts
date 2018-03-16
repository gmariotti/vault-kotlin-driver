import com.jfrog.bintray.gradle.BintrayExtension
import org.jetbrains.dokka.gradle.DokkaTask

apply {
    plugin("org.jetbrains.dokka")
}

plugins {
    id("base")
    id("com.jfrog.bintray")
    id("jacoco")
    id("maven")
    id("maven-publish")
}

base {
    archivesBaseName = "vault-kotlin-driver"
}

dependencies {
    api(group = "com.bettercloud", name = "vault-java-driver", version = "3.0.0")
    api(group = "org.funktionale", name = "funktionale-try", version = "1.2")
    api(group = "org.slf4j", name = "slf4j-api", version = "1.7.25")

    implementation(group = "com.fasterxml.jackson.module", name = "jackson-module-kotlin", version = "2.9.+")
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

    pkg(closureOf<BintrayExtension.PackageConfig> {
        repo = base.archivesBaseName
        name = base.archivesBaseName
        description = "Kotlin extension of java-vault-driver"
        vcsUrl = "https://github.com/gmariotti/vault-kotlin-driver.git"
        setLicenses("Apache-2.0")
        setLabels("kotlin", "vault", "olx")
    })
}