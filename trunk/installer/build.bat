rem @echo off

del DGSUtils_Setup.exe
cd ..

buildver.rb

cd Installer


buildver.rb

c:\progra~1\NSIS\makensis /V2 DGSUtils.nsi

SignCode -spc c:\keys\rts.spc -v c:\keys\rts.pvk -n "DGS Utilities Pack Setup" -i http://www.rtsz.com/products/dgs/ -$ commercial -t http://timestamp.verisign.com/scripts/timstamp.dll DGSUtils_Setup.exe
