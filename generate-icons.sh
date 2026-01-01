#!/bin/bash

# Script to generate app icons for all platforms from a source image
# Usage: ./generate-icons.sh <source-image.png>
# 
# Requirements:
# - ImageMagick (install with: brew install imagemagick on macOS, or apt-get install imagemagick on Linux)
# - Source image should be at least 1024x1024 pixels

if [ $# -eq 0 ]; then
    echo "Usage: ./generate-icons.sh <source-image.png>"
    echo "Example: ./generate-icons.sh app-icon-source.png"
    exit 1
fi

SOURCE_IMAGE="$1"

if [ ! -f "$SOURCE_IMAGE" ]; then
    echo "Error: Source image file '$SOURCE_IMAGE' not found!"
    exit 1
fi

echo "Generating icons from $SOURCE_IMAGE..."

# Check if ImageMagick is installed
if ! command -v convert &> /dev/null; then
    echo "Error: ImageMagick is not installed."
    echo "Install it with: brew install imagemagick (macOS) or apt-get install imagemagick (Linux)"
    exit 1
fi

# Create temporary directory
TEMP_DIR=$(mktemp -d)
echo "Working in temporary directory: $TEMP_DIR"

# Generate iOS icon (1024x1024)
echo "Generating iOS icon..."
mkdir -p "$TEMP_DIR/ios"
convert "$SOURCE_IMAGE" -resize 1024x1024 "$TEMP_DIR/ios/app-icon-1024.png"

# Generate Android icons
echo "Generating Android icons..."
ANDROID_SIZES=(
    "hdpi:72x72"
    "mdpi:48x48"
    "xhdpi:96x96"
    "xxhdpi:144x144"
    "xxxhdpi:192x192"
)

for size_info in "${ANDROID_SIZES[@]}"; do
    IFS=':' read -r density size <<< "$size_info"
    echo "  - Generating $density ($size)..."
    mkdir -p "$TEMP_DIR/android/mipmap-$density"
    convert "$SOURCE_IMAGE" -resize "${size}" "$TEMP_DIR/android/mipmap-$density/ic_launcher.png"
    convert "$SOURCE_IMAGE" -resize "${size}" "$TEMP_DIR/android/mipmap-$density/ic_launcher_round.png"
done

# Generate Android adaptive icon foreground (needs to be centered on 108x108 canvas)
echo "Generating Android adaptive icon foreground..."
mkdir -p "$TEMP_DIR/android/drawable-v24"
convert "$SOURCE_IMAGE" -resize 108x108 -gravity center -background transparent -extent 108x108 "$TEMP_DIR/android/drawable-v24/ic_launcher_foreground.png"

# Generate Android adaptive icon background (solid blue from the image)
echo "Generating Android adaptive icon background..."
# Extract dominant color or use blue (#0066FF as placeholder)
mkdir -p "$TEMP_DIR/android/drawable"
convert -size 108x108 xc:"#0066FF" "$TEMP_DIR/android/drawable/ic_launcher_background.png"

# Generate desktop icons (various sizes)
echo "Generating desktop icons..."
DESKTOP_SIZES=(16 32 48 64 128 256 512)
for size in "${DESKTOP_SIZES[@]}"; do
    echo "  - Generating ${size}x${size}..."
    mkdir -p "$TEMP_DIR/desktop/${size}x${size}"
    convert "$SOURCE_IMAGE" -resize "${size}x${size}" "$TEMP_DIR/desktop/${size}x${size}/icon.png"
done

# Generate web favicon (32x32, 16x16, and apple-touch-icon 180x180)
echo "Generating web icons..."
mkdir -p "$TEMP_DIR/web"
convert "$SOURCE_IMAGE" -resize 32x32 "$TEMP_DIR/web/favicon-32x32.png"
convert "$SOURCE_IMAGE" -resize 16x16 "$TEMP_DIR/web/favicon-16x16.png"
convert "$SOURCE_IMAGE" -resize 180x180 "$TEMP_DIR/web/apple-touch-icon.png"
convert "$SOURCE_IMAGE" -resize 192x192 "$TEMP_DIR/web/android-chrome-192x192.png"
convert "$SOURCE_IMAGE" -resize 512x512 "$TEMP_DIR/web/android-chrome-512x512.png"
# Generate ICO file for favicon (16, 32, 48 sizes)
convert "$SOURCE_IMAGE" -define icon:auto-resize=16,32,48 "$TEMP_DIR/web/favicon.ico"

echo ""
echo "Icons generated successfully!"
echo ""
echo "Next steps:"
echo "1. Review the generated icons in: $TEMP_DIR"
echo "2. Copy iOS icon:"
echo "   cp $TEMP_DIR/ios/app-icon-1024.png iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/"
echo ""
echo "3. Copy Android icons:"
echo "   cp -r $TEMP_DIR/android/mipmap-* composeApp/src/androidMain/res/"
echo "   cp $TEMP_DIR/android/drawable-v24/* composeApp/src/androidMain/res/drawable-v24/"
echo "   cp $TEMP_DIR/android/drawable/* composeApp/src/androidMain/res/drawable/"
echo ""
echo "4. Copy desktop icons to: composeApp/src/jvmMain/resources/"
echo "   (You'll need to configure the build.gradle.kts with the icon path)"
echo ""
echo "5. Copy web icons to: composeApp/src/wasmJsMain/resources/"
echo ""
echo "Or run this script with --install flag to automatically copy files."
echo ""

# Check if --install flag is provided
if [ "$2" == "--install" ]; then
    echo "Installing icons..."
    
    # Copy iOS
    cp "$TEMP_DIR/ios/app-icon-1024.png" iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/
    echo "✓ iOS icon installed"
    
    # Copy Android
    cp -r "$TEMP_DIR/android/mipmap-"* composeApp/src/androidMain/res/
    cp "$TEMP_DIR/android/drawable-v24/"* composeApp/src/androidMain/res/drawable-v24/
    cp "$TEMP_DIR/android/drawable/"* composeApp/src/androidMain/res/drawable/
    echo "✓ Android icons installed"
    
    # Copy Web
    cp "$TEMP_DIR/web/"* composeApp/src/wasmJsMain/resources/
    echo "✓ Web icons installed"
    
    # Note about desktop - needs manual configuration
    echo "⚠ Desktop icons need manual configuration in build.gradle.kts"
    echo "  Desktop icons are in: $TEMP_DIR/desktop/"
    
    echo ""
    echo "All icons installed! (except desktop - see note above)"
fi

echo "Temporary files are in: $TEMP_DIR"
echo "You can delete this directory after copying the files."

