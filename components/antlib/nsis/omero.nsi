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
######################################################################

# Next stages:
# Create new data dir
# Create new database
 ;; Use previous properties on later screens
# Configuration database connection
# Configuration proper directory, ports, and show how to modify other properties
# Configuration WEB
#  - ${EnvVarUpdate} $1 "PYTHONPATH" "A" "HKLM" "C:\Ice-3.3.1\python"
#  - Check etc with http://nsis.sourceforge.net/TextCompare (TextCompareS)
#   -- Or use: http://sourceforge.net/projects/nsispatchgen
#  - Check if any of our INCLUDE_DIR functions are in the distro
#  - On failure, open InstallLog or send it to the feedback
#  - Using only zips (with no compression) in bundle
#  - DE/REACTIVATE/XXX
#  - Smarter handling of non-local databases
#  - http://nsis.sourceforge.net/Uninstall_only_installed_files
#  - Pop ups only from omero.nsi
#  - Move all macros, etc. to nsh
#  - Pop ups if requierments not fulfilled (or abort)
#  - Decide on InstallOptions: http://forums.winamp.com/showthread.php?threadid=251391, http://nsis.sourceforge.net/Add-on_Custom_Installer_Sample_/w_InstallOptions
#  - Error handling, e.g. after RMDIR
#  - Handling going back and forth between windows
#  - Logging
#  - Call DoUpgradeCheck
#  - Call DoBugReport
#  - !define MUI_PAGE_CUSTOMFUNCTION_LEAVE "checkDirectory"
#  - do something appropriate with etc
#    - Test for infinite loops in string replacement
#    - Handle 64bit and Vista
#     -- http://forums.winamp.com/showthread.php?threadid=301724
#    - APPDATA: http://stackoverflow.com/questions/116876/how-do-you-set-directory-permissions-in-nsis
#    - http://nsis.sourceforge.net/AccessControl_plug-in
#    - Check all /* comments
#    - Have installer itself ping registry
#    - Add "Complete install" for use by uninstaller
#  - Data directory at beginning: http://forums.winamp.com/printthread.php?s=9ca5d467ad9c9844408db61e71d031e3&threadid=214898
#    - uninstall
#      -- use rebootok for itself
#      -- leave the directory and var and POSSIBLY etc
#       --- http://nsis.sourceforge.net/Advanced_Uninstall_Log_NSIS_Header
#      -- should shutdown the server properly
#      -- Have our own path
#      -- What about postgres
#      -- More careful about which registries entries are created
#
# REVIEW:
#   Startup
#   Uninstall
#   POSSIBILITES:
#    - PG installed/downloaded/not
#    - Ice installed/downloaded/not
#    - Java installed/downloaded/not
#    - OMERO installed/downloaded/not
#    - DB keep/don't keep
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
# Future possibilities:
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
#  - SilentInstall
#   -- http://nsis.sourceforge.net/Examples/silent.nsi
#   -- http://pginstaller.projects.postgresql.org/silent.html
#   -- http://forums.winamp.com/showthread.php?postid=2211896


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

; Largely constant
Var INSDIR
Var INSINI
Var COMSPEC
Var ICONS_GROUP
; Not constant
Var CommandLine
Var ExitCode
Var Message
; GUI elements
Var hnDialog
Var hnLabel

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
# The following downloads can also be overwritten
#
!define ICE_VC80_INSTALLER "Ice-3.3.1-VC80.msi"
!ifndef ICE_VC80_URL
  !define ICE_VC80_URL "http://www.zeroc.com/download/Ice/3.3/Ice-3.3.1-VC80.msi"
!endif
!ifndef ICE_VC80_MD5
  !define ICE_VC80_MD5 "a8c1e305a9d267a57e22a1fb78aa0c91"
!endif

!define ICE_VC90_INSTALLER "Ice-3.3.1-VC90.msi"
!ifndef ICE_VC90_URL
  !define ICE_VC90_URL "http://www.zeroc.com/download/Ice/3.3/Ice-3.3.1-VC90.msi"
!endif
!ifndef ICE_VC90_MD5
  !define ICE_VC90_MD5 "b958546a84c98dc49036f7b10d1a8ce2"
!endif

!ifdef ICE_VC90
  !define ICE_INSTALLER "${ICE_VC90_INSTALLER}"
  !define ICE_URL "${ICE_VC90_URL}"
  !define ICE_MD5 "${ICE_VC90_MD5}"
