# GitHub Actions Workflow Overview

This document explains the four Android GitHub Actions workflow files in logical order.
It is meant as a broad, human-readable guide rather than a line-by-line YAML reference.

## 1. `android-ci.yml`

```text
      _______________________________
     /                               \
    |   MANUAL VARIANT BUILD SWITCH   |
    |_________________________________|
          |        |         |
          v        v         v
       Branch   Variant   Secrets
          \        |        /
           \       v       /
            +-------------+
            | Gradle Build|
            +-------------+
                   |
                   v
              APK Artifact
```

Broadly speaking, this workflow is the flexible manual builder.

Logical steps:

1. A user manually starts the workflow from GitHub Actions.
2. The user chooses which branch to build from.
3. The user chooses a specific Android variant, such as `qaFreeDebug`, `stagingPaidDebug`, or `prodPaidRelease`.
4. The workflow checks out the selected branch.
5. It reads the variant name and classifies it into:
   - environment: QA, STAGING, or PROD
   - tier: FREE or PAID
   - build type: debug or release
6. It sets up JDK 21.
7. It injects the required secrets for analytics, backend tokens, and ad SDK keys.
8. If the selected variant is a release build, it also injects release signing secrets.
9. It converts the selected variant into a Gradle task, such as `assembleQaFreeDebug`.
10. It runs the Gradle build.
11. It uploads the generated APK as a GitHub Actions artifact.

In short: use this when you want to manually build a specific variant from a specific branch.

## 2. `android-pr-checks.yml`

```text
        Pull Request to main
                |
                v
        +----------------+
        | Checkout Code  |
        +----------------+
                |
                v
        +----------------+
        | Set up JDK 21  |
        +----------------+
                |
                v
    +-------------------------+
    | Test + Lint + Debug APK |
    +-------------------------+
                |
                v
          PR Confidence
```

Broadly speaking, this workflow protects the `main` branch from broken pull requests.

Logical steps:

1. The workflow runs when a pull request targets `main`.
2. It can also be started manually.
3. It checks out the pull request code.
4. It sets up JDK 21.
5. It uses safe placeholder values for required app configuration.
6. It does not require production secrets or release signing credentials.
7. It runs unit tests for the QA free debug variant.
8. It runs lint for the QA free debug variant.
9. It builds the QA free debug APK.

In short: use this as the pull request safety check before merging into `main`.

## 3. `android-main-build.yml`

```text
             Push to main
                 |
                 v
        +------------------+
        | Integration Run  |
        +------------------+
                 |
                 v
      +-----------------------+
      | Build Debug Variants  |
      +-----------------------+
          |               |
          v               v
    QA Free Debug   Staging Paid Debug
          \               /
           \             /
            v           v
           Debug APK Artifacts
```

Broadly speaking, this workflow verifies that the merged `main` branch still builds successfully.

Logical steps:

1. The workflow runs when code is pushed to `main`.
2. It can also be started manually.
3. It checks out the latest code from `main`.
4. It sets up JDK 21.
5. It uses demo placeholder values for required configuration.
6. It builds representative debug variants:
   - `assembleQaFreeDebug`
   - `assembleStagingPaidDebug`
7. It uploads the generated debug APKs as artifacts.

In short: use this as the post-merge confidence build for the main branch.

## 4. `android-release-build.yml`

```text
           Manual Release Start
                   |
                   v
        +-----------------------+
        | Choose Release Flavor |
        +-----------------------+
             |             |
             v             v
      prodFreeRelease  prodPaidRelease
             \             /
              \           /
               v         v
        +-----------------------+
        | Production Secrets    |
        | Release Signing       |
        +-----------------------+
                   |
                   v
            Signed Release APK
```

Broadly speaking, this workflow creates trusted signed release builds.

Logical steps:

1. A user manually starts the workflow from GitHub Actions.
2. The user chooses one production release variant:
   - `prodFreeRelease`
   - `prodPaidRelease`
3. The workflow runs in the `production` GitHub environment.
4. It checks out the code.
5. It sets up JDK 21.
6. It injects real production-related secrets.
7. It injects release signing secrets.
8. It converts the selected release variant into a Gradle task, such as `assembleProdPaidRelease`.
9. It runs the signed release build.
10. It uploads the signed APK as a GitHub Actions artifact.

In short: use this when you want to produce a signed production APK.

## Overall Flow

```text
        Developer wants a custom build
                    |
                    v
          1. android-ci.yml


        Developer opens a PR to main
                    |
                    v
       2. android-pr-checks.yml


        PR is merged into main
                    |
                    v
        3. android-main-build.yml


        Team is ready for release
                    |
                    v
      4. android-release-build.yml
```

At a high level:

1. `android-ci.yml` is for manual variant builds.
2. `android-pr-checks.yml` is for pull request validation.
3. `android-main-build.yml` is for post-merge main branch confidence.
4. `android-release-build.yml` is for signed production release artifacts.
