######################################################################
# OMERO NSIS Header
#
# Copyright 2009 Glencoe Software, Inc. All rights reserved.
# Use is subject to license terms supplied in LICENSE.txt
#
# This header file is imported by the main .nsi script
#
######################################################################

######################################################################
# Language Strings
######################################################################

LangString PREREQ_PAGE_TITLE ${LANG_ENGLISH} "Prerequisites"
LangString PREREQ_PAGE_SUBTITLE ${LANG_ENGLISH} "Specify installation directories or have prerequisites installed"
LangString CONFIG_PAGE_TITLE ${LANG_ENGLISH} "Configuration"
LangString CONFIG_PAGE_SUBTITLE ${LANG_ENGLISH} "Options for your OMERO install"

LangString SECSRV_DESC ${LANG_ENGLISH} "Install and start the OMERO server"
LangString SECWEB_DESC ${LANG_ENGLISH} "Install and start the OMERO web client"
LangString SECDB_DESC ${LANG_ENGLISH} "Create a new database based on configuration options"
LangString SECDATA_DESC ${LANG_ENGLISH} "Create a new binary data repository based on configuration options"

######################################################################
# Helpers functions
######################################################################

Function ExecuteLog
  IntOp $1 $1 + 1
  Pop $2
  ${LogText} " [ExecDos] Line $1: $2"
FunctionEnd

!define Execute "!insertmacro _ExecuteMacro"
!macro _ExecuteMacro Command ErrorMsg StandardIn
  ClearErrors
  StrLen $1 "${StandardIn}"
  ${LogText} "Executing ${Command} with stdin of length $1"
  # ExecWait ${Command} $ExitCode
  GetFunctionAddress $9 ExecuteLog
  ExecDos::exec /TOFUNC "${Command}" "${StandardIn}" $9
  Pop $ExitCode
  ${If} ${Errors}
  ${OrIf} $ExitCode != "0"
    ${LogText} "${ErrorMsg}: ExitCode = $ExitCode"
    MessageBox MB_OK "${ErrorMsg}: ExitCode = $ExitCode"
    SetErrors
  ${EndIf}
!macroend

!macro AppendChosenPath Sec Key
  StrCpy $1 ""
  ReadINIStr $1 "$INSINI" "${Sec}" "State"
  ${If} $1 != ""
    ${LogText} "Appending $1 to ${Key}"
    ${EnvVarUpdate} $1 "${Key}" "A" "HKLM" "$1"
  ${Else}
    ${LogText} "No ${Key}  value found for ${Sec}"
  ${EndIf}
!macroend

######################################################################
# Installation
######################################################################

!define UNNEEDED "Found on PATH. No input needed"

!define IsInstalled "!insertmacro _IsInstalled"
!macro _IsInstalled Prereq
  ClearErrors
  ReadINIStr $R1 "$INSINI" "${Prereq}" "State"
  ${LogText} "Checking ${Prereq}. State=$R1"

  ${If} $R1 == ""
    StrCpy $Message "${Prereq} is empty"
    ${LogText} "$Message"
    SetErrors
  ${Else}
    ${If} $R1 == "${UNNEEDED}"
      StrCpy $Message "${Prereq} == ${UNNEEDED}"
      ${LogText} "$Message"
    ${Else}
      ClearErrors
      ${If} ${FileExists} "$R1\*.*"
        ${LogText} "$R1 exists for ${Prereq}"
      ${Else}
        StrCpy $Message "$R1 does not exist for ${Prereq}"
        ${LogText} "$Message"
        SetErrors
      ${EndIf}
    ${EndIf}
  ${EndIf}

!macroend