!else
  !define ICE_INSTALLER "${ICE_VC80_INSTALLER}"
  !define ICE_URL "${ICE_VC80_URL}"
  !define ICE_MD5 "${ICE_VC80_MD5}"
!endif

!define PG_ZIP "postgresql-8.3.7-1.zip"
!ifndef PG_URL
  !define PG_URL "http://wwwmaster.postgresql.org/redir/198/h/binary/v8.3.7/win32/postgresql-8.3.7-1.zip"
!endif
!ifndef PG_MD5
  !define PG_MD5 "ec22457ddcadc0924bd891550dacb67b"
!endif

!define PY_INSTALLER "python-2.5.1.msi"
!ifndef PY_URL
  !define PY_URL "http://www.python.org/ftp/python/2.5.1/python-2.5.1.msi"
!endif
!ifndef PY_MD5
  !define PY_MD5 "a1d1a9c07bc4c78bd8fa05dd3efec87f"
!endif

!define PYAS_INSTALLER "python-2.5.4.3-win32-x86.msi"
!ifndef PYAS_URL
  !define PYAS_URL "http://downloads.activestate.com/ActivePython/windows/2.5/ActivePython-2.5.4.3-win32-x86.msi"
!endif
!ifndef PYAS_MD5
  !define PYAS_MD5 "c5ed8ea033d35ba87c48885d921f08c9"
!endif

!define PIL_INSTALLER "PIL-1.1.6.win32-py2.5.exe"
!ifndef PIL_URL
  !define PIL_URL "http://effbot.org/downloads/PIL-1.1.6.win32-py2.5.exe"
!endif
!ifndef PIL_MD5
  !define PIL_MD5 "e1347e7055f42f3e50c20e5935a4b62e"
!endif

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

#
# Registry keys
#
!define JRE_KEY "SOFTWARE\JavaSoft\Java Runtime Environment"
!define ZEROC_VS2005_KEY "Software\ZeroC\Ice 3.3.1 for Visual Studio 2005"
!define ZEROC_VS2008_KEY "Software\ZeroC\Ice 3.3.1 for Visual Studio 2008"
!define PG_KEY "SOFTWARE\PostgreSQL\Installations"
!define PY_KEY "SOFTWARE\Python\PythonCore"

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

Name "${PRODUCT_NAME} ${PRODUCT_VERSION}"
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
!define MUI_INSTFILESPAGE_FINISHHEADER_TEXT "Copying files finished"
!define MUI_INSTFILESPAGE_FINISHHEADER_SUBTEXT "All files have been copied to the install directory"
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
!include java.nsh                 ; Java installation
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
Page custom ConfigNewDatabase LeaveNewDatabase ""
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
  ${Requires} "Java"
  ${Requires} "Ice"

  File "README.txt"
  File /r "dist/bin"
  File /r "dist/etc"
  #File /r "dist/include" ===== REACTIVATE
  File /r "dist/lib"
  File /r "dist/sql"

  !insertmacro MUI_STARTMENU_WRITE_BEGIN Application
  SetShellVarContext All
  CreateDirectory "$SMPROGRAMS\$ICONS_GROUP"
  CreateShortCut "$SMPROGRAMS\$ICONS_GROUP\Start server.lnk" "python.exe" "$INSTDIR\bin\omero admin start"
  CreateShortCut "$SMPROGRAMS\$ICONS_GROUP\Stop server.lnk" "python.exe" "$INSTDIR\bin\omero admin stop"
  CreateDirectory "$SMPROGRAMS\$ICONS_GROUP\configuration"
  CreateShortCut "$SMPROGRAMS\$ICONS_GROUP\configuration\Grid descriptor (windefault.xml).lnk" "write.exe" "$INSTDIR\etc\grid\windefault.xml"
  CreateShortCut "$SMPROGRAMS\$ICONS_GROUP\configuration\Logging (winlog4j.xml).lnk" "write.exe" "$INSTDIR\etc\winlog4j.xml"
  CreateDirectory "$SMPROGRAMS\$ICONS_GROUP\logs"
  CreateShortCut "$SMPROGRAMS\$ICONS_GROUP\logs\All Logs.lnk" "$INSTDIR\var\logs"
  CreateShortCut "$SMPROGRAMS\$ICONS_GROUP\README.lnk" $INSTDIR\README.txt
  !insertmacro MUI_STARTMENU_WRITE_END

  !insertmacro FinishAction "Server"

