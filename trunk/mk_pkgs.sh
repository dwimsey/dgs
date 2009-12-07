#!/bin/sh

dgs_pkgname="dist/DGSPreviewer-0.6.0.0.dmg"
dgs_volname="DGS Previewer Installer"

mkdir dist 2>/dev/null

cd DGSPreviewer
./mkbundle.sh
cd ..

rm -rf dist/dmg_src 2>/dev/null
mkdir dist/dmg_src 2>/dev/null
mkdir dist/dmg_src/.branding 2>/dev/null
cp branding/dmgbackground.png dist/dmg_src/.branding/
#/Developer/Tools/SetFile -a V dist/dmg_src/.branding/dmgbackground.png
#/Developer/Tools/SetFile -a C dist/dmg_src/.branding/dmgbackground.png

cp -Rp "DGSPreviewer/dist/DGS Previewer.app" dist/dmg_src/
cd dist/dmg_src
ln -s /Applications ./Applications
cd ../..
/Developer/Tools/SetFile -a B "dist/dmg_src/DGS Previewer.app"

find dist/dmg_src -type d -name CVS -exec rm -rf {} \; 2>/dev/null

cp branding/DSStore dist/dmg_src/.DS_Store

rm "$dgs_pkgname" 2>/dev/null
./mk_dmg.sh "$dgs_volname" "$dgs_pkgname"
