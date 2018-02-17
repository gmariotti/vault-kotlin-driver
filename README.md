# Vault Kotlin Driver

Kotlin library that extends the behavior of [java-vault-driver](https://github.com/BetterCloud/vault-java-driver) for 
the [Vault](https://www.vaultproject.io/) secrets management solution from HashiCorp.

This library is compatible with Java 8 and up and with Kotlin 1.2 (not tested for previous versions). The main
characteristics of the library are:
- DSL builders for `Vault`, `VaultConfig` and `SslConfig`
- `VaultWithScheduledExecutor` for periodically check a secret in Vault.
- `ModuleVault` for deserialize a `VaultConfig` using Jackson.
- extension method `Logical.read(path, constructor)` to easily build a recovered secret from Vault.

To use the driver:

```groovy
repositories {
    jcenter()
}

dependencies {
    compile("com.olx.ps:kotlin-vault-driver:0.1.0")
}
```

## Dependencies required at runtime

- `kotlin-stdlib-jdk8`
- `kotlin-reflect`
- `jackson-module-kotlin` (Needed if using `ModuleVault`)

## TODO

- [ ] Library deployment to Maven Central
- [ ] CI build
- [ ] Code coverage (Codacy or Codecov)
- [ ] Fix Dokka documentation
- [ ] Integration Test for renewable credentials
- [ ] Junit 5
