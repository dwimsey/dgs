#!/bin/sh
#
# This build phase creates a disk image containing the plugin and an installer.
#
volume_name=DGSPreviewer
disk_image_name=DGSPreviewer
dmg_build_directory="build/mac"
backgroundPictureName="background.png"

rm -rf build/mac
mkdir -p build/mac

cd DGSPreviewer
./mkbundle.sh
mv dist/DGS\ Previewer.app ../${dmg_build_directory}
cd ..
sleep 5
#cp -Rp DGSPreviewer/Bundle.app/* ${dmg_build_directory}/DGSPreviewer.app

start_dir=`pwd`
echo "Starting directory: ${dmg_start_directory}"
echo "Creating DMG background image from template ..."
mkdir .background 2>/dev/null
java -jar dgs4cl/dist/dgs4cl.jar -d VersionInfo.dgs branding/dmgbackground.svg ${dmg_build_directory}/.background/background.png
cd "$start_dir"

echo "Creating DMG ..."
hdiutil create -fs HFS+ -srcfolder "${dmg_build_directory}" -volname "$volume_name" -format UDRW -imagekey zlig-level=9 -o "$disk_image_name.tmp"
echo hdiutil create -fs HFS+ -srcfolder "${dmg_build_directory}" -volname "$volume_name" -format UDRW -imagekey zlig-level=9 -o "$disk_image_name.tmp"
#sleep 5

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
           set position of item "Examples" of container window to {528, 50}
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