!define Requires "!insertmacro _Requires"
!macro _Requires Prereq

  ${LogText} "Requires ${Prereq}"

  ClearErrors
  ${IsInstalled} $Prereq
  ${If} ${Errors}
    ClearErrors
    !insertmacro Check${Prereq}
    ${IsInstalled} $Prereq
    ${If} ${Errors}
      nsDialogs::SelectFolderDialog "$Message : Manually choose a directory for ${Prereq}. Cancelling will abort the install"
      Pop $ExitCode
      WriteINIStr "$INSINI" $Prereq "State" "$ExitCode"
      ${LogText} "Setting state for $Prereq to $ExitCode after folder dialog"
    ${EndIf}
  ${EndIf}
  ${IsInstalled} ${Prereq}
  ${If} ${Errors}
    ${LogText} "${Prereq} *still* not installed. Aborting"
    Abort
  ${EndIf}

!macroend

!macro CheckPostgreSQL

  Call IsPgInstalled
  Pop $R2 ; Third
  Pop $R1 ; Second
  Pop $R0 ; First

  WriteINIStr "$INSINI" PostgreSQL First "$R0"
  WriteINIStr "$INSINI" PostgreSQL Second "$R1"
  WriteINIStr "$INSINI" PostgreSQL Third "$R2"

  ; If GLOBAL, all are set
  ${If} $R0 == "GLOBAL"
  ${AndIf} $R1 == "GLOBAL"
  ${AndIf} $R2 == "GLOBAL"
    ${LogText} "Disabling PostgreSQL (Field 1)"
    WriteINIStr "$INSINI" "PostgreSQL" "State" "${UNNEEDED}"
    Goto PGReady
  ${EndIf}

  ${If} $R0 == "" ; None
    ${LogText} "No values found for PG (Field 1)"
    ClearErrors
    !insertmacro ConfirmInstall PostgreSQL
    Call GetPg
  ${Else}
    ${If} $R1 != ""
      ${LogText} "Found multiple Postgres instances. Choosing first"
    ${EndIf}
    WriteRegStr ${PRODUCT_INST_ROOT_KEY} "${PRODUCT_INST_KEY}" "PgPath" "$R0"
    WriteINIStr "$INSINI" "PostgreSQL" "State" "$R0"
  ${EndIf}

  PGReady: ; ---------------------------------------------

!macroend

!macro CheckIce

  Call IsIceInstalled
  Pop $R1 ; VS2008
  Pop $R0 ; VS2005
  WriteINIStr "$INSINI" Ice VS2005 "$R0"
  WriteINIStr "$INSINI" Ice VS2008 "$R1"

  ; If GLOBAL, both are always set.
  ${If} $R0 == "GLOBAL"
  ${AndIf} $R1 == "GLOBAL"
    ${LogText} "Disabling Ice"
    WriteINIStr "$INSINI" "Ice" "State" "${UNNEEDED}"
    Goto IceReady
  ${EndIf}

  ${If} $R0 == ""
    ${If} $R1 == ""
      !insertmacro ConfirmInstall Ice
      Call GetIce
    ${Else}
      ${LogText} "Setting Ice (Field 4) State to $R1"
      WriteRegStr ${PRODUCT_INST_ROOT_KEY} "${PRODUCT_INST_KEY}" "IcePath" "$R1"
      WriteINIStr "$INSINI" "Ice" "State" "$R1"
    ${EndIf}
  ${ElseIf} $R1 == ""
    ${If} $R0 != ""
      ${LogText} "Setting Ice (Field 4) State to $R0"
      WriteRegStr ${PRODUCT_INST_ROOT_KEY} "${PRODUCT_INST_KEY}" "IcePath" "$R0"
      WriteINIStr "$INSINI" "Ice" "State" "$R0"
    ${EndIf}
  ${Else}
      MessageBox MB_OK "Two Ices installed: $R0 and $R1. Choosing $R0"
      ${LogText} "Setting Ice (Field 4) State to $R0"
      WriteRegStr ${PRODUCT_INST_ROOT_KEY} "${PRODUCT_INST_KEY}" "IcePath" "$R0"
      WriteINIStr "$INSINI" "Ice" "State" "$R0"
  ${EndIf}

  IceReady: ; ---------------------------------------------
  !insertmacro FinishAction "IcePage"

!macroend

