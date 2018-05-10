base {
    archivesBaseName = "vault-kotlin-coroutines"
}

dependencies {
    api(project(":vault-kotlin"))
    api(group = "io.arrow-kt", name = "arrow-core", version = "0.7.1")
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = "0.22.5")

    testImplementation(project(":common-test"))
    testImplementation(group = "com.squareup.okhttp3", name = "mockwebserver", version = "3.10.0")
}