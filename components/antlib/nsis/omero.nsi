######################################################################
# OMERO NSIS Script
#
# Copyright 2009 Glencoe Software, Inc. All rights reserved.
# Use is subject to license terms supplied in LICENSE.txt
#
# This script is compiled by makensis into a Windows setup
# executable. This can be done via the ant target:
#
#   ant release-nsis
#
# which generates the omero_installer.exe under target.
#
# Installer Usage:
#
#   omero_installer # GUI
#   omero_installer /S
#        /D=D:\OmeroInstallationDirectory
#        /DATA=D:\OmeroDataDir
#
# Other:
#   Define GUI_DEBUG to use the nsisdbg plugin
#
# Plugins used:
#  - http://nsis.sourceforge.net/DumpLog_plug-in
#  - http://nsis.sourceforge.net/ExecDos_plug-in
#  - http://nsis.sourceforge.net/GetVersion_(Windows)_plug-in
#  - http://nsis.sourceforge.net/Inetc_plug-in
#  - http://nsis.sourceforge.net/MD5_plugin
#  - http://nsis.sourceforge.net/Nsisdbg_plug-in
#  - http://nsis.sourceforge.net/Nsisunz_plug-in
#
######################################################################

#  - Check etc with http://nsis.sourceforge.net/TextCompare (TextCompareS)
#   -- Or use: http://sourceforge.net/projects/nsispatchgen
#   -- VPatch/example.nsi
#  - DE/REACTIVATE/XXX
#  - Error handling, e.g. after RMDIR
#  - Call DoBugReport (requires reading file into for POST)
#  - !define MUI_PAGE_CUSTOMFUNCTION_LEAVE "checkDirectory"
#  - Test for infinite loops in string replacement
#  - Running as admin
#  - APPDATA: http://stackoverflow.com/questions/116876/how-do-you-set-directory-permissions-in-nsis
#   -- http://nsis.sourceforge.net/AccessControl_plug-in
#  - Add "Complete install" for use by uninstaller
#  - On failure, open InstallLog or send it to the feedback
#  - uninstall
#    -- use rebootok for itself
#    -- leave the directory and var and POSSIBLY etc
#     --- http://nsis.sourceforge.net/Advanced_Uninstall_Log_NSIS_Header
#    -- should shutdown the server properly
#     --- Have our own path
#     --- What about postgres
#     --- More careful about which registries entries are created
#
# REVIEW (i.e. testing/refactoring)
#   Startup
#   Logging everywhere
#   Move all macros, etc. to nsh
#   Uninstall
#   POSSIBILITES:
#    - PG installed/downloaded/not (remote db)
#    - Ice installed/downloaded/not
#    - Java installed/downloaded/not
#    - OMERO installed/downloaded/not
#    - DB keep/don't keep
#  - Handling going back and forth between windows
#
#
# Next steps:
#  - Test for PIL individually (in case python already installed) ~ DONE, but it needs to match the py version
#  - Handle 64bit and Vista : http://forums.winamp.com/showthread.php?threadid=301724
#  - Using only zips (with no compression) in bundle
#  - http://nsis.sourceforge.net/Uninstall_only_installed_files
#  - http://nsis.sourceforge.net/Move_data_between_ListBoxes (users, etc.)
#  - http://nsis.sourceforge.net/Category:Text_Files_Manipulation_Functions
#  - http://nsis.sourceforge.net/Docs/NSISdl/ReadMe.txt (Proxies)
#  - http://nsis.sourceforge.net/NSIS_Simple_Firewall_Plugin to allow ping
#  - http://nsis.sourceforge.net/UAC_plug-in
#  - http://nsis.sourceforge.net/ModernUI_Mod_to_Display_Images_while_installing_files
#  - http://nsis.sourceforge.net/Adding_DropList_with_available_drives_instead_of_directory_page
#  - http://nsis.sourceforge.net/Multi-volume_Distribution
#  - http://nsis.sourceforge.net/Uninstall_Renamed-Moved-Copied_Shortcuts (ShellLink_plug-in)
#  - http://nsis.sourceforge.net/Setting_Default_Location_for_2nd_(Data)_Directory_Page
#  - http://nsis.sourceforge.net/RecFind:_Recursive_FindFirst,_FindNext,_FindClose
#  - http://nsis.sourceforge.net/NSIS_Beyond_a_Traditional_Installation_II
#  - http://nsis.sourceforge.net/Silent_database_import_installer
#  - http://nsis.sourceforge.net/One_Installer_with_Different_Installation_Files_Each_Time
#  - http://www.symantec.com/connect/articles/update-sql-your-nsis-installer
#  - http://forums.winamp.com/showthread.php?threadid=288129 (displaying rtfs)
#  - Look/Feel: Startup Icons
#   -- Use RTF for readme and license
#   -- http://nsis.sourceforge.net/Changing_Title_and_Subtitle_Fonts_on_MUI_Pages
#   -- InstTypes
#   -- http://nsis.sourceforge.net/New_header_wizard_and_checkbox_graphics_for_MUI
#   -- Splash : http://www.theresearchkitchen.com/blog/archives/date/2006/09
#  - SilentInstall
#   -- http://nsis.sourceforge.net/Examples/silent.nsi
#   -- http://pginstaller.projects.postgresql.org/silent.html
#   -- http://forums.winamp.com/showthread.php?postid=2211896
#  - 5.2 Predefines (${__LINE__}, ${__SECTION__}, ${__FUNCTION__} for logging)


