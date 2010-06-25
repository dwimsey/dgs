#!/bin/sh
#
# This build phase creates a disk image containing the plugin and an installer.
#
volume_name=DGSPreviewer
disk_image_name=DGSPreviewer.dmg
source_directory="mac/DGSPreviewer"

rm "$disk_image_name"
# 2>/dev/null
hdiutil create -fs HFS+ -srcfolder "$source_directory" -volname "$volume_name" -format UDZO -imagekey zlig-level=9 -o "$disk_image_name"

