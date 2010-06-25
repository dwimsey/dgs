@echo off

rmdir /s/q mac
mkdir mac
mkdir mac\DGSPreviewer
mkdir mac\DGSPreviewer\DGSPreviewer.app

xcopy /E /F DGSPreviewer\Bundle.app\*.* mac\DGSPreviewer\DGSPreviewer.app\

cd mac\DGSPreviewer\DGSPreviewer.app\Contents\Resources\Java

copy ..\..\..\..\..\..\DGSPreviewer\dist\DGSPreviewer.jar .

echo Copying DGS dependancies ...
mkdir lib
cd lib

copy ..\..\..\..\..\..\..\dgs4j\dist\dgs4j.jar .

copy ..\..\..\..\..\..\..\libs\batik\batik-1.8pre\lib\batik-all.jar .
copy ..\..\..\..\..\..\..\libs\batik\lib\*.jar .
REM Delete the javascript jar file to ensure it doesn't run in the previewer at this time
REM del js.jar

copy ..\..\..\..\..\..\..\libs\gif4free\dist\*.jar .


cd ..\..\..\..\..
mkdir .background
cd .background
copy ..\..\..\branding\dmgbackground.png" .
cd ..\..\..

del DGSPreviewerMacOSX.zip
zip -r DGSPreviewerMacOSX.zip mac