######################################################################
# Code paths for installer:
#
# If installer already running:
#     exit
#
# If already installed:
#     ("installed == uninstaller path and installation directory found)
#     Ask to run uninstaller
#     If user says no or execution fails, abort.
#     Otherwise, continue as if not installed.
#
# If not installed:
#     Ask for license confirmation
#     Ask for Startup Menu folder name
#     Ask for chosen directory (use previous install if uninstaller run)
#         If contains space, disable "Next" button
#         If directory is in use, disable "Next" button
#     Ask for components to be installed (each chooses a section)
#     Run prerequsites check
#         ....
#     Run configuration
#         ....
#
######################################################################

######################################################################
# Variables
######################################################################

; Constants
Var INSDIR
Var INSINI
Var COMSPEC
Var ICONS_GROUP
; Exec variables (not constant)
Var CommandLine
Var ExitCode
Var Message
; Gui controls (not constant)
Var Skipping
Var dLabelX
Var dStartY
Var dOffsetY
Var dFieldX
Var dLabelW
Var dLabelH
Var dFieldW
Var dFieldH
Var dCurrentRow
Var dCurrentY

######################################################################
# DEFINITIONS
######################################################################
#
# The following are set on the command-line by build.xml's
# release-nsis target
#
!ifndef VERSION
  !define VERSION '0.0.0.0'
!endif
!ifndef OMERO_VERSION
  !define OMERO_VERSION 'unknown_build'
!endif
!ifndef DBVERSION
  !define DBVERSION 'unknown_db'
!endif
!ifndef DBPATCH
  !define DBPATCH 'unknown_patch'
!endif
!ifndef INCLUDE_DIR
  !define INCLUDE_DIR 'components\antlib\resources'
!endif
!ifndef INSTALLER_NAME
  !define INSTALLER_NAME 'omero_installer.exe'
!endif

#
# Unconditionally set
#
!define PRODUCT_NAME "OMERO.platform"
!define PRODUCT_VERSION "${VERSION}"
!define PRODUCT_PUBLISHER "The Open Microscopy Environment"
!define PRODUCT_WEB_SITE "http://www.openmicroscopy.org"
!define PRODUCT_INST_KEY "Software\${PRODUCT_NAME}"
!define PRODUCT_INST_ROOT_KEY "HKLM"
!define PRODUCT_UNINST_KEY "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}"
!define PRODUCT_UNINST_ROOT_KEY "HKLM"
!define PRODUCT_STARTMENU_REGVAL "OMERO.platform:StartMenuDir"

Name "${PRODUCT_NAME} ${OMERO_VERSION}"
OutFile "${INSTALLER_NAME}"
InstallDir "C:\OMERO-${OMERO_VERSION}"
ShowInstDetails show
ShowUnInstDetails show
XPStyle on
AutoCloseWindow false
DirText 'Target directory for OMERO installation. Spaces are NOT allowed. If the directory contains a subdirectory named $\"var$\" then it will be considered in use.'

!define MUI_ICON "${NSISDIR}\Contrib\Graphics\Icons\modern-install.ico"
!define MUI_UNICON "${NSISDIR}\Contrib\Graphics\Icons\modern-uninstall.ico"
!define MUI_STARTMENUPAGE_DEFAULTFOLDER "${PRODUCT_NAME}"
!define MUI_STARTMENUPAGE_REGISTRY_ROOT "${PRODUCT_UNINST_ROOT_KEY}"
!define MUI_STARTMENUPAGE_REGISTRY_KEY "${PRODUCT_UNINST_KEY}"
!define MUI_STARTMENUPAGE_REGISTRY_VALUENAME "${PRODUCT_STARTMENU_REGVAL}"
!define MUI_FINISHPAGE_SHOWREADME "$INSTDIR\README.txt"
!define MUI_ABORTWARNING
!define MUI_STARTMENUPAGE_NODISABLE
!define MUI_CUSTOMFUNCTION_ABORT myAbort
!define MUI_CUSTOMFUNCTION_GUIINIT myGUIInit
!define MUI_CUSTOMFUNCTION_UNGUIINIT un.myGUIInit

######################################################################
# INCLUDES
######################################################################

!addplugindir "${INCLUDE_DIR}"
!addincludedir "${INCLUDE_DIR}"
!include "EnvVarUpdate.nsh"       ; Working with the registry
!include "TextLog.nsh"            ; Logging
!include FileFunc.nsh             ; Cli parsing for silent install
!insertmacro GetParameters        ; "
!insertmacro GetOptions           ; "
!include LogicLib.nsh             ; Custom pages & If/EndIf logic
!include WinMessages.nsh          ; Custom pages
!include nsDialogs.nsh            ; "
!include MUI.nsh                  ; Look and feel
!include third_party.nsh          ; Java installation, ReplaceInFile, etc.
!include omero.nsh                ; Our code
#!include "RelGotoPage.nsh"

######################################################################
# MUI - 1.67 compatible ------
######################################################################

