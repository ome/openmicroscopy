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

LangString SECSRV_DESC ${LANG_ENGLISH} "Install and start the OMERO server"
LangString SECWEB_DESC ${LANG_ENGLISH} "Install and start the OMERO web client"
LangString SECDB_DESC ${LANG_ENGLISH} "Create a new database based on configuration options"
LangString SECDATA_DESC ${LANG_ENGLISH} "Create a new binary data repository based on configuration options"

######################################################################
# Helpers functions
######################################################################

;
; Callback function used by ${Execute}/_ExecuteMacro to print stdout
; from commands
;
Function ExecuteLog
  IntOp $1 $1 + 1
  Pop $2
  ${LogText} " [ExecDos] Line $1: $2"
FunctionEnd

;
; General system call with special handling for logging,
; expected return codes.
;
; Parameters:
;   Command - with all necessary white space handling
;   ErrorMsg - Used in logging. Also StrCpy'd to $Message
;   StandardIn - Not currently used
;   Expected - 0 if should succeed, not 0 if should fail
;
!define Execute "!insertmacro _ExecuteMacro"
!macro _ExecuteMacro Command ErrorMsg StandardIn Expected

  ; Setup
  ClearErrors
  StrLen $1 "${StandardIn}"
  ${LogText} "Executing ${Command} with stdin of length $1"
  GetFunctionAddress $9 ExecuteLog

  ; Execute
  ExecDos::exec /TOFUNC "${Command}" "${StandardIn}" $9
  Pop $ExitCode

  ; Check
  ;
  ${If} ${Errors}
    ${If} ${Expected} == "0"
      ${LogText} "${ErrorMsg}: Errors found when expected success"
      # MessageBox MB_OK "${ErrorMsg}"
      SetErrors
    ${Else}
      ${LogText} "errors (expected)"
      ClearErrors
    ${EndIf}
  ${Else} ; ---- No errors
    ${If} ${Expected} == "0"
      ${If} $ExitCode == "0"
        ${LogText} "success (expected)"
      ${Else}
        ${LogText} "${ErrorMsg}: ExitCode = $ExitCode when expecting success"
        # MessageBox MB_OK "${ErrorMsg}"
        SetErrors
      ${EndIf}
    ${Else} ; ---- Failure expected
      ${If} $ExitCode == "0"
        ${LogText} "${ErrorMsg}: ExitCode = $ExitCode, Expected failure (${Expected})"
        # MessageBox MB_OK "${ErrorMsg}"
        SetErrors
      ${Else}
        ${LogText} "failure (expected)"
      ${EndIf}
    ${EndIf}
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

# The installation macros are a litte convoluted, but should be
# straight-forward to refactor. From the top-level, the call
# graph is:
#
# ${Requires} PostgreSQL|Java|Python|Ice|PIL
#  |--> IsInstalled X (if false, set errors)
#  |--> CheckX
#  | |--> IsXInstalled
#  | |--> ConfirmInstall X
#  | \--> GetX
#  |   |-> Download
#  |   |-> Check MD5
#  |   \-> Install
#  \--> IsInstalled X (if false, set errors)
#

!define UNNEEDED "Found on PATH. No input needed"

;
; Asserts that a given section in the main file (omero.nsi)
; requires the prerequisite ("Prereq")
;
!define Requires "!insertmacro _Requires"
!macro _Requires Prereq

  ${LogText} "Requires ${Prereq}"

  ClearErrors
  ${IsInstalled} ${Prereq}
  ${If} ${Errors}
    ClearErrors
    !insertmacro Check${Prereq}
    ${IsInstalled} ${Prereq}
    ${If} ${Errors}
      nsDialogs::SelectFolderDialog "$Message : Manually choose a directory for ${Prereq}. Cancelling will abort the install"
      Pop $ExitCode
      WriteINIStr "$INSINI" ${Prereq} "State" "$ExitCode"
      ${LogText} "Setting state for ${Prereq} to $ExitCode after folder dialog"
    ${EndIf}
  ${EndIf}
  ${IsInstalled} ${Prereq}
  ${If} ${Errors}
    ${LogText} "${Prereq} *still* not installed. Aborting"
    Abort
  ${EndIf}

!macroend

;
; Checks the current installer state to see if the
; prereq has already been found installed. This is
; a short-cut, mostly, in case the back button is
; used.
;
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

