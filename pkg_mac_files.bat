@echo on

rmdir /s/q mac 2> NUL
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

cd mac
cd DGSPreviewer
cd DGSPreviewer.app
cd Contents
cd Resources
mkdir Java 2> NUL
cd Java

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

cd ..\..\..\..\..\..\..

cd mac\DGSPreviewer
mkdir Examples
cd Examples
xcopy ..\..\..\DGSPreviewer\branding\DGSPreviewerDMGBackground.svg .
xcopy ..\..\..\versioninfo.dgs .
move versioninfo.xml VersionInfo.dgs
cd ..\..\..

del DGSPreviewerMacOSX.zip 2> NUL
zip -r DGSPreviewerMacOSX.zip mac