; INSTALLER -----------------
!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_LICENSE "LICENSE.txt"
!insertmacro MUI_PAGE_STARTMENU Application $ICONS_GROUP
!insertmacro MUI_PAGE_COMPONENTS
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES

Page custom ConfigInstructions "" ""
Page custom ConfigNewDatabaseAdmin LeaveNewDatabaseAdmin ""
Page custom ConfigNewDatabaseOwner LeaveNewDatabaseOwner ""
Page custom ConfigDatabase LeaveDatabase ""
Page custom ConfigServer LeaveServer ""
Page custom ConfigWeb LeaveWeb ""
!insertmacro MUI_PAGE_FINISH

; UNINSTALLER -----------------
!insertmacro MUI_UNPAGE_WELCOME
!insertmacro MUI_UNPAGE_INSTFILES

; Other
!insertmacro MUI_LANGUAGE "English"

######################################################################
# Component Sections
######################################################################

!macro StartAction SEC

  StrCpy $INSDIR "$INSTDIR\var\install"
  StrCpy $INSINI "$PLUGINSDIR\install.ini"

  SetOverwrite off
  SetOutPath "$INSTDIR"
  CreateDirectory "$INSDIR"

  Call GetLocalTime
  Pop $R0 ;Variable (for day)
  Pop $R1 ;Variable (for month)
  Pop $R2 ;Variable (for year)
  Pop $R3 ;Variable (for day of week name)
  Pop $R4 ;Variable (for hour)
  Pop $R5 ;Variable (for minute)
  Pop $R6 ;Variable (for second)
  StrCpy $R7 "$R2.$R1.$R0-$R4:$R5:$R6"

  ${LogSetFileName} "$INSDIR\InstallLog.txt"
  ${LogSetOn}
  ${LogText} ""
  ${LogText} "======================================================"
  ${LogText} " OMERO INSTALL ${SEC} : $R7 "
  ${LogText} "======================================================"
  ${LogText} ""

  WriteIniStr "$INSINI" "${SEC}" "Started" "true"

!macroend

!macro FinishAction SEC

  WriteIniStr "$INSINI" "${SEC}" "Finished" "true"

!macroend


Section "OMERO.server" SECSRV

  !insertmacro StartAction "Server"

  ${Requires} "Python"
  ${Requires} "Tables"
  ${Requires} "Java"
  ${Requires} "Ice"

  File "README.txt"
  File /r "dist\bin"
  File /r "dist\etc"
  #File /r "dist\include" ===== REACTIVATE
  File /r "dist\lib"
  File /r "dist\sql"

  !insertmacro MUI_STARTMENU_WRITE_BEGIN Application
  SetShellVarContext All
  CreateDirectory "$SMPROGRAMS\$ICONS_GROUP"
  CreateShortCut "$SMPROGRAMS\$ICONS_GROUP\Start server.lnk" "python.exe" "$INSTDIR\bin\omero admin start"
  CreateShortCut "$SMPROGRAMS\$ICONS_GROUP\Deploy configuration.lnk" "python.exe" "$INSTDIR\bin\omero admin deploy"
  CreateShortCut "$SMPROGRAMS\$ICONS_GROUP\Stop server.lnk" "python.exe" "$INSTDIR\bin\omero admin stop"
  CreateDirectory "$SMPROGRAMS\$ICONS_GROUP\configuration"
  CreateShortCut "$SMPROGRAMS\$ICONS_GROUP\configuration\All configuration files.lnk" "$INSTDIR\etc"
  CreateShortCut "$SMPROGRAMS\$ICONS_GROUP\configuration\Grid descriptor (windefault.xml).lnk" "write.exe" "$INSTDIR\etc\grid\windefault.xml"
  CreateShortCut "$SMPROGRAMS\$ICONS_GROUP\configuration\Shared elements (templates.xml).lnk" "write.exe" "$INSTDIR\etc\grid\templates.xml"
  CreateShortCut "$SMPROGRAMS\$ICONS_GROUP\configuration\Logging (winlog4j.xml).lnk" "write.exe" "$INSTDIR\etc\winlog4j.xml"
  CreateDirectory "$SMPROGRAMS\$ICONS_GROUP\logs"
  CreateShortCut "$SMPROGRAMS\$ICONS_GROUP\logs\All Logs.lnk" "$INSTDIR\var\logs"
  CreateShortCut "$SMPROGRAMS\$ICONS_GROUP\logs\Blitz-0.lnk" "$INSTDIR\var\logs\Blitz-0.log"
  CreateShortCut "$SMPROGRAMS\$ICONS_GROUP\logs\Processor-0.lnk" "$INSTDIR\var\logs\Processor-0.log"
  CreateShortCut "$SMPROGRAMS\$ICONS_GROUP\logs\Tables-0.lnk" "$INSTDIR\var\logs\Tables-0.log"
  CreateShortCut "$SMPROGRAMS\$ICONS_GROUP\README.lnk" $INSTDIR\README.txt
  !insertmacro MUI_STARTMENU_WRITE_END

  !insertmacro FinishAction "Server"

SectionEnd

