#!/bin/bash

# GitHub Followy Icon Generator
# This script generates Android app icons from the SVG source

echo "GitHub Followy - Icon Generator"
echo "================================"

# Check if ImageMagick is installed
if ! command -v convert &> /dev/null; then
    echo "❌ ImageMagick is not installed."
    echo "Please install it using:"
    echo "  Ubuntu/Debian: sudo apt-get install imagemagick"
    echo "  macOS: brew install imagemagick"
    exit 1
fi

# Source SVG file
SVG_FILE="app_icon.svg"

if [ ! -f "$SVG_FILE" ]; then
    echo "❌ Error: $SVG_FILE not found!"
    exit 1
fi

echo "✓ Found $SVG_FILE"

# Android icon sizes
declare -A SIZES=(
    ["mdpi"]=48
    ["hdpi"]=72
    ["xhdpi"]=96
    ["xxhdpi"]=144
    ["xxxhdpi"]=192
)

# Output directory
OUTPUT_DIR="androidApp/src/main/res"

echo ""
echo "Generating Android icons..."

for density in "${!SIZES[@]}"; do
    size=${SIZES[$density]}
    output_path="${OUTPUT_DIR}/mipmap-${density}"
    
    mkdir -p "$output_path"
    
    echo "  → mipmap-${density}/ic_launcher.png (${size}x${size})"
    convert -background none -resize ${size}x${size} "$SVG_FILE" "${output_path}/ic_launcher.png"
    
    echo "  → mipmap-${density}/ic_launcher_round.png (${size}x${size})"
    convert -background none -resize ${size}x${size} "$SVG_FILE" "${output_path}/ic_launcher_round.png"
done

# Generate foreground (for adaptive icons)
echo ""
echo "Generating adaptive icon foreground..."
for density in "${!SIZES[@]}"; do
    size=${SIZES[$density]}
    output_path="${OUTPUT_DIR}/mipmap-${density}"
    
    # For adaptive icons, use 108dp (1.5x the base size)
    adaptive_size=$((size * 108 / 72))
    
    echo "  → mipmap-${density}/ic_launcher_foreground.png (${adaptive_size}x${adaptive_size})"
    convert -background none -resize ${adaptive_size}x${adaptive_size} "$SVG_FILE" "${output_path}/ic_launcher_foreground.png"
done

echo ""
echo "✓ All icons generated successfully!"
echo ""
echo "📱 Icon locations:"
echo "   ${OUTPUT_DIR}/mipmap-*/"
echo ""
echo "Next steps:"
echo "1. Review the generated icons"
echo "2. Update colors in androidApp/src/main/res/values/colors.xml if needed"
echo "3. Build and run the app to see the new icon"
