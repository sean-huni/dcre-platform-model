# dcre-platform-model

> Part of the DCRE fleet. For the fleet map, the rulings and the diagrams that specify every stage, start at the [DCRE design register](https://github.com/sean-huni/dcre-design-register); the complete list of live repositories is its [Repositories](https://github.com/sean-huni/dcre-design-register/blob/dev/README.md#repositories) table.

Zero-dependency shared domain vocabulary for the DCRE pipeline, published as
`za.co.fnb.dcre:platform-model`.

## What it does

A library, not a service: it has no runtime of its own and is baked into the stage-service images.
It is the bottom of the DCRE platform-library stack and encodes register-ratified rules as plain Java
types, so every stage shares one vocabulary instead of re-implementing (and drifting on) the same
rules. No runtime dependencies at all; JUnit only for tests.

The five types (package `za.co.fnb.dcre.platform.model`):

- `OpaqueRef` (R-15): opaque identity; `ofFixedWidth(raw)` keeps the raw fixed-width bytes for
  audit and round-trip plus a right-trimmed canonical value for correlation and XML. Never parsed for
  semantics.
- `MoneyText` (R-21): the single amount converter; `parse(raw, scale)` builds a `BigDecimal` from the
  raw digit string with an explicit scale, never floating point. Scale stays config-driven in
  consumers while A-1 (amount scale attestation) is open.
- `ProductType` (R-22): product dimension for cap checks (`BALANCE_CARRYING` for FNBRF,
  `LIMIT_CARRYING` for FNBCC), derived by `fromProductCode` from the product-code prefix, never from
  client-code strings.
- `CtvOutcome`: symbolic CTV verdict vocabulary (`PASS` plus the `FAIL_*` set, including
  `FAIL_DUPLICATE_TX` per R-41); seeds the reason-code enum until the legacy DDxxx catalogue is
  attested (A-34).
- `MandateOutcome` (M10, SCRUM-73): symbolic MRV/MAS verdict vocabulary for the mandates flow
  (`PASS`, the `FAIL_*` set, `CONTRACT_HAS_LIVE_MANDATE`, `HOLD_BUREAU_UNAVAILABLE`). Persisted by
  name and reported to OnHost through MIR NACK reasons, so a rename is a data-contract break.

## Consumers

Counted from each fleet repo's `build.gradle` on `origin/dev` (local clones, not fetched), plus
AGT (checked 2026-09-28). Only `platform-files` declares this library directly
(`api 'za.co.fnb.dcre:platform-model:0.1.0'`); every service that depends on `platform-batch`
receives it transitively through `platform-batch` -> `platform-files`. AGT (Quarkus) is not a
consumer.

Services that import its types in `src/main`:

| Type | Imported by |
|---|---|
| `MoneyText`, `OpaqueRef` | `crr`, `prr`, `mrr` |
| `CtvOutcome`, `ProductType` | `ctv`, `ptv` |
| `MandateOutcome` | `mas`, `mir`, `mrv` |

## Architecture and principles

- SOLID: one register rule per type (single responsibility); `MoneyText` is the one and only amount
  converter, `OpaqueRef` the one identity carrier. Small immutable units (a `record`, enums, a final
  utility class) with clear interfaces, each independently testable.
- 12FactorApp Alignment - https://12factor.net/: dependencies are explicitly declared and isolated
  (factor II); the library carries zero runtime dependencies, so it adds nothing to consumer
  classpaths. It hardcodes no config: the amount scale is an explicit parameter that consuming
  services source from their environment.
- Layering: pure JDK, no Spring, Quarkus or persistence imports; the layer-first packaging of the
  stage services sits above it.
- Restart semantics support: `OpaqueRef` separates the raw fixed-width bytes from the trimmed
  canonical value the pipeline correlates on, which is what lets the stages' idempotent writes key on
  stable identities.

## Prerequisites

- Java 25 (`.sdkmanrc`: `java=25-tem`; `build.gradle` sets `sourceCompatibility` /
  `targetCompatibility` 25)
- Gradle wrapper 9.5.1 (committed); no Docker, no database, no `.env`

## Build and publish

Coordinates: `za.co.fnb.dcre:platform-model:0.1.0`. Distribution is Maven Local only; no remote
repository is configured, so Maven Local is the whole release step. Publish it first: `platform-files`
depends on it.

```bash
git clone https://github.com/sean-huni/dcre-platform-model.git
cd dcre-platform-model
./gradlew test publishToMavenLocal
```

This writes `~/.m2/repository/za/co/fnb/dcre/platform-model/0.1.0/`: the classes jar, the sources jar
(`withSourcesJar()`), the POM and Gradle module metadata.

```groovy
repositories { mavenCentral(); mavenLocal() }
dependencies {
    implementation 'za.co.fnb.dcre:platform-model:0.1.0'
}
```

Releasing a change: bump `version` in `build.gradle` (published artifacts are immutable, never
re-publish the same version), `./gradlew test publishToMavenLocal`, then bump the dependency in
`platform-files` and republish the chain. Release tags (digits-only three-component SemVer, no `v` prefix) are independent of the artifact version; this repo carries 1.0.0 through 2.2.1, and tagging is not uniform across the fleet (`git ls-remote --tags`, checked 2026-09-28).

## Configuration

None. The library reads no environment variables and ships no `application.yml`. Its only
configurable behaviour, the `MoneyText` amount scale, is a method parameter that consuming services
supply from their own configuration (kept external while A-1 is open).

## Testing

```bash
./gradlew test
```

JUnit Jupiter (BOM 6.0.2). 2 test classes, 4 `@Test` methods (counted from `src/test` at HEAD):
`ModelTest` covers the raw-vs-canonical `OpaqueRef` contract, both `MoneyText` scale readings plus
rejection of non-digit fields, and `ProductType` prefix mapping; `MandateOutcomeTest` snapshots the
constant list to guard against accidental renames.

## Related repositories

The complete, current list of live DCRE repositories (stage services, orchestrator, platform libraries, infra and tooling) lives in one place: the [DCRE design register README](https://github.com/sean-huni/dcre-design-register/blob/dev/README.md#repositories). Deprecated and archived repositories are deliberately absent from it. This README does not copy that list, so it cannot drift.

- Design register: https://github.com/sean-huni/dcre-design-register (start at `docs/specs/DESIGN-REGISTER.md`; the diagrams in `docs/diagrams/` are the specification)