Section "OMERO.web" SECWEB

  !insertmacro StartAction "Web"

  ${Requires} "Python"
  ${Requires} "Nginx"
  ${Requires} "PIL"
  ${Requires} "Ice"

  !insertmacro MUI_STARTMENU_WRITE_BEGIN Application
  SetShellVarContext All
  CreateDirectory "$SMPROGRAMS\$ICONS_GROUP"
  CreateDirectory "$SMPROGRAMS\$ICONS_GROUP\logs"
  CreateShortCut "$SMPROGRAMS\$ICONS_GROUP\logs\OMEROweb.log.lnk" "$INSTDIR\var\logs\OMEROweb.log"
  !insertmacro MUI_STARTMENU_WRITE_END

  !insertmacro FinishAction "Web"

SectionEnd

Section "New OMERO database" SECDB

  ${Requires} "PostgreSQL"
  ${Requires} "Python"
  ${Requires} "Ice"

  !insertmacro StartAction "Db"
  !insertmacro FinishAction "Db"

SectionEnd

Section "New binary repository" SECDATA

  !insertmacro StartAction "Data"

  # Requires nothing

  !insertmacro FinishAction "Data"

SectionEnd

!insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
  !insertmacro MUI_DESCRIPTION_TEXT ${SECDB}   "$(SECDB_DESC)"
  !insertmacro MUI_DESCRIPTION_TEXT ${SECDATA} "$(SECDATA_DESC)"
  !insertmacro MUI_DESCRIPTION_TEXT ${SECSRV}  "$(SECSRV_DESC)"
  !insertmacro MUI_DESCRIPTION_TEXT ${SECWEB}  "$(SECWEB_DESC)"
!insertmacro MUI_FUNCTION_DESCRIPTION_END


######################################################################
# Other Sections
######################################################################

Section -AdditionalIcons

  !insertmacro StartAction "AdditionalIcons"

  !insertmacro MUI_STARTMENU_WRITE_BEGIN Application
  SetShellVarContext All
  WriteIniStr "$INSTDIR\${PRODUCT_NAME}.url" "InternetShortcut" "URL" "${PRODUCT_WEB_SITE}"
  CreateDirectory "$SMPROGRAMS\$ICONS_GROUP"
  CreateShortCut "$SMPROGRAMS\$ICONS_GROUP\Website.lnk" "$INSTDIR\${PRODUCT_NAME}.url"
  CreateShortCut "$SMPROGRAMS\$ICONS_GROUP\Uninstall.lnk" "$INSTDIR\uninst.exe"
  !insertmacro MUI_STARTMENU_WRITE_END

  !insertmacro FinishAction "AdditionalIcons"

SectionEnd

Section Uninstall

  ExecWait '"python.exe" $INSTDIR\bin\omero admin stop'
  ${If} ${Errors}
    MessageBox MB_OK|MB_ICONEXCLAMATION "Failed to stop server. Aborting"
    Abort
  ${EndIf}

  SetShellVarContext All
  !insertmacro MUI_STARTMENU_GETFOLDER "Application" $ICONS_GROUP

  ClearErrors
  RMDir /r "$SMPROGRAMS\$ICONS_GROUP"
  ${If} ${Errors}
      MessageBox MB_OK|MB_ICONEXCLAMATION "Failed to remove $ICONS_GROUP"
      ClearErrors
  ${EndIf}

  # Server files
  Delete "$INSTDIR\README.txt"
  RMDIR /r "$INSTDIR\lib"
  RMDIR /r "$INSTDIR\include"
  RMDIR /r "$INSTDIR\bin"
  RMDIR /r "$INSTDIR\etc"

  # Extra files
  Delete "$INSTDIR\${PRODUCT_NAME}.url"
  Delete "$INSTDIR\uninst.exe"
  Delete "$SMPROGRAMS\$ICONS_GROUP\Uninstall.lnk"
  Delete "$SMPROGRAMS\$ICONS_GROUP\Website.lnk"

  # REGISTRY
  DeleteRegKey ${PRODUCT_INST_ROOT_KEY} "${PRODUCT_INST_KEY}"
  DeleteRegKey ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}"

  ${If} ${Errors}
      MessageBox MB_OK|MB_ICONEXCLAMATION "Other failures on uninstall"
      ClearErrors
  ${EndIf}

SectionEnd

######################################################################
# MUI Overrides
######################################################################

Function myGUIInit
  !ifdef GUI_DEBUG
    nsisdbg::init /NOUNLOAD
    nsisdbg::sendtolog /NOUNLOAD "myGUIInit"
  !endif
FunctionEnd

Function un.myGUIInit
  !ifdef GUI_DEBUG
    nsisdbg::init /NOUNLOAD
    nsisdbg::sendtolog /NOUNLOAD "un.myGUIInit"
  !endif
FunctionEnd

Function myAbort
  ${LogText} "Aborted by user"
FunctionEnd

######################################################################
# Callbacks
######################################################################

