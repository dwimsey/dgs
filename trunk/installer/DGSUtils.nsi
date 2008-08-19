;Include Modern UI
!include "MUI.nsh"

;General

;Name and file
Name "DGS Utilities Pack"
OutFile "DGSUtils_Setup.exe"

;Interface Settings
!define MUI_ABORTWARNING

!define CompanyInstallDir "$PROGRAMFILES\Research Triangle Software"

;Default installation folder
InstallDir "${CompanyInstallDir}\DGS\utils"

;Get installation folder from registry if available
InstallDirRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\DGSUtils" "InstallLocation"

Var /GLOBAL STARTMENU_FOLDER
;Pages
!insertmacro MUI_PAGE_LICENSE "License.rtf"
Page custom PageReinstall PageLeaveReinstall
;	// No need for the components page at the moment, maybe in the future
; !insertmacro MUI_PAGE_COMPONENTS
!insertmacro MUI_PAGE_DIRECTORY

!define MUI_STARTMENUPAGE_REGISTRY_ROOT HKLM
!define MUI_STARTMENUPAGE_REGISTRY_KEY "Software\Microsoft\Windows\CurrentVersion\Uninstall\DGSUtils"
!define MUI_STARTMENUPAGE_REGISTRY_VALUENAME "StartMenuPath"
!insertmacro MUI_PAGE_STARTMENU "DGSUTILS_SM" $STARTMENU_FOLDER
!insertmacro MUI_PAGE_INSTFILES
!define MUI_FINISHPAGE_BUTTON "Finish"

!define MUI_FINISHPAGE_SHOWREADME "$INSTDIR\Readme.txt"
!insertmacro MUI_PAGE_FINISH
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES
  
;Languages
!insertmacro MUI_LANGUAGE "English"

;ReserveFile "${NSISDIR}\Plugins\InstallOptions.dll"
ReserveFile "PageReinstall.ini"
!insertmacro MUI_RESERVEFILE_INSTALLOPTIONS

; setup exe version information
!include "installer_version.nsi"
!include "utils_product_version.nsi"

VIAddVersionKey /LANG=${LANG_ENGLISH} "ProductName" "DGS Utilities Pack Installer"
VIAddVersionKey /LANG=${LANG_ENGLISH} "Comments" "For more information, visit http://www.rtsz.com/products/dgs/"
VIAddVersionKey /LANG=${LANG_ENGLISH} "CompanyName" "Research Triangle Software, Inc"
VIAddVersionKey /LANG=${LANG_ENGLISH} "LegalTrademarks" "RTS, DGS, and Digital Graphics Server are trademarks of Research Triangle Software"
VIAddVersionKey /LANG=${LANG_ENGLISH} "LegalCopyright" "© Research Triangle Software"
VIAddVersionKey /LANG=${LANG_ENGLISH} "FileDescription" "DGS Utilities Pack Installer"
VIAddVersionKey /LANG=${LANG_ENGLISH} "InternalName" "DGSUtils_Setup.exe"
VIAddVersionKey /LANG=${LANG_ENGLISH} "OriginalFilename" "DGSUtils_Setup.exe"

Caption "DGS Utilities Package $ProductMajorVersion.$ProductMinorVersion $ProductSpecialBuild (Build: $ProductBuildNumber) Setup"
BrandingText "Research Triangle Software, Inc."

; FileFunc.nsh is used by the java detection code and by the version check install/uninstall/reinstall code
!include "FileFunc.nsh"

; Various bits used by the java checking code
!insertmacro GetFileVersion
;!insertmacro GetParameters
!include "WordFunc.nsh"
!insertmacro VersionCompare

; Definitions for Java 6.0
!define JRE_VERSION "6.0"
!define JRE_URL "http://javadl.sun.com/webapps/download/AutoDL?BundleId=20288&/jre-6u6-windows-i586-p.exe"

; Definitions for Java 5.0, we've not tested with the 5.x runtime so we'll stick with 6 as the requirement for now
;!define JRE_VERSION "5.0"
;!define JRE_URL "http://javadl.sun.com/webapps/download/AutoDL?BundleId=18675&/jre-1_5_0_15-windows-i586-p.exe"