!macro CheckPython

  Call IsPythonInstalled
  Pop $R2 ; Third
  Pop $R1 ; Second
  Pop $R0 ; First
  WriteINIStr "$INSINI" Python First  "$R0"
  WriteINIStr "$INSINI" Python Second "$R1"
  WriteINIStr "$INSINI" Python Third  "$R2"

  ; If GLOBAL, all are set
  ${If} $R0 == "GLOBAL"
  ${AndIf} $R1 == "GLOBAL"
  ${AndIf} $R2 == "GLOBAL"
    ${LogText} "Disabling Python (Field 3)"
    WriteINIStr "$INSINI" "Python" "State" "${UNNEEDED}"
    Goto PythonReady
  ${EndIf}

  ${If} $R0 == "" ; None
    ${LogText} "No values found for Python (Field 3)"
    !insertmacro ConfirmInstall Python
    Call GetPython
  ${Else}
    ${If} $R1 != ""
      ${LogText} "Found multiple Python instances. Choosing first"
    ${EndIf}
    WriteRegStr ${PRODUCT_INST_ROOT_KEY} "${PRODUCT_INST_KEY}" "PythonPath" "$R0"
    WriteINIStr "$INSINI" "Python" "State" "$R0"
  ${EndIf}

  PythonReady: ; ---------------------------------------------

!macroend

!macro CheckJava

  ; This section is slightly different since the GetJre
  ; function is copied code. Adds itself to path
  ;

  !insertmacro StartAction "JavaPage"
  !insertmacro MUI_HEADER_TEXT $(PREREQ_PAGE_TITLE) $(PREREQ_PAGE_SUBTITLE)

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

!macroend


!define Download "!insertmacro _DownloadMacro"
!macro _DownloadMacro Source Target MD5
  Push "${SOURCE}"
  Push "${TARGET}"
  Push "${MD5}"
  Call _DownloadFunction
!macroend
Function _DownloadFunction
  Pop $R2 ; MD5 Checksum
  Pop $R1 ; Target
  Pop $R0 ; Source
  ; Sets errors
  ; ReturnCode "success" | <failure message>

  ClearErrors
  IfFileExists $R1 Checking Downloading
  Downloading:
    nsisdl::download /TIMEOUT=30000 "$R0" "$R1"
    Pop $0
    ${If} $0 == "success"
      ${LogText} "Downloaded $R0 to $R1"
    ${Else}
      ${LogText} "Failed to download: $R0 to $R1"
      Push "Failed to download $R0"
      SetErrors
      Return
    ${EndIf}
  Checking:
    md5dll::GetMD5File "$R1"
    Pop $R3
    ${If} "$R2" != "$R3"
      ${LogText} "$R1 ($R3) != $R2"
      Push "MD5 checked failed for $R1"
      SetErrors
      Return
    ${Else}
      ${LogText} "$R1 ($R3) == $R2"
    ${EndIf}
  Push "success"
  Return
FunctionEnd

;
; Usage: !insertmacro TestIce <ice bin path>
;        ${If} ${Errors} ... ; Failed
;
!macro TestIce Path
  ClearErrors
  GetFunctionAddress $9 ExecuteLog
  ExecDos::exec /TOFUNC '"${Path}glacier2router.exe" --version' "" $9
  Pop $ExitCode
  ${If} $ExitCode == 1
    ClearErrors
  ${EndIf}
  ExecDos::exec /TOFUNC '"${Path}icegridadmin.exe" --version' "" $9
  Pop $ExitCode
  ${If} $ExitCode == 1
    ClearErrors
  ${EndIf}
  ExecDos::exec /TOFUNC '"${Path}icegridnode.exe" --version' "" $9
  Pop $ExitCode
  ${If} $ExitCode == 1
    ClearErrors
  ${EndIf}
!macroend