Function .onInit

  ExpandEnvStrings $COMSPEC %COMSPEC%
  Delete "$INSINI"
  InitPluginsDir
  Call DoUpgradeCheck

  ;
  ; Preventing multiple calls
  ;
  System::Call 'kernel32::CreateMutexA(i 0, i 0, t "OMERO_INSTALLER_ON_INIT_MUTEX") i .r1 ?e'
  Pop $R0

  StrCmp $R0 0 +3
  MessageBox MB_OK|MB_ICONEXCLAMATION "The installer is already running."
  Abort

  ;
  ; Forcing uninstall
  ;
  ClearErrors
  ReadRegStr $R1 ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "UninstallString"
  ${If} ${Errors}
    ClearErrors
  ${Else}
    ;
    ; Use the old install directory as the current value if available
    ; The current value otherwise. This will be re-assigned later
    ;
    ReadRegStr $R0 ${PRODUCT_INST_ROOT_KEY} "${PRODUCT_INST_KEY}" "InstallDir"
    ${If} ${Errors}
      ClearErrors
    ${Else}
      StrCpy $INSTDIR "$R0"
    ${EndIf}
    ;
    ; Now perform the uninstall
    ;

    MessageBox MB_OKCANCEL|MB_ICONEXCLAMATION \
    "Already installed under $R0. $\n$\nClick `OK` to remove the \
    previous version or `Cancel` to cancel this upgrade." \
    IDOK uninst
    Abort

    uninst:
    ClearErrors
    ExecWait '$R1 _?=$INSTDIR' ;Do not copy the uninstaller to a temp file
    ${If} ${Errors}
      MessageBox MB_OK "Uninstaller failed. Exiting"
      Abort
    ${EndIf}

  ${EndIf}

FunctionEnd

Function .onVerifyInstDir
  ${If} ${FileExists} "$INSTDIR\var\*.*"
    ${LogText} "$R0 is in use"
    #MessageBox MB_OK "Directory is in use"
    Abort
  ${Else}
    Push "$INSTDIR\var"
    Call CheckForSpaces
    Pop $R0
    ${If} $R0 == 0
      ### OK
    ${Else}
      ${LogText} "$R0 contains spaces"
      #MessageBox MB_OK "Directory contains spaces"
      Abort
    ${EndIf}
  ${EndIf}
FunctionEnd

Function .onInstSuccess
  ${LogText} "onInstSuccess"
  DumpLog::DumpLog "$INSDIR\LogDump.txt" .R0
FunctionEnd

Function .onInstFailed
  ${LogText} "onInstFailed"
  DumpLog::DumpLog "$INSDIR\LogDump.txt" .R0
FunctionEnd

Function un.onInit
  MessageBox MB_ICONQUESTION|MB_YESNO|MB_DEFBUTTON2 "Are you sure you want to completely remove $(^Name) and all of its components?" IDYES +2
  Abort
FunctionEnd

Function un.onUninstSuccess
  MessageBox MB_ICONINFORMATION|MB_OK "$(^Name) was successfully removed from your computer."
FunctionEnd

#################################
# GUI Macros
#################################

!define YELLOW "0xfff799"
!define RED "0xf69679"

!macro CreateDialog DLG LABEL

  Var /GLOBAL ${DLG}Dialog
  Var /GLOBAL ${DLG}Label
  Var /GLOBAL ${DLG}Counter

  ${If} $${DLG}Counter == ""
    StrCpy $${DLG}Counter "0"
  ${Else}
    IntOp $${DLG}Counter $${DLG}Counter + 1
  ${EndIf}

  nsDialogs::Create 1018
  Pop $${DLG}Dialog
  StrCpy $dCurrentRow  0
  StrCpy $dLabelX      20
  StrCpy $dStartY      54
  StrCpy $dOffsetY     20
  StrCpy $dFieldX      150
  StrCpy $dLabelW      80
  StrCpy $dlabelH      15
  StrCpy $dFieldW      140
  StrCpy $dFieldH      15

  ${NSD_CreateLabel} 0 0 100% 32u "${LABEL}"
  Pop $${DLG}Label

!macroend

!macro DialogRow DLG KEY LABEL VALUE TYPE
  Var /GLOBAL ${DLG}${KEY}Field
  Var /GLOBAL ${DLG}${KEY}Label
  Var /GLOBAL ${DLG}${KEY}State
  ; Calculate the proper Y offset
  IntOp $dCurrentY $dCurrentRow * $dOffsetY
  IntOp $dCurrentY $dCurrentY + $dStartY
  IntOp $dCurrentRow $dCurrentRow + 1
  ${NSD_CreateLabel} $dLabelX $dCurrentY $dLabelW $dLabelH "${LABEL}"
  Pop $${DLG}${KEY}Label

  ${If} $${DLG}Counter == 0
    StrCpy $${DLG}${KEY}State "${VALUE}"
    ${LogText} "Set ${DLG}${KEY}Field's  ${DLG}${KEY}State with default: ${VALUE}"
  ${Else}
    ${If} "${TYPE}" == "Password"
      ${LogText} "${DLG}${KEY}Field's ${DLG}${KEY}State currently XXXXXXXXX"
    ${Else}
      ${LogText} "${DLG}${KEY}Field's ${DLG}${KEY}State currently $${DLG}${KEY}State"
    ${EndIf}

  ${EndIf}
  ${NSD_Create${TYPE}} $dFieldX $dCurrentY $dFieldW $dFieldH "$${DLG}${KEY}State"
  Pop $${DLG}${KEY}Field

!macroend