!define JAVAEXE "javaw.exe"
RequestExecutionLevel user

Function .onInit
	System::Call 'kernel32::CreateMutexA(i 0, i 0, t "RTSDGSUtilsClientInstallerMutex") i .r1 ?e'
	Pop $R0

	StrCmp $R0 0 +3
		MessageBox MB_OK|MB_ICONEXCLAMATION "Another instance of the DGS Utilities Package installer is running.  You must finish or cancel the other instance."
		Abort

	!insertmacro MUI_INSTALLOPTIONS_EXTRACT "PageReinstall.ini"
	Call InitializeProductVersionTable
	
	Var /GLOBAL "InstalledVersionCheck"
	StrCpy $InstalledVersionCheck "0"

	Call CheckInstalledVersion
	IfSilent 0 +2
		Call SilentHandleInstalledVersion
		
FunctionEnd

; Sections
Section "Required Files" SecRequired
	Call GetJRE
	SectionIn RO
	SetOutPath "$INSTDIR"
	File Readme.txt
	File License.rtf

	; DGS Previewer
	File ..\dgspreviewer\dist\*.jar
	SetOutPath "$INSTDIR\lib"
	File /r ..\dgspreviewer\dist\lib\*.*

	SetOutPath "$INSTDIR\examples"
	File "..\examples\rts_card.svg"
	File "..\examples\userVars.xml"
	File "..\examples\substituteVariables.svg"
	File "..\examples\43x54.png"

	SetOutPath "$INSTDIR"

!insertmacro MUI_STARTMENU_WRITE_BEGIN "DGSUTILS_SM"
	CreateDirectory "$SMPROGRAMS\$STARTMENU_FOLDER"
	CreateShortCut "$SMPROGRAMS\$STARTMENU_FOLDER\DGS Previewer.lnk" "javaw.exe" "-jar $\"$INSTDIR\DGSPreviewer.jar$\""
;	CreateDirectory "$SMPROGRAMS\$STARTMENU_FOLDER\Help"
	CreateShortCut "$SMPROGRAMS\$STARTMENU_FOLDER\License.lnk" "$INSTDIR\License.rtf" "" "$INSTDIR\License.rtf" 0
	CreateShortCut "$SMPROGRAMS\$STARTMENU_FOLDER\ReadMe.lnk" "$INSTDIR\Readme.txt" "" "$INSTDIR\Readme.txt" 0
	CreateShortCut "$SMPROGRAMS\$STARTMENU_FOLDER\Uninstall.lnk" "$INSTDIR\Uninstall.exe" "" "$INSTDIR\Uninstall.exe" 0
!insertmacro MUI_STARTMENU_WRITE_END

;	Write the uninstall keys for Windows
	WriteRegExpandStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\DGSUtils" "InstallLocation" "$INSTDIR"
	WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\DGSUtils" "NoModify" 1
	WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\DGSUtils" "NoRepair" 1
	WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\DGSUtils" "VersionMajor" "$ProductMajorVersion"
	WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\DGSUtils" "VersionMinor" "$ProductMinorVersion"
	WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\DGSUtils" "PatchLevel" "$ProductPatchLevel"
	WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\DGSUtils" "Build" "$ProductBuildNumber"
;	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\DGSUtils" "DisplayIcon" "$INSTDIR\CryptoLock.exe,1"
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\DGSUtils" "DisplayVersion" "$ProductProductVersion"
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\DGSUtils" "DisplayName" "DGS Utilities Pack"
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\DGSUtils" "UninstallString" '"$INSTDIR\Uninstall.exe"'
	; support information in add/remove programs
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\DGSUtils" "Contact" "RTS Technical Support"
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\DGSUtils" "Publisher" "Research Triangle Software"
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\DGSUtils" "HelpTelephone" "(919) 657-0505"
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\DGSUtils" "URLInfoAbout" "http://www.rtsz.com/"
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\DGSUtils" "URLUpdateInfo" "http://webupdate.rtsz.com/dgs/"
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\DGSUtils" "HelpLink" "http://support.rtsz.com/products/dgs/client/"
;	Create the actual uninstaller
	WriteUninstaller "$INSTDIR\Uninstall.exe"
