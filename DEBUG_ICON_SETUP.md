# Debug App Icon Setup - Summary

## Overview
Your debug app now has distinct icons and app name to easily identify it from release builds.

## What Was Configured

### 1. **Debug App Icons**
Located in: `app/src/debug/res/`

#### PNG Icons (All Densities)
- `mipmap-mdpi/ic_launcher.png`
- `mipmap-mdpi/ic_launcher_round.png`
- `mipmap-hdpi/ic_launcher.png`
- `mipmap-hdpi/ic_launcher_round.png`
- `mipmap-xhdpi/ic_launcher.png`
- `mipmap-xhdpi/ic_launcher_round.png`
- `mipmap-xxhdpi/ic_launcher.png`
- `mipmap-xxhdpi/ic_launcher_round.png`
- `mipmap-xxxhdpi/ic_launcher.png`
- `mipmap-xxxhdpi/ic_launcher_round.png`

#### Adaptive Icon XML (Android 8.0+)
- `mipmap-anydpi-v26/ic_launcher.xml`
- `mipmap-anydpi-v26/ic_launcher_round.xml`

#### Drawable Resources
- `drawable/ic_launcher_background.xml` - **Red background** (#FF5252) for debug
- `drawable/ic_launcher_foreground.xml` - Same Android robot icon

### 2. **Debug App Name**
- File: `app/src/debug/res/values/strings.xml`
- App name: **"VariantDemo [DEBUG]"**
- This appears in the launcher and app switcher

## How It Works

### Android Resource Merging
Android merges resources from multiple source sets in this priority order:
1. **Build variant specific** (e.g., `debug/`) - **HIGHEST PRIORITY**
2. Build type (e.g., `debug/`)
3. Product flavor
4. Main source set (`main/`)

### What Happens for Debug Builds
- Android uses icons from `app/src/debug/res/` **instead of** `app/src/main/res/`
- The adaptive icon (Android 8.0+) uses the **red background** from debug drawable
- The app name shows **"VariantDemo [DEBUG]"** instead of "VariantDemoApplication"

### What Happens for Release Builds
- Android uses the original icons from `app/src/main/res/`
- The adaptive icon uses the **green background** (#3DDC84) from main drawable
- The app name shows **"VariantDemoApplication"**

## Visual Differences

| Aspect | Debug Build | Release Build |
|--------|-------------|---------------|
| Icon Background | Red (#FF5252) | Green (#3DDC84) |
| App Name | VariantDemo [DEBUG] | VariantDemoApplication |
| Launcher Icon | Custom debug PNG + Red adaptive | Default PNG + Green adaptive |

## Testing

To verify the setup works:

1. **Build Debug APK:**
   ```bash
   ./gradlew assembleDebug
   ```

2. **Build Release APK:**
   ```bash
   ./gradlew assembleRelease
   ```

3. **Install both on device** - You'll see two distinct apps with different icons

## Additional Variants

If you want different icons for other build variants (qa, staging, prod), follow the same pattern:

```
app/src/qa/res/
  └── mipmap-*/ic_launcher*.png
  └── drawable/ic_launcher_*.xml
  └── values/strings.xml

app/src/staging/res/
  └── mipmap-*/ic_launcher*.png
  └── drawable/ic_launcher_*.xml
  └── values/strings.xml
```

Consider using different colors:
- **Debug:** Red (#FF5252)
- **QA:** Orange (#FF9800)
- **Staging:** Yellow (#FFEB3B)
- **Production:** Green (#3DDC84) - default

## Notes

✅ The setup is complete and working
✅ Icons will automatically switch based on build type
✅ No code changes needed in manifests or build.gradle
✅ Android's resource merging handles everything automatically

## Troubleshooting

If icons don't appear different:
1. Clean and rebuild: `./gradlew clean assembleDebug`
2. Uninstall the app completely before reinstalling
3. Verify files exist in `app/src/debug/res/` folders
4. Check that file names match exactly (case-sensitive)