!macro CheckPIL

  Call IsPILInstalled
  Pop $R0 ; First
  WriteINIStr "$INSINI" PIL Value  "$R0"

  ; If GLOBAL, installed into python
  ${If} $R0 == "GLOBAL"
    ${LogText} "PIL value is GLOBAL"
    WriteINIStr "$INSINI" "PIL" "State" "${UNNEEDED}"
    Goto PILReady
  ${EndIf}

  ${If} $R0 == "" ; None
    ${LogText} "PIL not found"
    !insertmacro ConfirmInstall PIL
    Call GetPIL
  ${EndIf}

  PILReady: ; ---------------------------------------------

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

!macroend

;
; Provides downloading and md5 checking of requirements.
; See the URL and MD5 definitions in omero.nsi
;
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
    ${Execute} $R0 "Ice MSI installer failed" "" 0
  ${EndIf}
  Call IsIceInstalled
  Pop $0 ; 3.3.1 VS2005 InstallDir or "GLOBAL"
  Pop $1 ; 3.3.1 VS2008 InstallDir or "GLOBAL"
  ${If} $0 == ""
    ${If} $1 == ""
      StrCpy $Message "No Ice installation found. Aborting..."
      ${LogText} "$Message"
      MessageBox MB_OK "$Message"
      Quit
    ${Else}
      StrCpy $2 $1
    ${EndIf}
  ${Else}
    ${If} $1 != ""
      ${LogText} "Two Ice installations found on GetIce?!? $1 and $2. Using first."
    ${EndIf}
    StrCpy $2 $0
  ${EndIf}
  ${LogText} "Updating PATH and PYTHONPATH with $2"
  ${EnvVarUpdate} $1 "PATH" "A" "HKLM" "$2\bin"
  ${EnvVarUpdate} $1 "PYTHONPATH" "A" "HKLM" "$2\python"
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

  StrCpy $CommandLine 'psql --version'
  ${Execute} $CommandLine "PostgreSQL is not installed" "" 0
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
      ${Execute} $R2 "vcredist_x86 failed" "" 0
      IfErrors Failure 0
      StrCpy $R2 '"msiexec.exe" /i "$R4\postgresql-8.3.msi"'
      ${Execute} $R2 "PostgreSQL MSI installer failed" "" 0
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

  StrCpy $CommandLine 'python --version'
  ${Execute} $CommandLine "Python is not installed" "" 0
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
  ClearErrors
  StrCpy $R0 "$INSDIR\${PYAS_INSTALLER}"
  ${Download} "${PYAS_URL}" "$R0" "${PYAS_MD5}"
  ${IfNot} ${Errors}
    StrCpy $R2 '"msiexec.exe" /i $R0'
    ${Execute} $R2 "Python MSI installer failed" "" 0
    # For silent: msiexec /i ActivePython-<version>.msi /qn+ INSTALLDIR=C:\myapps\Python ADDLOCAL=core,doc
    # See: http://docs.activestate.com/activepython/2.4/installnotes.html#install_silent
  ${EndIf}
FunctionEnd

;
; Usage:
;  Call IsPILInstalled
;   Pop $0 ; "GLOBAL" or ""
;
Function IsPILInstalled

  StrCpy $CommandLine 'python -mImage'
  ${Execute} $CommandLine "PIL is not installed" "" 0
  ${If} ${Errors}
  ${OrIf} $ExitCode == 1
    push ""
    ClearErrors
  ${Else}
    push "GLOBAL"
    return
  ${EndIf}
FunctionEnd

Function GetPIL
  ClearErrors
  StrCpy $R1 "$INSDIR\${PIL_INSTALLER}"
  ${Download} "${PIL_URL}" "$R1" "${PIL_MD5}"
  ${IfNot} ${Errors}
    StrCpy $R2 '"$R1"'
    ${Execute} $R2 "PIL installer failed" "" 0
  ${EndIf}
FunctionEnd

######################################################################
# Connectivty (GET/POST)
######################################################################

; Usage: Call DefineOmeroUrls
;        Pop $0 ; UPGRADE
;        Pop $1 ; BUG
;
Function DefineOmeroUrls
  ;;
  ;; POSTing feedback
  ;;
    /*
    XXX
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
  Pop $1 ; Bug URL (unneeded)

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

