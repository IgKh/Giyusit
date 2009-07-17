!include "Sections.nsh"

!define JRE_VERSION "1.6"

OutFile "out\GiyusitInstall.exe"

Name "Giyusit"
InstallDir $PROGRAMFILES\Giyusit

Page directory
Page components
Page instfiles

UninstPage uninstConfirm
UninstPage instfiles

Section "!Application Files"
	SectionIn RO
	
	; Check for Java
	DetailPrint "Checking for Java installation..."
	
	Call DetectJRE
	Pop $0
	Pop $1
	StrCmp $0 "OK" Proceed
	
	; Java not found!
	MessageBox MB_OK|MB_ICONEXCLAMATION  "A recent Java installation was not found on this computer!$\nPlease install at least Java version 1.5" 
	Abort
Proceed:
	DetailPrint "Found Java version $1"
	
	; Write files
	SetOutPath $INSTDIR
	File out\Giyusit.exe
	
	; Create uninstaller
	WriteUninstaller $INSTDIR\uninstall.exe
	
	WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\Giyusit" "DisplayName" "Giyusit"
	WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\Giyusit" "UninstallString" "$INSTDIR\uninstall.exe"
SectionEnd

Section "Start Menu Shortcut"
	CreateShortCut "$SMPROGRAMS\Giyusit.lnk" "$INSTDIR\Giyusit.exe"
SectionEnd

Section "Desktop Shortcut"
	CreateShortCut "$DESKTOP\Giyusit.lnk" "$INSTDIR\Giyusit.exe"
SectionEnd

Section "Uninstall"
	Delete $INSTDIR\uninstall.exe
	Delete $INSTDIR\Giyusit.exe
	RMDir $INSTDIR
	
	Delete $SMPROGRAMS\Giyusit.lnk
	Delete $DESKTOP\Giyusit.lnk
	
	DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Giyusit"
SectionEnd

;
; Function used to detect if the JRE or JDK are installed
;
Function DetectJRE
	ReadRegStr $R1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" CurrentVersion
	StrCmp $R1 "" DetectTry2
	Goto GetJRE
	
DetectTry2:
	ReadRegStr $R1 HKLM "SOFTWARE\JavaSoft\Java Development Kit" CurrentVersion
	StrCmp $R1 "" NoFound
	
GetJRE:
	StrCpy $R2 $R1 1
	StrCpy $R3 ${JRE_VERSION} 1
	IntCmp $R2 $R3 0 FoundOld FoundNew
	
	StrCpy $R2 $R1 1 2
	StrCpy $R3 ${JRE_VERSION} 1 2
	IntCmp $R2 $R3 FoundNew FoundOld FoundNew

NoFound:
	Push "None"
	Push "NOK"
	Return

FoundOld:
	Push $R2
	Push "NOK"
	Return

FoundNew:
	Push $R2
	Push "OK"
	Return
FunctionEnd