!macro assert_passwords_match KEY
  ${NSD_GetText} $${KEY}passField $${KEY}passState
  ${NSD_GetText} $${KEY}confField $${KEY}confState
  ${If} $${KEY}confState != $${KEY}passState
    MessageBox MB_OK "Passwords don't match"
    ${NSD_SetFocus} $${KEY}passField
    SetCtlColors $${KEY}passField "" ${YELLOW}
    SetCtlColors $${KEY}confField "" ${YELLOW}
    Abort
  ${EndIf}
!macroend

!macro NonEmpty KEY MSG
  ${NSD_GetText} $${KEY}Field $${KEY}State
  StrLen $1 "$${KEY}State"
  ${If} $1 == 0
    ${LogText} "${KEY}State is empty"
    MessageBox MB_OK "${MSG} is required"
    Abort
  ${Else}
    ${LogText} "NonEmpty check: len(${KEY}) = $1"
  ${EndIf}
!macroend

!macro assert_section SEC
  ${Unless} ${SectionIsSelected} ${SEC}
    ${LogText} "${SEC} not selected. Aborting"
    StrCpy $Skipping "1"
    SendMessage $HWNDPARENT "0x408" "1" ""
  ${EndUnless}
!macroend

!macro check_skipping
  ${If} $Skipping != ""
    StrCpy $Skipping ""
    Return
  ${EndIf}
!macroend

######################################################################
# Custom pages for configuration
######################################################################

Function ConfigInstructions
  !insertmacro MUI_HEADER_TEXT "Configuration" "Now that all the necessary files have been copied to $INSTDIR, it is necessary to configuration your installation. The following pages will present you with several options which you are free to modify. In most cases, the default values will work"
  !insertmacro CreateDialog INSTR "Most configuration properties can be modified after install by stopping the server and editing the configuration properties. See $SMPROGRAMS\$ICONS_GROUP\ for options"
  nsDialogs::Show
FunctionEnd

Function ConfigNewDatabaseAdmin
  !insertmacro MUI_HEADER_TEXT "New database setup 1" "First configure the administrative user who can create users and databases"
  !insertmacro CreateDialog newdb "The properties below will be used only during the installation in order to create the database and owner (if necessary)"
  !insertmacro DialogRow newdb host "Database host" "localhost" Text
  !insertmacro DialogRow newdb port "Database port" "5432" Number
  !insertmacro DialogRow newdb user "Admin login" "postgres" Text
  !insertmacro DialogRow newdb pass "Admin password" "omero" Password
  !insertmacro DialogRow newdb conf "Confirm password" "omero" Password
  #Don't really need to confirm single use password!

 !insertmacro assert_section ${SECDB}
 nsDialogs::Show

FunctionEnd

Function LeaveNewDatabaseAdmin
  !insertmacro check_skipping
  !insertmacro NonEmpty newdbhost "Host for the OMERO database"
  !insertmacro NonEmpty newdbport "Port for the OMERO database"
  !insertmacro NonEmpty newdbuser "User for the OMERO database"
  !insertmacro NonEmpty newdbpass "Password for the OMERO database"
  !insertmacro assert_passwords_match newdb
  Push $newdbportState
  Push $newdbhostState
  Push $newdbuserState
  Push $newdbpassState
  Push "this won't exist and that's good" ; $newdbnameState
  Push noexists
  Call AssertDatabase
  ${If} ${Errors}
    Abort
  ${EndIf}
FunctionEnd

Function ConfigNewDatabaseOwner
  !insertmacro MUI_HEADER_TEXT "New database setup 2" "Then configure the database name and owner you would like to use"
  !insertmacro CreateDialog newdb2 "The values entered below are the defaults used by most OMERO installations"
  !insertmacro DialogRow newdb2 name "Database name" "omero" Text
  !insertmacro DialogRow newdb2 ownr "Database owner" "omero" Text
  !insertmacro DialogRow newdb2 dbpass "Owner password" "omero" Password
  !insertmacro DialogRow newdb2 dbconf "Confirm owner password" "omero" Password
  !insertmacro DialogRow newdb2 omeropass "OMERO root password" "omero" Password
  !insertmacro DialogRow newdb2 omeroconf "Confirm root password" "omero" Password

 !insertmacro assert_section ${SECDB}
 nsDialogs::Show

FunctionEnd

Function LeaveNewDatabaseOwner
  !insertmacro check_skipping
  !insertmacro NonEmpty newdb2name "Name of the OMERO database"
  !insertmacro NonEmpty newdb2ownr "Owner of the OMERO database"
  !insertmacro NonEmpty newdb2dbpass "Password for database owner"
  !insertmacro NonEmpty newdb2omeropass "Password for OMERO's root user"
  !insertmacro assert_passwords_match newdb2db
  !insertmacro assert_passwords_match newdb2omero
  Push $newdbportState
  Push $newdbhostState
  Push $newdbuserState
  Push $newdbpassState
  Push $newdb2nameState
  Push noexists
  Call AssertDatabase
  ${If} ${Errors}
    ${NSD_SetFocus} $newdb2nameField
    SetCtlColors $newdb2nameField "" ${YELLOW}
    Abort
  ${EndIf}
FunctionEnd