SectionEnd

Section "OMERO.web" SECWEB

  !insertmacro StartAction "Web"

  ${Requires} "Python"
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
  !insertmacro MUI_DESCRIPTION_TEXT ${SECSRV}  "$(SECSRV_DESC)"
  !insertmacro MUI_DESCRIPTION_TEXT ${SECWEB}  "$(SECWEB_DESC)"
  !insertmacro MUI_DESCRIPTION_TEXT ${SECDB}   "$(SECDB_DESC)"
  !insertmacro MUI_DESCRIPTION_TEXT ${SECDATA} "$(SECDATA_DESC)"
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
  ${un.EnvVarUpdate} $0 "PATH" "R" "HKLM" "C:\Ice-3.3.1\bin"
  ${un.EnvVarUpdate} $0 "PYTHONPATH" "R" "HKLM" "C:\Ice-3.3.1\bin"
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
  ## DEACTIVATE
  Goto DoInit

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

  ;
  ; Actual initialization
  ;
  DoInit:
    Delete "$INSINI"
    InitPluginsDir

FunctionEnd

Function .onVerifyInstDir
   ${If} ${FileExists} "$INSTDIR\var\*.*"
     Push "$INSTDIR\var"
     Call CheckForSpaces
     Pop $R0
     ${If} $R0 == 0
       ### OK
     ${Else}
       Abort "Directory contains spaces"
     ${EndIf}
   ${Else}
       Abort "Directory is in use"
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

;XXX
/*
Function .onPrevPage
  ${LogText} "onPrevPage"
  MessageBox MB_OK "Actions already performed. Please cancel and use the uninstaller"
  Abort
FunctionEnd
*/

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

!macro CreateDialog LABEL
  nsDialogs::Create 1018
  Pop $hnDialog

  StrCpy $R0 0   ; Which row
  StrCpy $R1 20  ; Label x
  StrCpy $R2 54  ; y start
  StrCpy $R3 20  ; y offset
  StrCpy $R4 150 ; Field x
  StrCpy $R5 80  ; Label width
  StrCpy $R6 12  ; Label height
  StrCpy $R7 140 ; Field width
  StrCpy $R8 12  ; Field height

  ${NSD_CreateLabel} 0 0 100% 32u "${LABEL}"
  Pop $hnLabel

!macroend

!macro DialogRow KEY LABEL VALUE TYPE
  Var /GLOBAL ${KEY}Field
  Var /GLOBAL ${KEY}Label
  Var /GLOBAL ${KEY}State
  IntOp $R9 $R0 * $R3
  IntOp $R9 $R9 + $R2
  IntOp $R0 $R0 + 1
  ${NSD_CreateLabel} $R1 $R9 $R5u $R6u "${LABEL}"
  Pop $${KEY}Label

  ${NSD_Create${TYPE}} $R4 $R9 $R7u $R8u "${VALUE}"
  Pop $${KEY}Field

!macroend

!macro assert_passwords_match KEY
  ${NSD_GetText} $${KEY}passField $${KEY}passState
  ${NSD_GetText} $${KEY}confField $${KEY}confState
  ${If} $${KEY}confState != $${KEY}passState
    MessageBox MB_OK "Passwords don't match"
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

######################################################################
# Custom pages for configuration
######################################################################

Function ConfigInstructions
  !insertmacro CreateDialog "Configuration : Now that all the necessary files have been copied to $INSTDIR, it is necessary to configuration your installation. The following pages will present you with several options which you are free to modify. In most cases, the default values will work"
  nsDialogs::Show
FunctionEnd

Function ConfigNewDatabase
  !insertmacro CreateDialog "New database setup : The properties below will be used only during the installation in order to create a new database. The database user entered must have permissions to create a new database (XXX and possibly a new user)"
  !insertmacro DialogRow newdbhost "Database host" "localhost" Text
  !insertmacro DialogRow newdbport "Database port" "5432" Number
  !insertmacro DialogRow newdbuser "Admin login" "postgres" Text
  !insertmacro DialogRow newdbpass "Admin password" "omero" Password
  !insertmacro DialogRow newdbconf "Confirm password" "omero" Password
  !insertmacro DialogRow newdbname "Database name" "omero" Text
  !insertmacro DialogRow newdbownr "Database owner" "omero" Text
  !insertmacro DialogRow rootpass "OMERO root password" "omero" Password
  !insertmacro DialogRow rootconf "Confirm root password" "omero" Password

  nsDialogs::Show

