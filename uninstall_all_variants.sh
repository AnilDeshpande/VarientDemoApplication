#!/bin/bash

# Script to uninstall all variants of the application
# Usage: ./uninstall_all_variants.sh

echo "========================================="
echo "Uninstalling All Variants"
echo "========================================="

# Check if device is connected
if ! adb devices | grep -q "device$"; then
    echo "❌ No Android device connected!"
    echo "Please connect a device or start an emulator."
    exit 1
fi

echo "✓ Device connected"
echo ""

echo "Uninstalling all variants..."
echo "----------------------------"

success_count=0
not_installed_count=0

uninstall_package() {
    local package=$1
    local name=$2
    echo -n "Uninstalling $name... "
    if adb uninstall "$package" > /dev/null 2>&1; then
        echo "✓ Done"
        ((success_count++))
    else
        echo "⚠ Not installed"
        ((not_installed_count++))
    fi
}

# Uninstall all variants
uninstall_package "com.codetutor.varientdemo.qa.debug" "QA Debug"
uninstall_package "com.codetutor.varientdemo.qa" "QA Release"
uninstall_package "com.codetutor.varientdemo.staging.debug" "Staging Debug"
uninstall_package "com.codetutor.varientdemo.staging" "Staging Release"
uninstall_package "com.codetutor.varientdemo.debug" "Prod Debug"
uninstall_package "com.codetutor.varientdemo" "Prod Release"

echo ""
echo "========================================="
echo "Summary:"
echo "  ✓ Successfully uninstalled: $success_count"
if [ $not_installed_count -gt 0 ]; then
    echo "  ⚠ Not installed: $not_installed_count"
fi
echo "  Total variants: $((success_count + not_installed_count))"
echo "========================================="

# Verify no variants are left on device
echo ""
echo "Verifying removal..."
echo "--------------------"
remaining=0

check_removed() {
    local package=$1
    local name=$2
    if adb shell pm list packages | grep -q "$package"; then
        echo "  ⚠ $name ($package) - Still installed!"
        ((remaining++))
    fi
}

check_removed "com.codetutor.varientdemo.qa.debug" "QA Debug"
check_removed "com.codetutor.varientdemo.qa" "QA Release"
check_removed "com.codetutor.varientdemo.staging.debug" "Staging Debug"
check_removed "com.codetutor.varientdemo.staging" "Staging Release"
check_removed "com.codetutor.varientdemo.debug" "Prod Debug"
check_removed "com.codetutor.varientdemo" "Prod Release"

if [ $remaining -eq 0 ]; then
    echo "  ✓ All variants successfully removed!"
fi

echo ""
echo "Done! 🎉"