; Usage:
;   Call IsIceInstalled
;   Pop $0 ; 3.3.1 VS2005 InstallDir or "GLOBAL"
;   Pop $1 ; 3.3.1 VS2008 InstallDir or "GLOBAL"
;   StrCmp $0 "" NotFound Found
;   StrCmp $1 "" NotFound Found
;
; Base on:
;    http://nsis.sourceforge.net/How_to_Detect_.NET_Framework
;
Function IsIceInstalled

  !insertmacro TestIce ""
  ${If} ${Errors}
   ClearErrors ; Not found so keep searching
  ${Else}
    Push "GLOBAL"
    Push "GLOBAL"
    Return
  ${EndIf}

  Push ""
  ReadRegStr $R1 HKLM "${ZEROC_VS2008_KEY}" "InstallDir"
  StrCpy $R1 "$R1" -1 1
  ${If} ${Errors}
    ClearErrors
  ${Else}
    !insertmacro TestIce "$R1bin\"
    ${If} ${Errors}
      ClearErrors
      ${LogText} "$R1 seems to be deleted. Should be removed from registry."
    ${Else}
      Pop $R3
      Push "$R1"
    ${EndIf}
  ${EndIf}

  Push ""
  ReadRegStr $R0 HKLM "${ZEROC_VS2005_KEY}" "InstallDir"
  StrCpy $R0 "$R0" -1 1
  ${If} ${Errors}
    ClearErrors
  ${Else}
    !insertmacro TestIce "$R0bin\"
    ${If} ${Errors}
      ClearErrors
      ${LogText} "$R0 seems to be deleted. Should be removed from registry."
    ${Else}
      Pop $R3
      Push "$R0"
    ${EndIf}
  ${EndIf}

FunctionEnd

Function GetIce
  ClearErrors
  StrCpy $R0 "$INSDIR\${ICE_INSTALLER}"
  ${Download} "${ICE_URL}" "$R0" "${ICE_MD5}"
  ${IfNot} ${Errors}
    StrCpy $R0 '"msiexec.exe" /i $R0'
    ${Execute} $R0 "Ice MSI installer failed" ""
  ${EndIf}
FunctionEnd

;
; Usage:
;  Call IsPgInstalled
;   Pop $0 ; First instance, "GLOBAL", or ""
;   Pop $1 ; Second instance, "GLOBAL", or ""
;   Pop $2 ; Third instance, "GLOBAL", or ""
;
; Based on:
;  http://nsis.sourceforge.net/EnumUsersReg

Function IsPgInstalled

  ExecWait '"psql.exe" --version' $ExitCode
  ${LogText} "psql exit code: $ExitCode"
  ${If} ${Errors}
  ${OrIf} $ExitCode == 1
    ClearErrors
  ${Else}
    push "GLOBAL"
    push "GLOBAL"
    push "GLOBAL"
    return
  ${EndIf}

  ${LogText} "Looking for PG under ${PG_KEY}"
  StrCpy $R0 0
  ${While} $R0 < 3
    EnumRegKey $R1 HKLM "${PG_KEY}" $R0
    IntOp $R0 $R0 + 1
    ${If} $R1 != ""
      ReadRegStr $R2 HKLM "${PG_KEY}\$R1" "Base Directory"
      ${LogText} "Found PG: $R1 = $R2"
      push "$R2bin"
    ${Else}
      push ""
    ${EndIf}
  ${EndWhile}

FunctionEnd