Function ConfigDatabase

  ${If} ${SectionIsSelected} ${SECDB}
    ${LogText} "Skipping ConfigDatabase since new database created"
    StrCpy $Skipping "1"
    SendMessage $HWNDPARENT "0x408" "1" ""
  ${EndIf}

  !insertmacro MUI_HEADER_TEXT "Database setup" "Configure the existing OMERO database you would like to connect to"
  !insertmacro CreateDialog db "The values entered below are the defaults used by most OMERO installations"
  !insertmacro DialogRow db host "Database host" "localhost" Text
  !insertmacro DialogRow db port "Database port" "5432" Number
  !insertmacro DialogRow db user "User login" "omero" Text
  !insertmacro DialogRow db pass "User password" "omero" Password
  !insertmacro DialogRow db conf "Confirm password" "omero" Password
  !insertmacro DialogRow db name "Database name" "omero" Text

  !insertmacro assert_section ${SECSRV}
  nsDialogs::Show

FunctionEnd

Function LeaveDatabase
  !insertmacro check_skipping
  !insertmacro NonEmpty dbhost "Host of database"
  !insertmacro NonEmpty dbport "Port of database"
  !insertmacro NonEmpty dbname "Name of database"
  !insertmacro NonEmpty dbuser "Database user name"
  !insertmacro NonEmpty dbpass "Database password"
  !insertmacro assert_passwords_match db
  Push $dbportState
  Push $dbhostState
  Push $dbpassState
  Push $dbuserState
  Push $dbnameState
  Push "may not exist yet since newdb hasn't been run"
  Call AssertDatabase
  ${If} ${Errors}
    Abort
  ${EndIf}
FunctionEnd

Function ConfigServer
  !insertmacro MUI_HEADER_TEXT "Server configuration" "If the directory does not exist or is empty, a selection dialog will be created"
  !insertmacro CreateDialog srv "Server configuration : If the directory does not exist or is empty, a selection dialog will be created"
  !insertmacro DialogRow srv data "Data directory" "$INSTDIR\DATA" Text
  !insertmacro DialogRow srv port "Router port" "4064" Number

  !insertmacro assert_section ${SECSRV}
  nsDialogs::Show

FunctionEnd

Function LeaveServer
  !insertmacro check_skipping
  ${If} ${SectionIsSelected} ${SECDATA}
    MessageBox MB_OK "XXX Expecting non-existant directory"
  ${Else}
    MessageBox MB_OK "XXX Expecting existant directory"
  ${EndIf}
  !insertmacro NonEmpty "srvport" "Router port"
  ${NSD_GetText} $srvdataField $srvdataState
  ClearErrors
  ${If} $srvdataState == ""
    nsDialogs::SelectFolderDialog "Select a data directory" "$INSTDIR"
    Pop $srvdataState
    ${NSD_SetText} $srvdataField $srvdataState
    Abort
  ${EndIf}
FunctionEnd

Function ConfigWeb
  !insertmacro MUI_HEADER_TEXT "OMERO.web configuration" "Select the configuration for your web server"
  !insertmacro CreateDialog web "OMERO.web configuration"
  !insertmacro DialogRow web port "Web server port" "8000" Number

  !insertmacro assert_section ${SECWEB}
  nsDialogs::Show

FunctionEnd

Function LeaveWeb
  !insertmacro check_skipping
  !insertmacro NonEmpty "webport" "Router port"
FunctionEnd

!macro ErrFatal M
  StrCpy $Message "${M} : Aborting install..."
  ${LogText} "$Message"
  MessageBox MB_OK "$Message"
  SetErrors
  Quit
!macroend

!macro ErrReturn M
  StrCpy $Message "${M}"
  ${LogText} "$Message"
  MessageBox MB_OK "$Message"
  SetErrors
  Return
!macroend

;
; Usage:
;   Push $dbpassword
;   Push $dbuser
;   Push $dbname
;   Push "exists" or "noexists"
;   Call AssertDatabase
Function AssertDatabase
  Pop $R1 ; "exists" or "noexists". If anything else, no action taken.
  Pop $R2 ; dbname
  Pop $R3 ; dbuser
  Pop $R4 ; dbpass
  Pop $R5 ; dbhost
  Pop $R6 ; dbport

  ${LogText} "Entering AssertDatabase"

  ; Setting up a pgpass.conf file to keep commands from blocking
  ; http://www.postgresql.org/docs/8.3/static/libpq-pgpass.html

  ClearErrors
  FileOpen $0 "$PLUGINSDIR\pgpass.conf" w
  FileWrite $0 "localhost:*:*:*:$R4"
  FileClose $0
  ${If} ${Errors}
    !insertmacro ErrFatal "Failed to create PGPASSFILE. Cannot initialize database"
  ${EndIf}

  FileOpen $0 "$PLUGINSDIR\now.sql" w
  FileWrite $0 "select now();"
  FileClose $0
  ${If} ${Errors}
    !insertmacro ErrFatal "Failed to create now script. Cannot initialize database"
  ${EndIf}

  System::Call 'Kernel32::SetEnvironmentVariableA(t, t) i("PGPASSFILE", "$PLUGINSDIR\pgpass.conf").r0'
  ${If} $0 == 0
    !insertmacro ErrFatal "Failed to set environment variable PGPASSFILE. Cannot initialize database"
  ${EndIf}

  StrCpy $CommandLine '"$COMSPEC" /C "psql -h $R5 -p $R6 -U $R3 -c \l"'
  ${Execute} $CommandLine "Failed to connect to postgres" "" 0
  ${If} ${Errors}
    !insertmacro ErrReturn "Cannot connect to postgres"
    MessageBox MB_OK "$Message"
    SetErrors
  ${Else}
    ${LogText} "Connected to database as $R3"
  ${EndIf}

  StrCpy $CommandLine '"$COMSPEC" /C "psql -h $R5 -p $R6 -U $R3 $R2" < $PLUGINSDIR\now.sql'
  ${If} $R1 == "noexists"
    ${Execute} $CommandLine "Database already exists $R2" "" 1
    ${If} ${Errors}
      !insertmacro ErrReturn "Database $R2 already exists. Please enter a new one"
    ${Else}
      ${LogText} "Database $R2 doesn't exit. Good."
    ${EndIf}
  ${ElseIf} $R1 == "exists"
    ${Execute} $CommandLine "Database $R2 doesn't exist. Please enter a new one" "" 0
    ${If} ${Errors}
      !insertmacro ErrReturn "Database $R2 doesn't exist. Please enter a new one"
    ${Else}
      ${LogText} "Database $R2 exists. Good."
    ${EndIf}
  ${EndIf}

  ${LogText} "Returing from AssertDatabase"
  Push "ok"
  Return

