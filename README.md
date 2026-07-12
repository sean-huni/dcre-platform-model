# dcre-platform-model

Zero-dependency domain vocabulary shared by the DCRE Collections 3.0 pipeline services
(C-family and M-family, see the `dcre` umbrella repo). It is the bottom of the platform
stack: `dcre-platform-files` and `dcre-platform-batch` build on it via `api` dependencies.
It encodes register-ratified rules as types:

- `OpaqueRef` (R-15): opaque identity; raw fixed-width bytes kept for audit/round-trip,
  right-trimmed canonical value for correlation and XML. Never parsed for semantics.
- `MoneyText` (R-21): the single amount converter; `BigDecimal` from the raw digit
  string with an explicit scale, never floating point. Scale stays config-driven while
  A-1 (amount scale attestation) is open.
- `ProductType` (R-22): product dimension for cap checks (`BALANCE_CARRYING` for FNBRF,
  `LIMIT_CARRYING` for FNBCC), derived from the product-code prefix, never client-code
  strings.
- `CtvOutcome`: symbolic CTV verdict vocabulary from the GLOSSARY (PASS plus the
  FAIL_* set); seeds the reason-code enum until the legacy DDxxx catalogue is attested
  (A-34).

No runtime dependencies at all; JUnit only for tests (`ModelTest`).

## Publishing (how this module is made available for reuse)

This module is published as a Maven artifact via the Gradle `maven-publish` plugin
(see `build.gradle`) so other DCRE services can import it as a normal dependency.

Coordinates:

```
za.co.fnb.dcre:dcre-platform-model:0.1.0
```

### How it was published

1. `build.gradle` applies `java-library` + `maven-publish`, sets
   `group = 'za.co.fnb.dcre'` and `version = '0.1.0'`, and declares a single
   `MavenPublication` from `components.java`. `withSourcesJar()` publishes a
   sources jar alongside the binary jar.
2. Publish to the local Maven repository (`~/.m2/repository`):

   ```bash
   ./gradlew publishToMavenLocal
   ```

3. This produces, under `~/.m2/repository/za/co/fnb/dcre/dcre-platform-model/0.1.0/`:
   - `dcre-platform-model-0.1.0.jar` (classes)
   - `dcre-platform-model-0.1.0-sources.jar`
   - `dcre-platform-model-0.1.0.pom` (Maven metadata)
   - `dcre-platform-model-0.1.0.module` (Gradle module metadata)

There is currently no remote repository configured; distribution is Maven Local only.
Every consuming project is built on the same machine, so `publishToMavenLocal` is the
whole release step. When a shared artifact repository (e.g. Nexus/Artifactory) becomes
available, add it under `publishing.repositories` and publish with `./gradlew publish`.

### How to consume it from another project

1. Make sure the version you need exists locally (clone this repo at the matching
   commit and run `./gradlew publishToMavenLocal` if it does not).
2. In the consuming project's `build.gradle`, include `mavenLocal()` in the
   repositories and add the dependency:

   ```groovy
   repositories {
       mavenCentral()
       mavenLocal()
   }

   dependencies {
       implementation 'za.co.fnb.dcre:dcre-platform-model:0.1.0'
   }
   ```

3. Note: this module has no `compileOnly` dependencies; consumers need to provide
   nothing beyond a JVM. Projects depending on `dcre-platform-files` or
   `dcre-platform-batch` get this module transitively (both declare it via `api`).

### Releasing a new version

1. Bump `version` in `build.gradle` (SemVer; released versions are immutable, so any
   change after a release means a new version, never a re-publish of the same one).
2. Run the tests: `./gradlew test`.
3. Publish: `./gradlew publishToMavenLocal`.
4. Commit with the JIRA ticket in the title, then bump the dependency version in the
   consuming projects.

The sibling platform modules (`dcre-platform-files`, `dcre-platform-batch`,
`dcre-platform-persistence`) follow the same publish/consume flow under the same
`za.co.fnb.dcre` group.
