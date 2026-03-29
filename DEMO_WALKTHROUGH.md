# Demo Walk-Through — Video 3

Use this as a narration cheat-sheet when recording. Run three variants
side-by-side and point out what changes, what stays the same, and why.

## Three Representative Variants

| | `qaFreeDebug` | `stagingPaidDebug` | `prodPaidRelease` |
|---|---|---|---|
| **Environment** | QA | Staging | Prod |
| **Tier** | Free | Paid | Paid |
| **Build Type** | Debug | Debug | Release |
| **APPLICATION_ID** | `…qa.free.debug` | `…staging.paid.debug` | `…paid` |
| **BASE_URL** | `qa.api.example.com` | `staging.api.example.com` | `api.example.com` |

### Secrets — what changes, what stays the same

| Secret | Category | `qaFreeDebug` | `stagingPaidDebug` | `prodPaidRelease` | Same or Different? |
|--------|----------|---|---|---|---|
| `ANALYTICS_SDK_KEY` | Common | `demo-…-abc123` | `demo-…-abc123` | `demo-…-abc123` | ✅ **Same** |
| `BACKEND_TOKEN` | Env-specific | `…qa-token-111` | `…staging-token-222` | `…prod-token-333` | 🔀 **Differs by env** |
| `AD_SDK_KEY` | Tier-specific | `…free-444` | `…paid-555` | `…paid-555` | 🔀 **Differs by tier** |
| Signing configured? | Release-only | ❌ Not needed | ❌ Not needed | ✅ Active | 🔒 **Release only** |

### Narration Script

1. **Open the app for `qaFreeDebug`** — point at the Secrets section.
   - Analytics key: same as every other variant (common).
   - Backend token: ends `111` — that's the QA backend.
   - Ad SDK key: ends `444` — that's the free-tier ad key.
   - Signing: "Not needed for debug builds."

2. **Switch to `stagingPaidDebug`** — compare.
   - Analytics key: **still the same** — common secret, one value.
   - Backend token: now ends `222` — staging backend, **different env**.
   - Ad SDK key: now ends `555` — paid-tier key, **different tier**.
   - Signing: still not needed — it's debug.

3. **Switch to `prodPaidRelease`** — the release build.
   - Analytics key: **still the same**.
   - Backend token: ends `333` — production backend.
   - Ad SDK key: ends `555` — same as `stagingPaidDebug` (same tier!).
   - Signing: "✅ Release signing active" — **this is the only variant that needs it**.

4. **Zoom out** — recap the numbers:
   - Naïve approach: 4 keys × 12 variants = **48 secrets**.
   - Classified approach: 1 + 3 + 2 + 1 = **7 secrets**. Same security. Done.

## The Reusable Method (Section H Summary)

1. **List your variant axes** — build type, environment, tier.
2. **Classify what is actually secret** — not all config is sensitive.
3. **Classify what actually varies** — by which axis, not by every combination.
4. **Keep common values common** — one secret, one value, everywhere.
5. **Inject only what the current build needs** — release-only stays release-only.
6. **Keep naming consistent and readable** — `BACKEND_TOKEN_QA`, not `SECRET_7`.

## GitHub Secrets Inventory (7 total)

| # | Secret Name | Category | Why |
|---|-------------|----------|-----|
| 1 | `ANALYTICS_SDK_KEY` | Common | Same for all 12 variants |
| 2 | `BACKEND_TOKEN_QA` | Env-specific | QA backend auth |
| 3 | `BACKEND_TOKEN_STAGING` | Env-specific | Staging backend auth |
| 4 | `BACKEND_TOKEN_PROD` | Env-specific | Prod backend auth |
| 5 | `AD_SDK_KEY_FREE` | Tier-specific | Free tier ad network |
| 6 | `AD_SDK_KEY_PAID` | Tier-specific | Paid tier (placeholder) |
| 7 | `RELEASE_SIGNING_STORE_PASSWORD` | Release-only | Keystore credentials |

Plus 3 more signing-related secrets (`KEY_ALIAS`, `KEY_PASSWORD`, `STORE_FILE`)
that are all in the same release-only category = **10 total GitHub Secrets**.

Compare to 48+ if you went naïve. And the naming tells you exactly what each one is for.

---

## Learnings — Pitfalls We Hit (So You Don't Have To)

While building this demo we ran into three real CI failures. Each one
teaches something important about how Gradle, GitHub Actions, and
Android build variants interact. These are mistakes mid-level developers
routinely make — and now you know how to avoid them.

### 1. 🔴 `workflow_dispatch` requires the YAML on the target branch too

**What happened:** We put `android-ci.yml` on `main` (the default branch)
so GitHub would show it in the Actions tab. When we selected a different
branch in the "Use workflow from" dropdown, GitHub showed:

> *"Workflow does not exist or does not have a workflow_dispatch trigger
> in this branch."*

The build either refused to run or silently checked out `main` instead,
which didn't have the product flavors → `Task 'assembleQaFreeDebug' not found`.