FunctionEnd

Function ActualInstall

  !insertmacro StartAction "ActualInstall"

  ReadIniStr $ExitCode "$INSINI" "SECDB" "Finished"
  MessageBox MB_OK "SECSRV: $ExitCode"
  MessageBox MB_OK "SECDATA: $ExitCode"
  MessageBox MB_OK "SECWEB: $ExitCode"
  MessageBox MB_OK "SECDB: $ExitCode"

  #
  # Installer
  #
  WriteRegStr ${PRODUCT_INST_ROOT_KEY} "${PRODUCT_INST_KEY}" "InstallDir" "$INSTDIR"

  #
  # Uninstaller
  #
  WriteUninstaller "$INSTDIR\uninst.exe"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "DisplayName" "$(^Name)"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "UninstallString" "$INSTDIR\uninst.exe"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "DisplayVersion" "${PRODUCT_VERSION}"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "URLInfoAbout" "${PRODUCT_WEB_SITE}"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "Publisher" "${PRODUCT_PUBLISHER}"

  #
  # Various cleanup targets
  #
  ${LogText} "Disabling bzip2.dll from PyTables"
  ${Execute} 'python $INSDIR\lib\python\omero\install\bzip2_tool.py disable' "Could not disable bzip2" "" 0
  ${If} ${Errors}
    Abort
  ${EndIf}

  #
  # Handle Grid XML replacements
  #
  StrCpy $5 '<property name="omero.example" value="my_value"/>'
  StrCpy $6 'test'
  !insertmacro ReplaceInFile $INSTDIR\etc\grid\windefault.xml "$5" "$6"
  !insertmacro ReplaceInFile $INSTDIR\etc\grid\windefault.xml "value=$\"4063$\"" "value=$\"$R9$\""
  !insertmacro ReplaceInFile $INSTDIR\etc\grid\windefault.xml "c:\\omero_dist" "$INSTDIR"
  !insertmacro ReplaceInFile $INSTDIR\etc\Windows.cfg "c:\omero_dist" "$INSTDIR"

  !insertmacro FinishAction "ActualInstall"

  #
  # Data directory
  #
  CreateDirectory "$R6"
  StrCpy $CommandLine '"$COMSPEC" /C "createdb -U $R3 $R2"'
  ${Execute} $CommandLine "Failed to create database" "" 0
  ${If} ${Errors}
    Goto FatalError
  ${Else}
    ${LogText} "Created database $R2"
  ${EndIf}

  ;
  ; Create database
  ;
  StrCpy $CommandLine '"$COMSPEC" /C "createlang -U $R3 plpgsql $R2"'
  ${Execute} $CommandLine "Failed to add plpgsql" "" 0
  ${If} ${Errors}
    Goto FatalError
  ${Else}
    ${LogText} "Added plpgsql to $R2"
  ${EndIf}

  StrCpy $CommandLine '"python.exe" $INSTDIR\bin\omero db script ${DBVERSION} ${DBPATCH}'
  ${Execute} $CommandLine "Failed to create database script" "$R0" 0
  ${If} ${Errors}
    Goto FatalError
  ${Else}
    ${LogText} "Created database script: ${DBVERSION}__${DBPATCH}.sql"
  ${EndIf}

  StrCpy $CommandLine '"$COMSPEC" /C "psql -U $R3 $R2 < "$INSTDIR\${DBVERSION}__${DBPATCH}.sql"'
  ${Execute} $CommandLine "Failed to execute database script" "$R4" 0
  ${If} ${Errors}
    Goto FatalError
  ${Else}
    ${LogText} "Created database from script"
  ${EndIf}

  !insertmacro FinishAction "ActualInstall"
  Return

  FatalError:
    Abort "Fatal error during database creation"

FunctionEnd
