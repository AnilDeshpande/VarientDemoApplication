#!/bin/bash

# Script to uninstall and install all variants of the application
# Usage: ./install_all_variants.sh

echo "========================================="
echo "Uninstalling and Installing All Variants"
echo "========================================="

# Check if device is connected
if ! adb devices | grep -q "device$"; then
    echo "❌ No Android device connected!"
    echo "Please connect a device or start an emulator."
    exit 1
fi

echo "✓ Device connected"
echo ""

# Step 1: Uninstall all variants
echo "Step 1: Uninstalling all variants..."
echo "-----------------------------------"

uninstall_package() {
    local package=$1
    local name=$2
    echo -n "Uninstalling $name ($package)... "
    if adb uninstall "$package" > /dev/null 2>&1; then
        echo "✓ Done"
    else
        echo "⚠ Not installed or failed"
    fi
}

uninstall_package "com.codetutor.varientdemo.qa.debug" "QA Debug"
uninstall_package "com.codetutor.varientdemo.qa" "QA Release"
uninstall_package "com.codetutor.varientdemo.staging.debug" "Staging Debug"
uninstall_package "com.codetutor.varientdemo.staging" "Staging Release"
uninstall_package "com.codetutor.varientdemo.debug" "Prod Debug"
uninstall_package "com.codetutor.varientdemo" "Prod Release"

echo ""
echo "Step 2: Building all variants..."
echo "--------------------------------"
echo "Running: ./gradlew assembleDebug assembleRelease"
./gradlew assembleDebug assembleRelease

if [ $? -ne 0 ]; then
    echo "❌ Build failed! Please fix the errors and try again."
    exit 1
fi

echo ""
echo "✓ Build completed successfully"
echo ""

# Debug: Show what APKs were actually created
echo "APKs found in build directory:"
find app/build/outputs/apk -name "*.apk" -type f | while read apk; do
    echo "  - $apk"
done
echo ""

echo "Step 3: Installing all variants..."
echo "----------------------------------"
success_count=0
failed_count=0

install_variant() {
    local package=$1
    local name=$2
    local apk_path=$3

    echo "Processing $name..."
    echo "  Package: $package"
    echo "  Expected APK: $apk_path"

    if [ ! -f "$apk_path" ]; then
        echo "  ❌ APK not found!"
        # Try to find the actual APK
        local flavor_dir=$(dirname "$apk_path")
        echo "  Searching in: $flavor_dir"
        if [ -d "$flavor_dir" ]; then
            local actual_apk=$(find "$flavor_dir" -name "*.apk" -type f | head -1)
            if [ -n "$actual_apk" ]; then
                echo "  Found alternative: $actual_apk"
                apk_path="$actual_apk"
            else
                echo "  No APK files found in directory"
                echo ""
                return 1
            fi
        else
            echo "  Directory doesn't exist"
            echo ""
            return 1
        fi
    else
        echo "  ✓ APK found"
    fi

    echo -n "  Installing... "
    local install_output=$(adb install -r "$apk_path" 2>&1)
    local install_result=$?

    if [ $install_result -eq 0 ]; then
        echo "✓ Success"
        echo ""
        return 0
    else
        echo "❌ Failed"
        echo "  Error: $install_output"
        echo ""
        return 1
    fi
}

# Install all variants
install_variant "com.codetutor.varientdemo.qa.debug" "QA Debug" "app/build/outputs/apk/qa/debug/app-qa-debug.apk"
[ $? -eq 0 ] && ((success_count++)) || ((failed_count++))

install_variant "com.codetutor.varientdemo.qa" "QA Release" "app/build/outputs/apk/qa/release/app-qa-release.apk"
[ $? -eq 0 ] && ((success_count++)) || ((failed_count++))

install_variant "com.codetutor.varientdemo.staging.debug" "Staging Debug" "app/build/outputs/apk/staging/debug/app-staging-debug.apk"
[ $? -eq 0 ] && ((success_count++)) || ((failed_count++))

install_variant "com.codetutor.varientdemo.staging" "Staging Release" "app/build/outputs/apk/staging/release/app-staging-release.apk"
[ $? -eq 0 ] && ((success_count++)) || ((failed_count++))

install_variant "com.codetutor.varientdemo.debug" "Prod Debug" "app/build/outputs/apk/prod/debug/app-prod-debug.apk"
[ $? -eq 0 ] && ((success_count++)) || ((failed_count++))

install_variant "com.codetutor.varientdemo" "Prod Release" "app/build/outputs/apk/prod/release/app-prod-release.apk"
[ $? -eq 0 ] && ((success_count++)) || ((failed_count++))

echo ""
echo "========================================="
echo "Summary:"
echo "  ✓ Successfully installed: $success_count"
if [ $failed_count -gt 0 ]; then
    echo "  ❌ Failed: $failed_count"
fi
echo "  Total variants: $((success_count + failed_count))"
echo "========================================="

# List installed variants
echo ""
echo "Installed variants on device:"
echo "-----------------------------"

check_installed() {
    local package=$1
    local name=$2
    if adb shell pm list packages | grep -q "$package"; then
        echo "  ✓ $name ($package)"
    fi
}

check_installed "com.codetutor.varientdemo.qa.debug" "QA Debug"
check_installed "com.codetutor.varientdemo.qa" "QA Release"
check_installed "com.codetutor.varientdemo.staging.debug" "Staging Debug"
check_installed "com.codetutor.varientdemo.staging" "Staging Release"
check_installed "com.codetutor.varientdemo.debug" "Prod Debug"
check_installed "com.codetutor.varientdemo" "Prod Release"

echo ""
echo "Done! 🎉"