SectionEnd


; Descriptions

	; Language strings
	LangString DESC_SecRequired ${LANG_ENGLISH} "Base files required for installation."

	; Assign language strings to sections
	!insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
		!insertmacro MUI_DESCRIPTION_TEXT ${SecRequired} $(DESC_SecRequired)
	!insertmacro MUI_FUNCTION_DESCRIPTION_END


;Uninstaller Section
Section "Uninstall"

	Delete "$INSTDIR\examples\rts_card.svg"
	Delete "$INSTDIR\examples\userVars.xml"
	Delete "$INSTDIR\examples\substituteVariables.svg"
	Delete "$INSTDIR\examples\43x54.png"
	RMDir "$INSTDIR\examples"

	Delete "$INSTDIR\lib\*.*"
	RMDir "$INSTDIR\lib"
	Delete "$INSTDIR\DGSPreviewer.jar"

	Delete "$INSTDIR\License.rtf"
	Delete "$INSTDIR\Readme.txt"
	Delete "$INSTDIR\Uninstall.exe"

Var /GLOBAL LMC_SM_PATH
!insertmacro MUI_STARTMENU_GETFOLDER "DGSUTILS_SM" $LMC_SM_PATH
; Remove startmenu shortcuts, if any
	Delete "$SMPROGRAMS\$LMC_SM_PATH\Help\*.*"
	RMDir "$SMPROGRAMS\$LMC_SM_PATH\Help"
	Delete "$SMPROGRAMS\$LMC_SM_PATH\*.*"
	RMDir "$SMPROGRAMS\$LMC_SM_PATH"

	; Remove registry keys
	DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\DGSUtils"

	; Remove directories used
	RMDir "$INSTDIR"
	RMDir "${CompanyInstallDir}"

SectionEnd

Function CheckInstalledVersion

  ReadRegStr $R0 HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\DGSUtils" "InstallLocation"

  StrCmp $R0 "" 0 +3
	StrCpy $InstalledVersionCheck "0"
    Goto resinst_check_done

  ;Detect version
    ReadRegDWORD $R0 HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\DGSUtils" "VersionMajor"
    IntCmp $R0 $ProductMajorVersion minor_check new_version older_version
  minor_check:
    ReadRegDWORD $R0 HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\DGSUtils" "VersionMinor"
    IntCmp $R0 $ProductMinorVersion revision_check new_version older_version
  revision_check:
    ReadRegDWORD $R0 HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\DGSUtils" "PatchLevel"
    IntCmp $R0 $ProductPatchLevel build_check new_version older_version
  build_check:
    ReadRegDWORD $R0 HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\DGSUtils" "Build"
    IntCmp $R0 $ProductBuildNumber same_version new_version older_version

  new_version:
	StrCpy $InstalledVersionCheck "1"
   Goto resinst_check_done

  older_version:
	StrCpy $InstalledVersionCheck "3"
   Goto resinst_check_done

  same_version:
	StrCpy $InstalledVersionCheck "2"

  resinst_check_done:

FunctionEnd

Function SilentHandleInstalledVersion
   StrCmp $InstalledVersionCheck "3" 0 +2
		Quit

   StrCmp $InstalledVersionCheck "0" 0 +2
		Goto handle_installed_version_done
		
	
   ReadRegStr $R1 HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\DGSUtils" "UninstallString"

  ;Run uninstaller
	ClearErrors
	ExecWait '$R1 /S _?=$INSTDIR'
	Delete $R1
	RMDir $INSTDIR
	handle_installed_version_done:
FunctionEnd

