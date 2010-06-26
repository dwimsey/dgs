@echo on

rmdir /s/q mac
mkdir mac

mkdir mac\dgs4cl
cd mac\dgs4cl

xcopy ..\..\dgs4cl\dist\dgs4cl.jar .

echo Copying dgs4cl dependancies ...
mkdir lib
cd lib

xcopy ..\..\..\dgs4j\dist\dgs4j.jar .

xcopy ..\..\..\libs\batik\batik-1.8pre\lib\batik-all.jar .
xcopy ..\..\..\libs\batik\lib\*.jar .
REM Delete the javascript jar file to ensure it doesn't run in the previewer at this time
REM del js.jar

xcopy ..\..\..\libs\gif4free\dist\*.jar .

cd ..\..\..



mkdir mac\DGSPreviewer
mkdir mac\DGSPreviewer\DGSPreviewer.app

xcopy /E /F DGSPreviewer\Bundle.app\*.* mac\DGSPreviewer\DGSPreviewer.app\

cd mac\DGSPreviewer\DGSPreviewer.app\Contents\Resources\Java

xcopy ..\..\..\..\..\..\DGSPreviewer\dist\DGSPreviewer.jar .

echo Copying DGS dependancies ...
mkdir lib
cd lib

xcopy ..\..\..\..\..\..\..\dgs4j\dist\dgs4j.jar .

xcopy ..\..\..\..\..\..\..\libs\batik\batik-1.8pre\lib\batik-all.jar .
xcopy ..\..\..\..\..\..\..\libs\batik\lib\*.jar .
REM Delete the javascript jar file to ensure it doesn't run in the previewer at this time
REM del js.jar

xcopy ..\..\..\..\..\..\..\libs\gif4free\dist\*.jar .

xcopy "C:\Program Files\NetBeans 6.8\java3\modules\ext\appframework-1.0.3.jar" .
xcopy "C:\Program Files\NetBeans 6.8\java3\modules\ext\swing-worker-1.1.jar" .

cd ..\..\..\..\..
mkdir .background
cd .background
java -jar ..\..\..\mac\dgs4cl\dgs4cl.jar -d ..\..\branding\DGSPreviewerDMGBackground.xml ..\..\branding\DGSPreviewerDMGBackground.svg background.png
cd ..\..\..

del DGSPreviewerMacOSX.zip
zip -r DGSPreviewerMacOSX.zip mac
