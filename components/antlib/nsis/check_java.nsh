######################################################################
# OMERO NSIS Dependency Check
#
# Copyright 2009 Glencoe Software, Inc. All rights reserved.
# Use is subject to license terms supplied in LICENSE.txt
#
# This header file is imported by the omero.nsh script
# for support on installing libraries
#
######################################################################

!define JRE5_INSTALLER "jre-1_5_0_16-windows-i586-p.exe"
!ifndef JRE5_URL
  !define JRE5_URL "http://javadl.sun.com/webapps/download/AutoDL?BundleId=22933&/jre-1_5_0_16-windows-i586-p.exe"
!endif
!ifndef JRE5_VERSION
  !define JRE5_VERSION "5.0"
!endif
!ifndef JRE5_MD5
  !define JRE5_MD5 "cfda28a6c4890e82944d267fc7440d5f"
!endif

!define JRE6_INSTALLER "jre-6u10-windows-i586-p.exe"
!ifndef JRE6_URL
  !define JRE6_URL "http://javadl.sun.com/webapps/download/AutoDL?BundleId=24936&/jre-6u10-windows-i586-p.exe"
!endif
!ifndef JRE6_VERSION
  !define JRE6_VERSION "6.0"
!endif
!ifndef JRE6_MD5
  !define JRE6_MD5 "UNKNOWN"
!endif

!ifdef JAVA5
  !define JRE_URL "${JRE5_URL}"
  !define JRE_MD5 "${JRE5_MD5}"
  !define JRE_VERSION "${JRE5_VERSION}"
  !define JRE_INSTALLER "${JRE5_INSTALLER}"
!else
  !define JRE_URL "${JRE6_URL}"
  !define JRE_MD5 "${JRE6_MD5}"
  !define JRE_VERSION "${JRE6_VERSION}"
  !define JRE_INSTALLER "${JRE6_INSTALLER}"
!endif

!define JRE_KEY "SOFTWARE\JavaSoft\Java Runtime Environment"

!macro CheckJava

  ; This section is slightly different since the GetJre
  ; function is copied code. Adds itself to path
  ;

  !insertmacro StartAction "CheckJava"

  Call GetJre
  ${If} ${Errors}
    ${LogText} "FAILED TO INSTALL JAVA"
    MessageBox MB_OK "Failed to install Java. Please do so manually and re-run the installer"
  ${Else}
    Pop $R2 ; Installer
    Pop $R1 ; Version
    Pop $R0 ; Path
    WriteINIStr "$INSINI" Java Path  "$R0"
    WriteINIStr "$INSINI" Java Version "$R1"
    WriteINIStr "$INSINI" Java Installer "$R2"
    WriteINIStr "$INSINI" Java State "${UNNEEDED}"
    ${IfNot} $R2 == ""
      WriteRegStr ${PRODUCT_INST_ROOT_KEY} "${PRODUCT_INST_KEY}" "JavaInstalledPath" "$R0"
    ${EndIf}
  ${EndIf}

  # JavaReady: ; ---------------------------------------------
  !insertmacro FinishAction "CheckJava"

!macroend

; Java Launcher with automatic JRE installation
;-----------------------------------------------
; http://nsis.sourceforge.net/Java_Launcher_with_automatic_JRE_installation
; See also: http://nsis.sourceforge.net/New_installer_with_JRE_check_(includes_fixes_from_%27Simple_installer_with_JRE_check%27_and_missing_jre.ini)
;
; Usage: Call GetJRE
;        Pop $R0
;        StrCpy $0 '"$R0" -classpath "${CLASSPATH}" ${CLASS} ... parameters ...'
;        SetOutPath $EXEDIR
;        Exec $0
;

;  returns the full path of a valid java.exe
;  looks in:
;  1 - .\jre directory (JRE Installed with application)
;  2 - JAVA_HOME environment variable
;  3 - the registry
;  4 - hopes it is in current dir or PATH

!include "WordFunc.nsh"
!insertmacro VersionCompare
!define JAVAEXE "java.exe"

Function GetJRE
    Push $R0     ; Path
    Push $R1     ; Version
    Push $2      ; Installer

    Goto CheckGlobal

  ; 0) Check global JRE
  CheckGlobal:
    ClearErrors
    StrCpy $R0 "${JAVAEXE}"
    Call CheckJREVersion
    IfErrors CheckLocal 0
      StrCpy $2 "GLOBAL"
      Goto JreFound

  ; 1) Check local JRE
  CheckLocal:
    ClearErrors
    StrCpy $R0 "$EXEDIR\jre\bin\${JAVAEXE}"
    ${LogText} "CheckLocal $R0"
    IfFileExists $R0 JreFound CheckJavaHome

  ; 2) Check for JAVA_HOME
  CheckJavaHome:
    ClearErrors
    ReadEnvStr $R0 "JAVA_HOME"
    StrCpy $R0 "$R0\bin\${JAVAEXE}"
    ${LogText} "CheckJavaHome $R0"
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
    ${LogText} "Check Java in Registry $R0"
    IfErrors DownloadJRE
    IfFileExists $R0 0 DownloadJRE
    Call CheckJREVersion
    IfErrors DownloadJRE JreFound

  DownloadJRE:
    ##
    ## Call ElevateToAdmin
    ##
    ${ConfirmInstall} Java
    StrCpy $2 "$INSDIR\${JRE_INSTALLER}"
    IfFileExists $2 Installing Downloading
    Downloading:
      MessageBox MB_ICONINFORMATION "${PRODUCT_NAME} uses Java Runtime Environment ${JRE_VERSION}, it will now be downloaded and installed from $2."
      nsisdl::download /TIMEOUT=30000 ${JRE_URL} $2
      Pop $R0 ;Get the return value
      StrCmp $R0 "success" +3
        ${LogText} "Download failed: $R0"
    Installing:
      ExecWait $2

    ReadRegStr $R1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
    ReadRegStr $R0 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$R1" "JavaHome"
    StrCpy $R0 "$R0\bin\${JAVAEXE}"
    IfFileExists $R0 0 GoodLuck
    Call CheckJREVersion
    IfErrors GoodLuck JreFound

  ; 4) wishing you good luck
  GoodLuck:
    StrCpy $R0 "${JAVAEXE}"
    ${LogText} "Cannot find appropriate Java Runtime Environment."

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
    ${LogText} "File: $R0 Version: $R1"
    ${VersionCompare} ${JRE_VERSION} $R1 $R1
    ${LogText} "JRE: ${JRE_VERSION} is 1 newer or 2 older = $R1"

    ; Check whether $R1 != "1"
    ClearErrors
    StrCmp $R1 "1" 0 CheckDone
    SetErrors

  CheckDone:
    Pop $R1

FunctionEnd
