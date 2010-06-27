#!/bin/sh
#
# This build phase creates a disk image containing the plugin and an installer.
#
volume_name=DGSPreviewer
disk_image_name=DGSPreviewer
source_directory="mac/DGSPreviewer"
backgroundPictureName="background.png"

find "$source_directory" -type f -exec chmod 644 {} \;
find "$source_directory" -type d -exec chmod 755 {} \;
chmod 755 "${source_directory}/DGSPreviewer.app/Contents/MacOS/JavaApplicationStub"

rm "$disk_image_name.dmg" 2>/dev/null

start_dir=`pwd`
echo "Starting directory: ${start_dir}"
cd "$source_directory"
echo "Creating DMG background image from template ..."
java -jar dgs4cl/dgs4cl.jar -d Examples/VersionInfo.dgs Examples/DGSPreviewerDMGBackground.svg ./background.png
cd "$start_dir"

echo "Creating DMG ..."
hdiutil create -fs HFS+ -srcfolder "$source_directory" -volname "$volume_name" -format UDRW -imagekey zlig-level=9 -o "$disk_image_name.tmp"
sleep 5

echo "Mounting DMG ..."
device=$(hdiutil attach -readwrite -noverify -noautoopen "$disk_image_name.tmp.dmg" | egrep '^/dev/' | sed 1q | awk '{print $1}')
echo Device: "$device"
sleep 5

echo "Setting folder view options and icon positions ..."
echo '
   tell application "Finder"
     tell disk "'${volume_name}'"
           open
           set current view of container window to icon view
           set toolbar visible of container window to false
           set statusbar visible of container window to false
           set the bounds of container window to {400, 100, 1000, 460}
           set theViewOptions to the icon view options of container window
           set arrangement of theViewOptions to not arranged
           set icon size of theViewOptions to 80
           set background picture of theViewOptions to file ".background:'${backgroundPictureName}'"
           make new alias file at container window to POSIX file "/Applications" with properties {name:"Applications"}
           set position of item "'DGSPreviewer.app'" of container window to {210, 250}
           set position of item "Applications" of container window to {400, 250}
           close
           open
           update without registering applications
           delay 5
           eject
     end tell
   end tell
' | osascript
echo "Detaching DMG ..."
hdiutil detach ${device}
sleep 5
echo "Converting DMG to compressed readonly archive."
hdiutil convert "$disk_image_name.tmp.dmg" -format UDZO -imagekey zlib-level=9 -o "$disk_image_name.dmg"
sleep 5
echo "Done"

rm "$disk_image_name.tmp.dmg"
