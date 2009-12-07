#!/bin/sh

mkdir dist
rm -rf dist/Bundle.app
rm -rf dist/DGS\ Previewer.app
cp -Rp Bundle.app dist
cp dist/DGSPreviewer.jar dist/Bundle.app/Contents/Resources/Java
cp dist/lib/*.jar dist/Bundle.app/Contents/Resources/Java

/Developer/Tools/SetFile -a B dist/Bundle.app
mv dist/Bundle.app "dist/DGS Previewer.app"
