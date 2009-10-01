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

