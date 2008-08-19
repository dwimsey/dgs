rem @echo off

del DGSUtils_Setup.exe
cd ..

buildver.rb

cd Installer


buildver.rb

c:\progra~1\NSIS\makensis /V2 DGSUtils.nsi
