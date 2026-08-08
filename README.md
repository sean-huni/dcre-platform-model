# dcre-platform-model

Zero-dependency shared domain vocabulary for the DCRE Collections 3.0 pipeline, published as `za.co.fnb.dcre:platform-model`.

## What it does

Bottom of the DCRE platform-library stack: it encodes register-ratified rules as plain Java types so every stage service shares one vocabulary instead of re-implementing (and drifting on) the same rules. `platform-files` builds on it via an `api` dependency, `platform-batch` builds on `platform-files`, and the stage services (collections CRR, CTV, CDE, CRW, CIR, CIX, CSX, CPX, CRG; payments PRR, PTV, PAI, PRW, PIR, PIX, PSX, PPX, PRG; mandates MRR, MRV, MAS, MIT, MIR, MRW, MIX, MSX, MPX, MRG; plus HCS) receive it transitively through those libraries. It has no runtime dependencies at all; JUnit only for tests.

The four types (package `za.co.fnb.dcre.platform.model`):

- `OpaqueRef` (R-15): opaque identity; raw fixed-width bytes kept for audit/round-trip, right-trimmed canonical value for correlation and XML. Never parsed for semantics.
- `MoneyText` (R-21): the single amount converter; `BigDecimal` from the raw digit string with an explicit scale, never floating point. Scale stays config-driven in consumers while A-1 (amount scale attestation) is open.
- `ProductType` (R-22): product dimension for cap checks (`BALANCE_CARRYING` for FNBRF, `LIMIT_CARRYING` for FNBCC), derived from the product-code prefix, never client-code strings.
- `CtvOutcome`: symbolic CTV verdict vocabulary from the GLOSSARY (`PASS` plus the `FAIL_*` set, including `FAIL_DUPLICATE_TX` per R-41); seeds the reason-code enum until the legacy DDxxx catalogue is attested (A-34).

## Architecture and principles

- SOLID: one register rule per type (single responsibility); `MoneyText` is the one and only amount converter, `OpaqueRef` the one identity carrier. Small immutable units (`record`, enums, a final utility class) with clear interfaces, each independently testable.
- 12FactorApp Alignment - https://12factor.net/: dependencies are explicitly declared and isolated (factor II); the library itself carries zero runtime dependencies, so it adds nothing to consumer classpaths. It hardcodes no config: the amount scale is an explicit parameter that consuming services source from their environment.
- Layering: pure JDK, no Spring/Quarkus/persistence imports; the layer-first packaging of the stage services sits above it. Swapping anything upstream never touches this vocabulary.
- Restart semantics support: `OpaqueRef` separates the raw fixed-width bytes (audit, byte-verbatim round-trip) from the trimmed canonical value the pipeline correlates on, which is what lets the stages' idempotent writes key on stable identities.

## Prerequisites

- Java 25 (Gradle toolchain, declared in `build.gradle`)
- Gradle wrapper included (9.5.1); no Docker, no database, no `.env`

## Quickstart

A clean clone builds and publishes with no configuration:

```bash
git clone https://github.com/sean-huni/dcre-platform-model.git
cd dcre-platform-model
./gradlew test publishToMavenLocal
```

This publishes to `~/.m2/repository/za/co/fnb/dcre/platform-model/0.1.0/`:

- `platform-model-0.1.0.jar` (classes)
- `platform-model-0.1.0-sources.jar` (via `withSourcesJar()`)
- `platform-model-0.1.0.pom` and Gradle module metadata

There is no remote repository configured; Maven Local is the whole release step. When a shared artifact repository (Nexus/Artifactory) becomes available, add it under `publishing.repositories` and publish with `./gradlew publish`.

Consuming it directly from another Gradle project:

```groovy
repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation 'za.co.fnb.dcre:platform-model:0.1.0'
}
```

Projects depending on `platform-files` (which declares `api 'za.co.fnb.dcre:platform-model:0.1.0'`) or on `platform-batch` get it transitively.

## Configuration

None. The library reads no environment variables and ships no `application.yml`. Its only configurable behavior, the `MoneyText` amount scale, is a method parameter that consuming services supply from their own configuration (kept external while A-1 is open).

## Testing

```bash
./gradlew test
```

JUnit Jupiter (BOM 6.0.2). `ModelTest` covers the raw-vs-canonical `OpaqueRef` contract, both `MoneyText` scale readings plus rejection of non-digit fields, and `ProductType` prefix mapping.

## Local cluster deployment

This library is not deployed; it is baked into the stage-service images. It is step 2 of the fleet dev quickstart, before any service image build:

```bash
# 1. dcre-infra: scripts/kind-up.sh (kind cluster dcre-dev + CRDB + exchange hostPath)
# 2. publish the platform libs to mavenLocal, model first (platform-files depends on it):
./gradlew publishToMavenLocal
# 3. then build/load stage-service images and let AGT mint their k8s Jobs
```

Releasing a change: bump `version` in `build.gradle` (published artifacts are immutable, never re-publish the same version), `./gradlew test publishToMavenLocal`, then bump the dependency version in the consumers. Fleet release tags (digits-only three-component SemVer, no `v` prefix, currently `2.1.1`) mark this repo uniformly with the rest of the fleet; the Maven artifact version is independent and is currently `0.1.0`.

## Related repositories

- Platform libraries: [dcre-platform-files](https://github.com/sean-huni/dcre-platform-files), [dcre-platform-batch](https://github.com/sean-huni/dcre-platform-batch), [dcre-platform-persistence](https://github.com/sean-huni/dcre-platform-persistence)
- Orchestrator: [dcre-agt](https://github.com/sean-huni/dcre-agt)
- Stage services: [dcre-crr](https://github.com/sean-huni/dcre-crr), [dcre-ctv](https://github.com/sean-huni/dcre-ctv), [dcre-cde](https://github.com/sean-huni/dcre-cde), [dcre-cir](https://github.com/sean-huni/dcre-cir), [dcre-crw](https://github.com/sean-huni/dcre-crw), [dcre-cix](https://github.com/sean-huni/dcre-cix), [dcre-csx](https://github.com/sean-huni/dcre-csx), [dcre-cpx](https://github.com/sean-huni/dcre-cpx), [dcre-crg](https://github.com/sean-huni/dcre-crg), [dcre-pai](https://github.com/sean-huni/dcre-pai), [dcre-hcs](https://github.com/sean-huni/dcre-hcs)
- Infrastructure and tooling: [dcre-infra](https://github.com/sean-huni/dcre-infra), [dcre-fixture-toolkit](https://github.com/sean-huni/dcre-fixture-toolkit), [dcre-design-register](https://github.com/sean-huni/dcre-design-register), [dcre-rpt](https://github.com/sean-huni/dcre-rpt)
