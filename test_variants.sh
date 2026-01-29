#!/bin/bash

# Script to test variant app labels

echo "Testing variant labels..."
echo "=========================="
echo ""

cd "$(dirname "$0")"

# Build all debug variants
echo "Building all debug variants..."
./gradlew assembleDebug --quiet

# Check the manifests
for variant in qaDebug stagingDebug prodDebug qaRelease stagingRelease prodRelease; do
    manifest_file=$(find app/build/intermediates/merged_manifests -name "AndroidManifest.xml" -path "*/${variant}/*" | head -1)
    if [ -f "$manifest_file" ]; then
        label=$(grep "android:label" "$manifest_file" | head -1 | sed 's/.*android:label="\([^"]*\)".*/\1/')
        echo "${variant}: ${label}"
    else
        echo "${variant}: Manifest not found (not built)"
    fi
done