**Root cause:** GitHub Actions discovers workflows from the default branch,
but when you trigger `workflow_dispatch` on a different branch, it expects
the YAML file to **also exist on that branch**.

**Fix:** Add an explicit `branch` text input to the workflow and pass it
to `actions/checkout` via `ref:`. The workflow always runs from `main`
(no branch-dropdown confusion) and explicitly checks out the branch you type:

```yaml
inputs:
  branch:
    description: 'Branch to build from'
    required: true
    type: string
    default: 'secrets-demo-video-three'

steps:
  - uses: actions/checkout@v5
    with:
      ref: ${{ inputs.branch }}
```

**Takeaway:** Don't rely on the branch dropdown for cross-branch builds.
Use an explicit `ref:` in checkout.

---

### 2. 🔴 `signingConfigs` runs at configuration time — even for debug builds

**What happened:** CI failed with:

> *`Cannot convert '' to File.`* (line 103, `build.gradle.kts`)

We had `storeFile = file(resolveSecret("RELEASE_SIGNING_STORE_FILE"))` inside
`signingConfigs { create("release") { … } }`. On CI, debug builds received
`RELEASE_SIGNING_STORE_FILE=''` (empty string). `resolveSecret` returned `''`,
and `file('')` threw.

**Root cause:** `signingConfigs` is evaluated during **Gradle configuration
phase** — it runs for **every** variant, not just the one you're building.
So even `qaFreeDebug` triggers the release signing config evaluation.

**Fix:**
- `resolveSecret` now ignores blank env vars (`takeIf { it.isNotBlank() }`).
- Added `resolveSecretOrNull` for optional secrets.
- Signing config uses `resolveSecretOrNull` and skips when absent:

```kotlin
signingConfigs {
    create("release") {
        val storeFilePath = resolveSecretOrNull("RELEASE_SIGNING_STORE_FILE")
        if (storeFilePath != null) {
            storeFile = file(storeFilePath)
            storePassword = resolveSecretOrNull("RELEASE_SIGNING_STORE_PASSWORD") ?: ""
            keyAlias = resolveSecretOrNull("RELEASE_SIGNING_KEY_ALIAS") ?: ""
            keyPassword = resolveSecretOrNull("RELEASE_SIGNING_KEY_PASSWORD") ?: ""
        }
    }
}
```

**Takeaway:** Never call `file(...)` or `error(...)` unconditionally inside
`signingConfigs`. Use a nullable resolver and guard with `if != null`.

---

### 3. 🔴 Gradle configures ALL product flavors — not just the active variant

**What happened:** CI failed with:

> *`Missing secret: BACKEND_TOKEN_STAGING`* — while building `qaFreeDebug`.

We had the workflow inject only the env-specific secret for the current
environment (e.g. only `BACKEND_TOKEN_QA` for a QA build). But Gradle
still evaluated the `staging` and `prod` flavor blocks, which called
`resolveSecret("BACKEND_TOKEN_STAGING")` and
`resolveSecret("BACKEND_TOKEN_PROD")` → fail-fast error.

**Root cause:** All `productFlavors { … }` blocks run at **Gradle
configuration time**, regardless of which variant is being assembled.
This is a fundamental Gradle/AGP behavior.

**Fix:** The workflow injects **all** common, env-specific, and tier-specific
secrets — always. Only release-only signing secrets stay conditional:

```yaml
env:
  # Always inject all flavor secrets — Gradle needs them all
  ANALYTICS_SDK_KEY:     ${{ secrets.ANALYTICS_SDK_KEY }}
  BACKEND_TOKEN_QA:      ${{ secrets.BACKEND_TOKEN_QA }}
  BACKEND_TOKEN_STAGING: ${{ secrets.BACKEND_TOKEN_STAGING }}
  BACKEND_TOKEN_PROD:    ${{ secrets.BACKEND_TOKEN_PROD }}
  AD_SDK_KEY_FREE:       ${{ secrets.AD_SDK_KEY_FREE }}
  AD_SDK_KEY_PAID:       ${{ secrets.AD_SDK_KEY_PAID }}
  # Only signing is truly conditional
  RELEASE_SIGNING_STORE_FILE: ${{ … conditional … }}
```

**Takeaway:** The classification reduces how many **GitHub Secrets** you
maintain (7 instead of 48), not how many env vars you pass per CI run.
All flavor secrets must be present at configuration time. Only signing
(which uses `resolveSecretOrNull`) can be truly conditional.

---

### Summary Table

| Pitfall | Symptom | Root Cause | Fix |
|---------|---------|------------|-----|
| YAML not on target branch | "Workflow does not exist" | `workflow_dispatch` needs YAML on both branches | Use explicit `branch` input + `ref:` in checkout |
| `file('')` in signingConfigs | "Cannot convert '' to File" | `signingConfigs` runs at config time for all variants | Use `resolveSecretOrNull` + null guard |
| Missing flavor secrets | "Missing secret: BACKEND_TOKEN_STAGING" | ALL `productFlavors` run at config time | Inject all flavor secrets, not just the active one |