Function GetPg
  StrCpy $R0 "$INSDIR\${PG_ZIP}"
  StrCpy $R4 "$INSDIR\postgres"

  ClearErrors
  StrCpy $R0 "$INSDIR\${ICE_INSTALLER}"
  ${Download} "${PG_URL}" "$R0" "${PG_MD5}"
  ${IfNot} ${Errors}
    IfFileExists "$R4\setup.bat" Installing Unzipping
    Unzipping:
      CreateDirectory "$R4"
      nsisunz::Unzip "$R0" "$R4"
      Pop $0
      StrCmp $0 "success" Installing
        ${LogText} "Unzip failed: $0"
      ${LogText} "Unzipping of PostgreSQL installer returned $0"
    Installing:
      ; Copied from setup.bat
      StrCpy $R2 '"$R4\vcredist_x86.exe"'
      ${Execute} $R2 "vcredist_x86 failed" ""
      IfErrors Failure 0
      StrCpy $R2 '"msiexec.exe" /i "$R4\postgresql-8.3.msi"'
      ${Execute} $R2 "PostgreSQL MSI installer failed" ""
      # http://pginstaller.projects.postgresql.org/silent.html
      # ExecWait 'msiexec /i postgresql-8.0.0-rc1-int.msi  /qr INTERNALLAUNCH=1 ADDLOCAL=server,psql,docs SERVICEDOMAIN="%COMPUTERNAME%"
      #      SERVICEPASSWORD="SecretWindowsPassword123" SUPERPASSWORD="VerySecret" BASEDIR="c:\postgres" TRANSFORMS=:lang_de'
  ${EndIf}

  Failure:
    SetErrors
    Push "Failed to install PostgreSQL"

FunctionEnd

;
; Usage:
;  Call IsPythonInstalled
;   Pop $0 ; First instance, "GLOBAL", or ""
;   Pop $1 ; Second instance, "GLOBAL", or ""
;   Pop $2 ; Third instance, "GLOBAL", or ""
;
Function IsPythonInstalled

  ExecWait '"python.exe" --version' $ExitCode
  ${LogText} "python.exe exit code: $ExitCode"
  ${If} ${Errors}
  ${OrIf} $ExitCode == 1
    ClearErrors
  ${Else}
    push "GLOBAL"
    push "GLOBAL"
    push "GLOBAL"
    return
  ${EndIf}

  ${LogText} "Looking for Python under ${PY_KEY}"
  StrCpy $R0 0
  ${While} $R0 < 3
    EnumRegKey $R1 HKLM "${PY_KEY}" $R0
    IntOp $R0 $R0 + 1
    ${If} $R1 != ""
      ReadRegStr $R2 HKLM "${PY_KEY}\$R1\InstallPath" ""
      ${LogText} "Found PY: $R1 = $R2"
      push "$R2"
    ${Else}
      push ""
    ${EndIf}
  ${EndWhile}

FunctionEnd

Function GetPython
  # Python
  ClearErrors
  StrCpy $R0 "$INSDIR\${PYAS_INSTALLER}"
  ${Download} "${PYAS_URL}" "$R0" "${PYAS_MD5}"
  ${IfNot} ${Errors}
    StrCpy $R2 '"msiexec.exe" /i $R0'
    ${Execute} $R2 "Python MSI installer failed" ""
    # For silent: msiexec /i ActivePython-<version>.msi /qn+ INSTALLDIR=C:\myapps\Python ADDLOCAL=core,doc
    # See: http://docs.activestate.com/activepython/2.4/installnotes.html#install_silent
  ${EndIf}

  # PIL
  StrCpy $R1 "$INSDIR\${PIL_INSTALLER}"
  ${Download} "${PIL_URL}" "$R0" "${PIL_MD5}"
  ${IfNot} ${Errors}
    StrCpy $R2 '"$R1"'
    ${Execute} $R2 "PIL installer failed" ""
  ${EndIf}

FunctionEnd


; StrReplace
; Replaces all ocurrences of a given needle within a haystack with another string
; Written by dandaman32
; http://nsis.sourceforge.net/StrRep

Var STR_REPLACE_VAR_0
Var STR_REPLACE_VAR_1
Var STR_REPLACE_VAR_2
Var STR_REPLACE_VAR_3
Var STR_REPLACE_VAR_4
Var STR_REPLACE_VAR_5
Var STR_REPLACE_VAR_6
Var STR_REPLACE_VAR_7
Var STR_REPLACE_VAR_8

