#!/bin/sh
#
# This build phase creates a disk image containing the plugin and an installer.
#
#volume_name="$1"
#disk_image_name="$2"
source_directory="dmg_src"

rm "$2" 2>/dev/null
hdiutil create -fs HFS+ -srcfolder "$source_directory" -volname "$1" -format UDZO -imagekey zlig-level=9 -o "$2"

