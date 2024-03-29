@echo off
set DEBUGGING=0
if "%QUIET%" == "" set QUIET=0

if "%1" == "DEBUG" goto enable_debug_output

goto debug_setup_done

:enable_debug_output
set DEBUGGING=1
set QUIET=0
SHIFT
@echo on

:debug_setup_done

REM if "%VB6EXE%" == "" goto need_nb6_env_setup
REM goto nb6_env_setup_done

REM :need_nb6_env_setup
REM call nb6env.bat
REM if NOT "%ERRORLEVEL%" == "0" EXIT /B %ERRORLEVEL%
@if "%DEBUGGING%" == "1" @echo on

:nb6_env_setup_done

echo Updating version information ...
buildver.py
REM IF NOT %ERRORLEVEL% == 0 EXIT /B %ERRORLEVEL%
cd Installer
REM IF NOT %ERRORLEVEL% == 0 EXIT /B %ER`RORLEVEL%
buildver.py
cd ..

call "C:\Program Files\NetBeans 6.8\java3\ant\bin\ant.bat" "-Dlibs.swing-app-framework.classpath=/Program Files/NetBeans 6.8/java3/modules/ext/appframework-1.0.3.jar:/Program Files/NetBeans 6.8/java3/modules/ext/swing-worker-1.1.jar"

echo DGS Previewer Windows Installer ...
cd installer
call build.bat
cd ..
REM echo Updating version resource in installer ...
REM versplice installer\DGSUtils_Setup.exe .\versioninfo.xml
REM echo Done
REM IF %ERRORLEVEL% NEQ 0 GOTO versplice_error
GOTO versplice_noerr

:versplice_error
echo Verspice returned non-zero ...
EXIT /B 255

:versplice_noerr


if "%1" == "NOSIGN" goto no_signing_components

echo Signing components......
signtool sign /t http://timestamp.verisign.com/scripts/timstamp.dll /d "DGS Utilities Setup" /f c:\keys\rts.pfx /v /p %1 Installer/DGSUtils_Setup.exe

IF NOT %ERRORLEVEL% == 0 GOTO signing_error


:signing_component_complete
REM cd ..
goto no_signing_components


:signing_error
echo An error occurred signing binaries.
cd ..
EXIT /B 255

:no_signing_components

goto pkg_mac_files

:error_no_signpass
@echo ERROR: you need to specify a signing password on the command line or NOSIGN for an unsigned build
@echo usage: %0 password
EXIT /B 255

:pkg_mac_files
call pkg_mac_files.bat
:end
EXIT /B 0