Function StrReplace
  Exch $STR_REPLACE_VAR_2
  Exch 1
  Exch $STR_REPLACE_VAR_1
  Exch 2
  Exch $STR_REPLACE_VAR_0
    StrCpy $STR_REPLACE_VAR_3 -1
    StrLen $STR_REPLACE_VAR_4 $STR_REPLACE_VAR_1
    StrLen $STR_REPLACE_VAR_6 $STR_REPLACE_VAR_0
    loop:
      IntOp $STR_REPLACE_VAR_3 $STR_REPLACE_VAR_3 + 1
      StrCpy $STR_REPLACE_VAR_5 $STR_REPLACE_VAR_0 $STR_REPLACE_VAR_4 $STR_REPLACE_VAR_3
      StrCmp $STR_REPLACE_VAR_5 $STR_REPLACE_VAR_1 found
      StrCmp $STR_REPLACE_VAR_3 $STR_REPLACE_VAR_6 done
      Goto loop
    found:
      StrCpy $STR_REPLACE_VAR_5 $STR_REPLACE_VAR_0 $STR_REPLACE_VAR_3
      IntOp $STR_REPLACE_VAR_8 $STR_REPLACE_VAR_3 + $STR_REPLACE_VAR_4
      StrCpy $STR_REPLACE_VAR_7 $STR_REPLACE_VAR_0 "" $STR_REPLACE_VAR_8
      StrCpy $STR_REPLACE_VAR_0 $STR_REPLACE_VAR_5$STR_REPLACE_VAR_2$STR_REPLACE_VAR_7
      StrLen $STR_REPLACE_VAR_6 $STR_REPLACE_VAR_0
      Goto loop
    done:
  Pop $STR_REPLACE_VAR_1 ; Prevent "invalid opcode" errors and keep the
  Pop $STR_REPLACE_VAR_1 ; stack as it was before the function was called
  Exch $STR_REPLACE_VAR_0
FunctionEnd

!macro _strReplaceConstructor OUT NEEDLE NEEDLE2 HAYSTACK
  Push "${HAYSTACK}"
  Push "${NEEDLE}"
  Push "${NEEDLE2}"
  Call StrReplace
  Pop "${OUT}"
!macroend

!define StrReplace '!insertmacro "_strReplaceConstructor"'

; Usage:
;   !insertmacro ReplaceInFile SOURCE_FILE SEARCH_TEXT REPLACEMENT
;
; See:
;   http://nsis.sourceforge.net/ReplaceInFile

!macro ReplaceInFile SOURCE_FILE SEARCH_TEXT REPLACEMENT
  Push "${SOURCE_FILE}"
  Push "${SEARCH_TEXT}"
  Push "${REPLACEMENT}"
  Call RIF
!macroend

Function RIF

  ClearErrors  ; want to be a newborn

  Exch $0      ; REPLACEMENT
  Exch
  Exch $1      ; SEARCH_TEXT
  Exch 2
  Exch $2      ; SOURCE_FILE

  Push $R0     ; SOURCE_FILE file handle
  Push $R1     ; temporary file handle
  Push $R2     ; unique temporary file name
  Push $R3     ; a line to sar/save
  Push $R4     ; shift puffer

  IfFileExists $2 +1 RIF_error      ; knock-knock
  FileOpen $R0 $2 "r"               ; open the door

  GetTempFileName $R2               ; who's new?
  FileOpen $R1 $R2 "w"              ; the escape, please!

  RIF_loop:                         ; round'n'round we go
    FileRead $R0 $R3                ; read one line
    IfErrors RIF_leaveloop          ; enough is enough
    RIF_sar:                        ; sar - search and replace
      Push "$R3"                    ; (hair)stack
      Push "$1"                     ; needle
      Push "$0"                     ; blood
      Call StrReplace               ; do the bartwalk
      StrCpy $R4 "$R3"              ; remember previous state
      Pop $R3                       ; gimme s.th. back in return!
      StrCmp "$R3" "$R4" +1 RIF_sar ; loop, might change again!
    FileWrite $R1 "$R3"             ; save the newbie
  Goto RIF_loop                     ; gimme more

  RIF_leaveloop:                    ; over'n'out, Sir!
    FileClose $R1                   ; S'rry, Ma'am - clos'n now
    FileClose $R0                   ; me 2

    Delete "$2.old"                 ; go away, Sire
    Rename "$2" "$2.old"            ; step aside, Ma'am
    Rename "$R2" "$2"               ; hi, baby!

    ClearErrors                     ; now i AM a newborn
    Goto RIF_out                    ; out'n'away

  RIF_error:                        ; ups - s.th. went wrong...
    SetErrors                       ; ...so cry, boy!

  RIF_out:                          ; your wardrobe?
  Pop $R4
  Pop $R3
  Pop $R2
  Pop $R1
  Pop $R0
  Pop $2
  Pop $0
  Pop $1

