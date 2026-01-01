# App Icon Setup Guide

This guide explains how to set up the app icon for all platforms (Android, iOS, Desktop, and Web).

## Prerequisites

1. **Source Image**: You need a source image file (PNG format recommended)
   - Minimum size: 1024x1024 pixels
   - Square aspect ratio (1:1)
   - The image should represent your app icon design

2. **ImageMagick**: Required for the icon generation script
   - macOS: `brew install imagemagick`
   - Linux: `sudo apt-get install imagemagick`
   - Windows: Download from https://imagemagick.org/

## Quick Setup

1. **Place your source image** in the project root (e.g., `app-icon-source.png`)

2. **Run the generation script**:
   ```bash
   ./generate-icons.sh app-icon-source.png --install
   ```

   This will:
   - Generate all required icon sizes for Android, iOS, Desktop, and Web
   - Automatically copy them to the correct locations
   - Set up everything except desktop icons (which need manual configuration)

3. **For Desktop Icons** (optional):
   - The script generates desktop icons in a temporary directory
   - Copy the icons to `composeApp/src/jvmMain/resources/icons/`
   - Ensure you have:
     - `icon.png` (for Linux, at least 512x512)
     - `icon.ico` (for Windows, multiple sizes embedded)
     - `icon.icns` (for macOS)
   - To convert PNG to ICO/ICNS, you can use:
     - Online converters
     - ImageMagick: `convert icon.png -define icon:auto-resize=16,32,48,64,128,256 icon.ico`
     - For ICNS on macOS: Use `iconutil` or online converters

## Manual Setup (Alternative)

If you prefer to set up icons manually:

### Android Icons

Place icons in the following directories with these sizes:

- `composeApp/src/androidMain/res/mipmap-mdpi/`: 48x48 px
- `composeApp/src/androidMain/res/mipmap-hdpi/`: 72x72 px
- `composeApp/src/androidMain/res/mipmap-xhdpi/`: 96x96 px
- `composeApp/src/androidMain/res/mipmap-xxhdpi/`: 144x144 px
- `composeApp/src/androidMain/res/mipmap-xxxhdpi/`: 192x192 px

Files needed:
- `ic_launcher.png` (square icon)
- `ic_launcher_round.png` (square icon, will be cropped to circle)

Adaptive icons:
- Foreground: `composeApp/src/androidMain/res/drawable-v24/ic_launcher_foreground.png` (108x108 px)
- Background: `composeApp/src/androidMain/res/drawable/ic_launcher_background.png` (108x108 px, solid color)

### iOS Icons

Place a single 1024x1024 PNG file:
- `iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/app-icon-1024.png`

### Desktop Icons

Place icon files in:
- `composeApp/src/jvmMain/resources/icons/`

Required files:
- `icon.png` - At least 512x512 (for Linux)
- `icon.ico` - Multiple sizes (for Windows)
- `icon.icns` - Multiple sizes (for macOS)

The build configuration will automatically use these files if they exist.

### Web Icons (Favicons)

Place the following files in:
- `composeApp/src/wasmJsMain/resources/`

Required files:
- `favicon.ico` - Multi-size ICO file (16x16, 32x32, 48x48)
- `favicon-16x16.png` - 16x16 PNG
- `favicon-32x32.png` - 32x32 PNG
- `apple-touch-icon.png` - 180x180 PNG (for iOS Safari)
- `android-chrome-192x192.png` - 192x192 PNG
- `android-chrome-512x512.png` - 512x512 PNG

## Current Status

✅ **Android**: Configuration updated, ready for icon files
✅ **iOS**: Configuration updated, ready for icon file
✅ **Desktop**: Configuration updated, will use icons when files are present
✅ **Web**: HTML updated with favicon links, ready for icon files

## Notes

- The Android adaptive icon background has been set to blue (#0066FF) to match your design
- All configurations are set up and will work once icon files are in place
- The build won't fail if desktop icons are missing (they're optional)

## Testing

After setting up icons:

1. **Android**: Build and run the Android app to see the new launcher icon
2. **iOS**: Build and run the iOS app to see the new app icon
3. **Desktop**: Build the desktop app to see the new window icon
4. **Web**: Build and serve the web app, then check the browser tab for the favicon