FunctionEnd

Function LeaveNewDatabase
  !insertmacro NonEmpty "newdbhost" "Host for the OMERO database"
  !insertmacro NonEmpty "newdbport" "Port for the OMERO database"
  !insertmacro NonEmpty "newdbuser" "User for the OMERO database"
  !insertmacro NonEmpty "newdbpass" "Password for the OMERO database"
  !insertmacro NonEmpty "newdbname" "Name of the OMERO database"
  !insertmacro NonEmpty "newdbownr" "Owner of the OMERO database"
  !insertmacro NonEmpty "rootpass" "Password for OMERO's root user"
  !insertmacro assert_passwords_match newdb
  !insertmacro assert_passwords_match root
  Push $newdbpassState
  Push $newdbuserState
  Push $newdbnameState
  Push "noexists"
  Call AssertDatabase
FunctionEnd

Function ConfigDatabase
  !insertmacro CreateDialog "Database configuration : The properties below are acceptable for default installations."
  !insertmacro DialogRow dbhost "Database host" "localhost" Text
  !insertmacro DialogRow dbport "Database port" "5432" Number
  !insertmacro DialogRow dbuser "User login" "omero" Text
  !insertmacro DialogRow dbpass "User password" "omero" Password
  !insertmacro DialogRow dbconf "Confirm password" "omero" Password
  !insertmacro DialogRow dbname "Database name" "omero" Text

  nsDialogs::Show

FunctionEnd

Function LeaveDatabase
  !insertmacro NonEmpty "dbhost" "Host of database"
  !insertmacro NonEmpty "dbport" "Port of database"
  !insertmacro NonEmpty "dbname" "Name of database"
  !insertmacro NonEmpty "dbuser" "Database user name"
  !insertmacro NonEmpty "dbpass" "Database password"
  !insertmacro assert_passwords_match db
  Push $dbpassState
  Push $dbuserState
  Push $dbnameState
  Push "may not exist yet since newdb hasn't been run"
  Call AssertDatabase
FunctionEnd

Function ConfigServer
  !insertmacro CreateDialog "Server configuration : If the directory does not exist or is empty, a selection dialog will be created"
  !insertmacro DialogRow srvdata "Data directory" "$INSTDIR\DATA" Text
  !insertmacro DialogRow srvport "Router port" "4063" Number

  nsDialogs::Show

FunctionEnd

Function LeaveServer
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
  !insertmacro CreateDialog "OMERO.web configuration"
  !insertmacro DialogRow webport "Web server port" "8000" Number

  nsDialogs::Show

FunctionEnd

Function LeaveWeb
  !insertmacro NonEmpty "webport" "Router port"
FunctionEnd


