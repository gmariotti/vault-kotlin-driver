dependencies {
    api(project(":vault-kotlin-coroutines"))

    testImplementation("org.bouncycastle:bcprov-jdk15on:1.59")
    testImplementation("org.testcontainers:testcontainers:1.6.0")
    testImplementation("org.testcontainers:vault:1.4.3")
}