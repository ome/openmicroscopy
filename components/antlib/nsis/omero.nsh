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
# ${Requires} PostgreSQL|Java|Python|Ice|PIL|Tables|Nginx
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
 #
 # This corrective action doesn't make sense for all the libraries,
 # and most users won't know what to click on anyway. Something like
 # this may eventually be useful, so just commenting out.
 #
 #   ${If} ${Errors}
 #     nsDialogs::SelectFolderDialog "$Message : Manually choose a directory for ${Prereq}. Cancelling will abort the install"
 #     Pop $ExitCode
 #     WriteINIStr "$INSINI" ${Prereq} "State" "$ExitCode"
 #     ${LogText} "Setting state for ${Prereq} to $ExitCode after folder dialog"
 #   ${EndIf}
 #
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
  ${If} ${Errors}
    MessageBox MB_OK "Failed to download/verify ${SOURCE}"
    Abort
  ${EndIf}
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
    inetc::get /CAPTION "Required library download..." /TIMEOUT 30000 /BANNER "Downloading $R0..." "$R0" "$R1"
    Pop $0
    ${If} $0 == "OK"
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
; The most common thing to do with a download (*.exe) is
; to execute it.
;
!define DownloadAndRun "!insertmacro _DownloadAndRunMacro"
!macro _DownloadAndRunMacro Source Target MD5 Message
  Push "${SOURCE}"
  Push "${TARGET}"
  Push "${MD5}"
  Push "${Message}"
  Call _DownloadAndrunFunction
!macroend
Function _DownloadAndRunFunction
  Pop $R3 ; Message
  Pop $R2 ; MD5 Checksum
  Pop $R1 ; Target
  Pop $R0 ; Source
  ${Download} "$R0" "$R1" "$R2"
  ${IfNot} ${Errors}
    StrCpy $R4 '"$R1"'
    ${Execute} $R4 "$R3" "" 0
  ${EndIf}
FunctionEnd

!define ConfirmInstall "!insertmacro _ConfirmInstallMacro"
!macro _ConfirmInstallMacro Prereq

  MessageBox MB_ICONQUESTION|MB_YESNO|MB_DEFBUTTON1 "Component selections requires ${Prereq}. Would you like to install it? 'No' aborts the install." IDYES ${Prereq}Confirmed
    Abort
  ${Prereq}Confirmed:

!macroend

!include check_postgres.nsh
!include check_ice.nsh
!include check_java.nsh
!include check_python.nsh
!include check_pil.nsh
!include check_tables.nsh
!include check_nginx.nsh


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

