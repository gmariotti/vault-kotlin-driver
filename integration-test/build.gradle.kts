dependencies {
    api(project(":vault-kotlin"))

    testImplementation("org.bouncycastle:bcprov-jdk15on:1.59")
    testImplementation("org.testcontainers:testcontainers:1.6.0")
}