# Variant Matrix — VariantDemoApplication

This app has **12 build variants** produced by three independent axes:

| Axis | Values | What it controls |
|------|--------|-----------------|
| **Build Type** | `debug`, `release` | Debuggable flag, minification, signing |
| **Environment** (`env`) | `qa`, `staging`, `prod` | `BASE_URL`, backend credentials |
| **Tier** (`tier`) | `free`, `paid` | Ad visibility, feature gating |

## Full 12-Variant Matrix

| # | Environment | Tier | Build Type | Application ID | Variant Name |
|---|------------|------|------------|----------------|--------------|
| 1 | qa | free | debug | `com.codetutor.varientdemo.qa.free.debug` | `qaFreeDebug` |
| 2 | qa | free | release | `com.codetutor.varientdemo.qa.free` | `qaFreeRelease` |
| 3 | qa | paid | debug | `com.codetutor.varientdemo.qa.paid.debug` | `qaPaidDebug` |
| 4 | qa | paid | release | `com.codetutor.varientdemo.qa.paid` | `qaPaidRelease` |
| 5 | staging | free | debug | `com.codetutor.varientdemo.staging.free.debug` | `stagingFreeDebug` |
| 6 | staging | free | release | `com.codetutor.varientdemo.staging.free` | `stagingFreeRelease` |
| 7 | staging | paid | debug | `com.codetutor.varientdemo.staging.paid.debug` | `stagingPaidDebug` |
| 8 | staging | paid | release | `com.codetutor.varientdemo.staging.paid` | `stagingPaidRelease` |
| 9 | prod | free | debug | `com.codetutor.varientdemo.free.debug` | `prodFreeDebug` |
| 10 | prod | free | release | `com.codetutor.varientdemo.free` | `prodFreeRelease` |
| 11 | prod | paid | debug | `com.codetutor.varientdemo.paid.debug` | `prodPaidDebug` |
| 12 | prod | paid | release | `com.codetutor.varientdemo.paid` | `prodPaidRelease` |

## The Key Question

> Not all config needs to differ for every combination.
> That assumption is where the complexity explodes.

If you need secrets (API keys, tokens, signing credentials), do you really need
**12 separate secret setups** — or is there a smarter way to decide what should
vary and what should stay shared?

That's what this demo answers.

