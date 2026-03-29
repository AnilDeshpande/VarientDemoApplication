# Secret Classification — VariantDemoApplication

Before writing any Gradle or CI code, classify every piece of configuration
by **what axis it actually varies on**. This is the most important step.

## The Four Categories

| Category | Varies by… | # of values needed | Example |
|----------|-----------|-------------------|---------|
| **Common** | Nothing — same everywhere | 1 | `ANALYTICS_SDK_KEY` |
| **Environment-specific** | `env` (qa / staging / prod) — NOT by tier | 3 | `BACKEND_TOKEN` |
| **Tier-specific** | `tier` (free / paid) — NOT by env | 2 | `AD_SDK_KEY` |
| **Release-only** | `buildType == release` only | 1 | `RELEASE_SIGNING_STORE_PASSWORD` |

## Walk-Through with Concrete Examples

### 1. `ANALYTICS_SDK_KEY` → Common
The analytics SDK key is the **same** for every variant. QA, staging, prod,
free, paid, debug, release — they all report to the same analytics dashboard.
**One secret. One value. Done.**

### 2. `BACKEND_TOKEN` → Environment-specific
The backend auth token differs by **environment** (qa talks to the QA server,
staging to the staging server, prod to the production server). But within
an environment, it does **not** change by tier — `qaFreeDebug` and `qaPaidDebug`
hit the same QA backend.
**Three secrets:** `BACKEND_TOKEN_QA`, `BACKEND_TOKEN_STAGING`, `BACKEND_TOKEN_PROD`.

### 3. `AD_SDK_KEY` → Tier-specific
The ad network SDK key differs by **tier** — the `free` tier shows ads (needs
a real ad key), while the `paid` tier shows no ads (placeholder or absent).
It does **not** change by environment — `qaFreeDebug` and `prodFreeRelease`
use the same ad key.
**Two secrets:** `AD_SDK_KEY_FREE`, `AD_SDK_KEY_PAID`.

### 4. `RELEASE_SIGNING_STORE_PASSWORD` → Release-only
The keystore password is only needed when `buildType == release`. Debug
builds use the auto-generated debug keystore. Injecting signing credentials
into debug builds is unnecessary and a security anti-pattern.
**One secret**, resolved only inside the `release` block.

## Why This Matters

| Approach | Secrets needed for 4 keys across 12 variants |
|----------|----------------------------------------------|
| Naïve (one per variant per key) | 4 × 12 = **48 secrets** |
| Classified (by actual axis) | 1 + 3 + 2 + 1 = **7 secrets** |

From 48 → 7. Same app, same security, dramatically less maintenance.

## The Rule

> Inject only what the current build actually needs.
> Keep common values common.
> Only genuinely varying values are split.
> Release-only values are resolved only when the build type requires them.