Function PageReinstall

   StrCmp $InstalledVersionCheck "0" 0 +2
		Abort

   StrCmp $InstalledVersionCheck "1" 0 +2
		Goto prompt_new_version
	
   StrCmp $InstalledVersionCheck "2" 0 +2
		Goto prompt_same_version

   StrCmp $InstalledVersionCheck "3" 0 +2
		Goto prompt_older_version

   prompt_new_version:
	StrCpy $InstalledVersionCheck "1"
   !insertmacro MUI_INSTALLOPTIONS_WRITE "PageReinstall.ini" "Field 1" "Text" "An older version of the DGS Utilities Pack is installed on your system. It's recommended that you uninstall the current version before installing. Select the operation you want to perform and click Next to continue."
   !insertmacro MUI_INSTALLOPTIONS_WRITE "PageReinstall.ini" "Field 2" "Text" "Uninstall before installing"
   !insertmacro MUI_INSTALLOPTIONS_WRITE "PageReinstall.ini" "Field 3" "Text" "Do not uninstall"
   !insertmacro MUI_HEADER_TEXT "Already Installed" "Choose how you want to install the DGS Utilities Pack."
   StrCpy $R0 "1"
   Goto reinst_start

  prompt_older_version:
	StrCpy $InstalledVersionCheck "3"
   !insertmacro MUI_INSTALLOPTIONS_WRITE "PageReinstall.ini" "Field 1" "Text" "A newer version of the DGS Utilities Pack is already installed!  It is not recommended that you install an older version. If you really want to install this older version, you must uninstall the existing version first.  Select the operation you want to perform and click Next to continue."
   !insertmacro MUI_INSTALLOPTIONS_WRITE "PageReinstall.ini" "Field 2" "Text" "Do not install this old version."
   !insertmacro MUI_INSTALLOPTIONS_WRITE "PageReinstall.ini" "Field 3" "Text" "Uninstall before installing"
   !insertmacro MUI_HEADER_TEXT "Already Installed" "Choose how you want to install the DGS Utilities Pack."
   StrCpy $R0 "3"
   Goto reinst_start

  prompt_same_version:
	StrCpy $InstalledVersionCheck "2"
   !insertmacro MUI_INSTALLOPTIONS_WRITE "PageReinstall.ini" "Field 1" "Text" "DGS Utilities Pack $ProductMajorVersion.$ProductMinorVersion (Build: $ProductBuildNumber) is already installed. Select the operation you want to perform and click Next to continue."
   !insertmacro MUI_INSTALLOPTIONS_WRITE "PageReinstall.ini" "Field 2" "Text" "Add/Reinstall components"
   !insertmacro MUI_INSTALLOPTIONS_WRITE "PageReinstall.ini" "Field 3" "Text" "Uninstall the DGS Utilities Pack"
   !insertmacro MUI_HEADER_TEXT "Already Installed" "Choose the maintenance option to perform."
   StrCpy $R0 "2"

  reinst_start:
  !insertmacro MUI_INSTALLOPTIONS_DISPLAY "PageReinstall.ini"
FunctionEnd

Function PageLeaveReinstall

  !insertmacro MUI_INSTALLOPTIONS_READ $R1 "PageReinstall.ini" "Field 2" "State"

  StrCmp $R0 "1" 0 +2
    StrCmp $R1 "1" reinst_uninstall reinst_done

  StrCmp $R0 "2" 0 +2
    StrCmp $R1 "1" reinst_done reinst_uninstall

 StrCmp $R0 "3" 0 +2
    StrCmp $R1 "1" reinst_quit reinst_uninstall

  reinst_uninstall:
  ReadRegStr $R1 HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\DGSUtils" "UninstallString"

  ;Run uninstaller
  ; HideWindow

    ClearErrors
    ExecWait '$R1 /S /REINSTALL _?=$INSTDIR'

    IfErrors no_remove_uninstaller
    IfFileExists "$INSTDIR\CryptoLock.exe" no_remove_uninstaller

      Delete $R1
      RMDir $INSTDIR

    no_remove_uninstaller:

  StrCmp $R0 "2" 0 +2
    Quit

  BringToFront
  Goto reinst_done
  
  reinst_quit:
    MessageBox MB_YESNO "Are you sure you want to quit the DGS Utilities Pack setup?" IDYES reinst_quit_really IDNO reinst_quit_abort
  reinst_quit_really:
	Quit
  reinst_quit_abort:
	Abort
	
  reinst_done:

