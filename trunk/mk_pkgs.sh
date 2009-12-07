#!/bin/sh

dgs_pkgname="dist/DGSPreviewer-0.6.0.0.dmg"
dgs_volname="DGS Previewer Installer"

cd DGSPreviewer
./mkbundle.sh
cd ..

rm -rf dmg_src 2>/dev/null
mkdir dmg_src 2>/dev/null

cp -Rp "DGSPreviewer/dist/DGS Previewer.app" dmg_src

find dmg_src -type d -name CVS -exec rm -rf {} \; 2>/dev/null

rm $dgs_pkgname 2>/dev/null
mkdir dist 2>/dev/null
./mk_dmg.sh "$dgs_volname" "$dgs_pkgname"