;
; Usage:
;   Push $dbpassword
;   Push $dbuser
;   Push $dbname
;   Push "exists" or "noexists"
;   Call ValidateDatabase
Function AssertDatabase
  Pop $R1 ; "exists" or "noexists". If anything else, no action taken.
  Pop $R2 ; dbname
  Pop $R3 ; dbuser
  Pop $R4 ; dbpass

  ${LogText} "Entering AssertDatabase"

  ; Setting up a pgpass.conf file to keep commands from blocking
  ; http://www.postgresql.org/docs/8.3/static/libpq-pgpass.html

  ClearErrors
  FileOpen $0 "$PLUGINSDIR\pgpass.conf" w
  FileWrite $0 "localhost:*:*:*:$R4"
  FileClose $0
  ${If} ${Errors}
    ${LogText} "Failed to create PGPASSFILE"
    MessageBox MB_OK "Failed to create PGPASSFILE. Cannot initialize database"
    Goto FatalError
  ${EndIf}

  FileOpen $0 "$PLUGINSDIR\now.sql" w
  FileWrite $0 "select now();"
  FileClose $0
  ${If} ${Errors}
    ${LogText} "Failed to create now script"
    MessageBox MB_OK "Failed to create now script. Cannot initialize database"
    Goto FatalError
  ${EndIf}

  System::Call 'Kernel32::SetEnvironmentVariableA(t, t) i("PGPASSFILE", "$PLUGINSDIR\pgpass.conf").r0'
  ${If} $0 == 0
    ${LogText} "Failed to set environment variable PGPASSFILE. Cannot initialize database"
    MessageBox MB_OK "Failed to set environment variable PGPASSFILE. Cannot initialize database"
    Goto FatalError
  ${EndIf}

  StrCpy $CommandLine '"$COMSPEC" /C "psql -U $R3 -c \l"'
  ${Execute} $CommandLine "Failed to connect to postgres" ""
  ${If} ${Errors}
    MessageBox MB_OK "Cannot connect to postgres"
    Abort
  ${Else}
    ${LogText} "Connected to postgres as $R3"
  ${EndIf}

  ${If} $R1 == "noexists"
  ;${OrIf} $R1 == "noexists" XXX Cancelling for the minute
    StrCpy $CommandLine '"$COMSPEC" /C "psql -U $R3 $R2" < $PLUGINSDIR\now.sql'
    ${Execute} $CommandLine "Database already exists $R2" ""
    ${IfNot} ${Errors}
      ${If} $R1 == "noexists"
        MessageBox MB_OK "Database $R2 already exists. Please enter a new one"
        Abort
      ${Else}
        ${LogText} "Database $R2 exists. Good."
      ${EndIf}
    ${Else}
      ${If} $R1 == "exists"
        MessageBox MB_OK "Database $R2 doesn't exist. Please enter a new one"
        Abort
      ${Else}
        ${LogText} "Database $R2 doesn't exit. Good."
      ${EndIf}
   ${EndIf}
 ${EndIf}

  ${LogText} "Returing from AssertDatabase"
  Return

  ;
  ; Error handling
  ;
  FatalError:
    ${LogText} "Aborting from AssertDatabase"
    MessageBox MB_OK "Aborting install..."
    Quit

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

  ;
  ; Update the environment values
  ;
  !insertmacro AppendChosenPath "${Prereq}" "PATH"

  ;
  ; Handle Grid XML replacements
  ;
  StrCpy $5 '<property name="omero.example" value="my_value"/>'
  StrCpy $6 'test'
  !insertmacro ReplaceInFile $INSTDIR\etc\grid\windefault.xml "$5" "$6"
  !insertmacro ReplaceInFile $INSTDIR\etc\grid\windefault.xml "value=$\"4063$\"" "value=$\"$R9$\""
  !insertmacro ReplaceInFile $INSTDIR\etc\grid\windefault.xml "c:\\omero_dist" "$INSTDIR"
  !insertmacro ReplaceInFile $INSTDIR\etc\Windows.cfg "c:\omero_dist" "$INSTDIR"

  !insertmacro FinishAction "ActualInstall"

  ;
  ; Data directory
  ;
  CreateDirectory "$R6"
  StrCpy $CommandLine '"$COMSPEC" /C "createdb -U $R3 $R2"'
  ${Execute} $CommandLine "Failed to create database" ""
  ${If} ${Errors}
    Goto FatalError
  ${Else}
    ${LogText} "Created database $R2"
  ${EndIf}

  ;
  ; Create database
  ;
  StrCpy $CommandLine '"$COMSPEC" /C "createlang -U $R3 plpgsql $R2"'
  ${Execute} $CommandLine "Failed to add plpgsql" ""
  ${If} ${Errors}
    Goto FatalError
  ${Else}
    ${LogText} "Added plpgsql to $R2"
  ${EndIf}

  StrCpy $CommandLine '"python.exe" $INSTDIR\bin\omero db script ${DBVERSION} ${DBPATCH}'
  ${Execute} $CommandLine "Failed to create database script" "$R0"
  ${If} ${Errors}
    Goto FatalError
  ${Else}
    ${LogText} "Created database script: ${DBVERSION}__${DBPATCH}.sql"
  ${EndIf}

  StrCpy $CommandLine '"$COMSPEC" /C "psql -U $R3 $R2 < "$INSTDIR\${DBVERSION}__${DBPATCH}.sql"'
  ${Execute} $CommandLine "Failed to execute database script" "$R4"
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


/*
Function Splash
  # http://www.theresearchkitchen.com/blog/archives/date/2006/09
  ;File /oname=$PLUGINSDIR\splash.bmp "installer.bmp"
  ;splash::show 1000 $PLUGINSDIR\splash

  Pop $0 ; $0 has '1' if the user closed the splash screen early,
         ; '0' if everything closed normally, and '-1' if some error occurred.

FunctionEnd
*/