FunctionEnd

;
; Usage:
;   Push "string"
;   Call CheckForSpaces
;   Pop $R0
;  StrCmp $R0 0 NoSpaces
;
; See:
;   http://nsis.sourceforge.net/Check_for_spaces_in_a_directory_path
;
Function CheckForSpaces
 Exch $R0
 Push $R1
 Push $R2
 Push $R3
 StrCpy $R1 -1
 StrCpy $R3 $R0
 StrCpy $R0 0
 loop:
   StrCpy $R2 $R3 1 $R1
   IntOp $R1 $R1 - 1
   StrCmp $R2 "" done
   StrCmp $R2 " " 0 loop
   IntOp $R0 $R0 + 1
 Goto loop
 done:
 Pop $R3
 Pop $R2
 Pop $R1
 Exch $R0
FunctionEnd


; http://nsis.sourceforge.net/Get_Local_Time
;----------------------------------------------------------------------------
; Superseded by     : GetTime function.
;----------------------------------------------------------------------------
; Title             : Get Local Time
; Short Name        : GetLocalTime
; Last Changed      : 22/Feb/2005
; Code Type         : Function
; Code Sub-Type     : One-way Output
;----------------------------------------------------------------------------
; Required          : System plugin.
; Description       : Gets the current local time of the user's computer
;----------------------------------------------------------------------------
; Function Call     : Call GetLocalTime
;
;                     Pop "$Variable1"
;                       Day.
;
;                     Pop "$Variable2"
;                       Month.
;
;                     Pop "$Variable3"
;                       Year.
;
;                     Pop "$Variable4"
;                       Day of the week name.
;
;                     Pop "$Variable5"
;                       Hour.
;
;                     Pop "$Variable6"
;                       Minute.
;
;                     Pop "$Variable7"
;                       Second.
;----------------------------------------------------------------------------
; Author            : Diego Pedroso
; Author Reg. Name  : deguix
;----------------------------------------------------------------------------
 
Function GetLocalTime
 
  # Prepare variables
  Push $0
  Push $1
  Push $2
  Push $3
  Push $4
  Push $5
  Push $6
 
  # Call GetLocalTime API from Kernel32.dll
  System::Call '*(&i2, &i2, &i2, &i2, &i2, &i2, &i2, &i2) i .r0'
  System::Call 'kernel32::GetLocalTime(i) i(r0)'
  System::Call '*$0(&i2, &i2, &i2, &i2, &i2, &i2, &i2, &i2)i \
  (.r4, .r5, .r3, .r6, .r2, .r1, .r0,)'
 
  # Day of week: convert to name
  StrCmp $3 0 0 +3
    StrCpy $3 Sunday
      Goto WeekNameEnd
  StrCmp $3 1 0 +3
    StrCpy $3 Monday
      Goto WeekNameEnd
  StrCmp $3 2 0 +3
    StrCpy $3 Tuesday
      Goto WeekNameEnd
  StrCmp $3 3 0 +3
    StrCpy $3 Wednesday
      Goto WeekNameEnd
  StrCmp $3 4 0 +3
    StrCpy $3 Thursday
      Goto WeekNameEnd
  StrCmp $3 5 0 +3
    StrCpy $3 Friday
      Goto WeekNameEnd
  StrCmp $3 6 0 +2
    StrCpy $3 Saturday
  WeekNameEnd:
 
  # Minute: convert to 2 digits format
        IntCmp $1 9 0 0 +2
          StrCpy $1 '0$1'
 
  # Second: convert to 2 digits format
        IntCmp $0 9 0 0 +2
          StrCpy $0 '0$0'
 
  # Return to user
  Exch $6
  Exch
  Exch $5
  Exch
  Exch 2
  Exch $4
  Exch 2
  Exch 3
  Exch $3
  Exch 3
  Exch 4
  Exch $2
  Exch 4
  Exch 5
  Exch $1
  Exch 5
  Exch 6
  Exch $0
  Exch 6
 