FunctionEnd

;  returns the full path of a valid java.exe
;  looks in:
;  1 - .\jre directory (JRE Installed with application)
;  2 - JAVA_HOME environment variable
;  3 - the registry
;  4 - hopes it is in current dir or PATH
Function GetJRE
    Push $R0
    Push $R1
    Push $2
 
  ; 1) Check local JRE
  CheckLocal:
    ClearErrors
    StrCpy $R0 "$EXEDIR\jre\bin\${JAVAEXE}"
    IfFileExists $R0 JreFound
 
  ; 2) Check for JAVA_HOME
  CheckJavaHome:
    ClearErrors
    ReadEnvStr $R0 "JAVA_HOME"
    StrCpy $R0 "$R0\bin\${JAVAEXE}"
    IfErrors CheckRegistry     
    IfFileExists $R0 0 CheckRegistry
    Call CheckJREVersion
    IfErrors CheckRegistry JreFound
 
  ; 3) Check for registry
  CheckRegistry:
    ClearErrors
    ReadRegStr $R1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
    ReadRegStr $R0 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$R1" "JavaHome"
    StrCpy $R0 "$R0\bin\${JAVAEXE}"
    IfErrors DownloadJRE
    IfFileExists $R0 0 DownloadJRE
    Call CheckJREVersion
    IfErrors DownloadJRE JreFound
 
  DownloadJRE:
;    Call ElevateToAdmin
    MessageBox MB_ICONINFORMATION "The DGS Utilities Pack requires the Java Runtime Environment ${JRE_VERSION}, it will now be downloaded and installed."
    StrCpy $2 "$TEMP\Java Runtime Environment.exe"
    nsisdl::download /TIMEOUT=30000 ${JRE_URL} $2
    Pop $R0 ;Get the return value
    StrCmp $R0 "success" +3
      MessageBox MB_ICONSTOP "Download failed: $R0"
      Abort
    ExecWait $2
    Delete $2
 
    ReadRegStr $R1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
    ReadRegStr $R0 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$R1" "JavaHome"
    StrCpy $R0 "$R0\bin\${JAVAEXE}"
    IfFileExists $R0 0 GoodLuck
    Call CheckJREVersion
    IfErrors GoodLuck JreFound
 
  ; 4) wishing you good luck
  GoodLuck:
    StrCpy $R0 "${JAVAEXE}"
    ; MessageBox MB_ICONSTOP "Cannot find appropriate Java Runtime Environment."
    ; Abort
 
  JreFound:
    Pop $2
    Pop $R1
    Exch $R0
FunctionEnd
 
; Pass the "javaw.exe" path by $R0
Function CheckJREVersion
    Push $R1
 
    ; Get the file version of javaw.exe
    ${GetFileVersion} $R0 $R1
    ${VersionCompare} ${JRE_VERSION} $R1 $R1
 
    ; Check whether $R1 != "1"
    ClearErrors
    StrCmp $R1 "1" 0 CheckDone
    SetErrors
 
  CheckDone:
    Pop $R1
FunctionEnd
 
; Attempt to give the UAC plug-in a user process and an admin process.
;Function ElevateToAdmin
;  UAC_Elevate:
;    UAC::RunElevated
;    StrCmp 1223 $0 UAC_ElevationAborted ; UAC dialog aborted by user?
;    StrCmp 0 $0 0 UAC_Err ; Error?
;    StrCmp 1 $1 0 UAC_Success ;Are we the real deal or just the wrapper?
;    Quit
; 
;  UAC_ElevationAborted:
;    # elevation was aborted, run as normal?
;    MessageBox MB_ICONSTOP "This installer requires admin access, aborting!"
;    Abort
; 
;  UAC_Err:
;    MessageBox MB_ICONSTOP "Unable to elevate, error $0"
;    Abort
; 
;  UAC_Success:
;    StrCmp 1 $3 +4 ;Admin?
;    StrCmp 3 $1 0 UAC_ElevationAborted ;Try again?
;    MessageBox MB_ICONSTOP "This installer requires admin access, try again"
;    goto UAC_Elevate 
;FunctionEnd