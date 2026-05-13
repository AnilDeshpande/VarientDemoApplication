#!/bin/bash

echo "🧹 Complete Clean Reinstall of All Variants"
echo "=========================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Check if device is connected
echo "Checking for connected devices..."
DEVICE_COUNT=$(adb devices | grep -v "List of devices" | grep "device$" | wc -l | tr -d ' ')

if [ "$DEVICE_COUNT" -eq "0" ]; then
    echo -e "${RED}❌ No device connected. Please start your emulator or connect a device.${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Device connected${NC}"
echo ""

# Step 1: Uninstall all variants
echo "Step 1: Uninstalling all existing variants..."
./uninstall_all_variants.sh
echo ""

# Step 2: Clear launcher cache
echo "Step 2: Clearing launcher cache..."
adb shell pm clear com.google.android.apps.nexuslauncher 2>/dev/null || \
adb shell pm clear com.android.launcher3 2>/dev/null || \
adb shell pm clear com.google.android.launcher 2>/dev/null || \
echo -e "${YELLOW}⚠ Could not clear launcher cache automatically${NC}"
echo ""

# Step 3: Clean project
echo "Step 3: Cleaning project..."
./gradlew clean
echo -e "${GREEN}✓ Project cleaned${NC}"
echo ""

# Step 4: Build APKs
echo "Step 4: Building all variants..."
./gradlew assembleDebug assembleRelease -x lint
if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Build failed${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Build successful${NC}"
echo ""

# Step 5: Install all variants
echo "Step 5: Installing all variants..."
./install_all_variants.sh
echo ""

# Step 6: Restart launcher
echo "Step 6: Restarting launcher to refresh app list..."
adb shell am force-stop com.google.android.apps.nexuslauncher 2>/dev/null || \
adb shell am force-stop com.android.launcher3 2>/dev/null || \
adb shell am force-stop com.google.android.launcher 2>/dev/null || \
echo -e "${YELLOW}⚠ Could not force-stop launcher${NC}"

echo ""
echo -e "${GREEN}=========================================="
echo -e "✨ Done! All variants reinstalled cleanly."
echo -e "==========================================${NC}"
echo ""
echo -e "${YELLOW}📱 If you still see old app names:${NC}"
echo "   1. Manually restart your emulator/device"
echo "   2. Or run: adb reboot"
echo ""

