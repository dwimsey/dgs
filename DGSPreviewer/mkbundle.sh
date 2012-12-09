#!/bin/sh

mkdir dist 2>/dev/null
rm -rf dist/Bundle.app 2>/dev/null
rm -rf dist/DGS\ Previewer.app 2>/dev/null
cp -Rp Bundle.app dist
mkdir dist/Bundle.app/Contents/Resources/Java
cp dist/DGSPreviewer.jar dist/Bundle.app/Contents/Resources/Java
mkdir dist/Bundle.app/Contents/Resources/Java/lib
cp dist/lib/*.jar dist/Bundle.app/Contents/Resources/Java/lib/

SetFile -a B dist/Bundle.app
mv dist/Bundle.app "dist/DGS Previewer.app"