FunctionEnd
;

; Usage: Call DefineOmeroUrls
;        Pop $0 ; UPGRADE
;        Pop $1 ; BUG
;
Function DefineOmeroUrls
  ;;
  ;; POSTing feedback
  ;;
    /*
    Push "HTTP.nsi"
    Call LineCount
    Pop $R0 ; Line count
    StrCpy "$

    StrCpy $R3 "...\n"
    IntOp $R1 100 + 0 ; Counter
    Push $R0
    Push "HTTP.nsi"
    Call ReadFileLine
    Pop $R2 ;output string
    StrCpy ...UNFINISHED
    */

  !define BUGURL "http://users.openmicroscopy.org.uk/~brain/omero/bugcollector.php"
  !define EMAIL "comments@openmicroscopy.org.uk"

  StrCpy $0 "${EMAIL}"
  StrCpy $1 "nsis failed"
  StrCpy $2 "some error"

  # StrCpy $R4 "${BUGURL}?email=$0&comment=$1&error=$2&extra=$3&type=NSIS_INSTALLER&java_version=$4&java_class_path=NONE&os_name=$5&os_arch=$6&os_version=$7"
  StrCpy $R4 "${BUGURL}?email=$0&comment=$1&error=$2&type=NSIS_INSTALLER"
  StrCpy $R5 "http://upgrade.openmicroscopy.org.uk/?version=${OMERO_VERSION}"

  GetVersion::WindowsName
  Pop $R0
  GetVersion::WindowsType
  Pop $R1
  StrCpy $R4 "$R4&os_name=$R0 $R1"
  StrCpy $R5 "$R5&os.name=$R0 $R1"
  GetVersion::WindowsVersion
  Pop $R0
  StrCpy $R4 "$R4&os_version=$R0"
  StrCpy $R5 "$R5&os.version=$R0"
  GetVersion::WindowsPlatformArchitecture
  Pop $R0
  StrCpy $R4 "$R4&os_arch=$R0"
  StrCpy $R5 "$R5&os.arch=$R0"
  GetVersion::WindowsServicePack

FunctionEnd

Function DoUpgradeCheck

  Call DefineOmeroUrls
  Pop $0 ; Upgrade URL
  Pop $1 ; Unneeded

  Delete "$INSDIR\upgrade_check.txt"
  inetc::get /SILENT \
     /USERAGENT "OMERO.nsis_installer" \
     /TIMEOUT 10000 "$R5" "$INSDIR\upgrade_check.txt" /end
  Pop $ExitCode
  ${If} $ExitCode == "OK"
    ${LogText} "Checked upgrade"
  ${Else}
    ${LogText} "GET error: $ExitCode"
  ${EndIf}

FunctionEnd

Function DoBugReport

  Call DefineOmeroUrls
  Pop $0 ; Upgrade URL (unneeded)
  Pop $1 ; Bug URL

  inetc::post "test feedback here" /TIMEOUT 30000 /USERAGENT "OMERO.nsis_feedback" \
    /BANNER "Sending OMERO Installer feedback" \
    $1 "$INSDIR\bug_post.txt"
  Pop $ExitCode
  ${If} $ExitCode == "OK"
    MessageBox MB_OK "Upload successful. Thanks for helping to improve OMERO"
  ${Else}
    MessageBox MB_OK|MB_ICONEXCLAMATION "Upload Error. You may want to send $INSDIR\InstallLog.txt to the ome-users mailing list"
  ${EndIf}

FunctionEnd

