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
#  - ${EnvVarUpdate} $1 "PYTHONPATH" "A" "HKLM" "C:\Ice-3.3.1\python"
#  - Check etc with http://nsis.sourceforge.net/TextCompare (TextCompareS)
#   -- Or use: http://sourceforge.net/projects/nsispatchgen
#  - Check if any of our INCLUDE_DIR functions are in the distro
#  - On failure, open InstallLog or send it to the feedback
#  - Using only zips (with no compression) in bundle
#  - DE/REACTIVATE
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
Var PREREQ
Var COMSPEC
Var ICONS_GROUP
; No constant
Var CommandLine
Var ExitCode
Var Message
; GUI elements
Var hnDialog
Var hnLabel
Var hnText

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
Page custom JavaPage JavaLeave ""
Page custom PostgreSQLPage PostgreSQLLeave ""
Page custom PythonPage PythonLeave ""
Page custom IcePage IceLeave ""
Page custom Config1Page Config1Leave ""
Page custom Config2Page Config2Leave ""
Page custom ActualInstall "" ""
!insertmacro MUI_PAGE_FINISH

; UNINSTALLER -----------------
!insertmacro MUI_UNPAGE_WELCOME
!insertmacro MUI_UNPAGE_INSTFILES

; Other
!insertmacro MUI_LANGUAGE "English"

######################################################################
# SECTIONS
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

  !insertmacro MUI_STARTMENU_WRITE_BEGIN Application
  SetShellVarContext All
  CreateDirectory "$SMPROGRAMS\$ICONS_GROUP"
  CreateDirectory "$SMPROGRAMS\$ICONS_GROUP\logs"
  CreateShortCut "$SMPROGRAMS\$ICONS_GROUP\logs\OMEROweb.log.lnk" "$INSTDIR\var\logs\OMEROweb.log"
  !insertmacro MUI_STARTMENU_WRITE_END

  !insertmacro FinishAction "Web"

SectionEnd

Section "New OMERO database" SECDB

  !insertmacro StartAction "Db"
  !insertmacro FinishAction "Db"

SectionEnd

Section "New binary repository" SECDATA

  !insertmacro StartAction "Data"
  !insertmacro FinishAction "Data"

SectionEnd

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

Section -Post

  !insertmacro StartAction "Post"

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

  !insertmacro FinishAction "Post"

SectionEnd

!insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
  !insertmacro MUI_DESCRIPTION_TEXT ${SECSRV} "SECSRV_DESC"
  !insertmacro MUI_DESCRIPTION_TEXT ${SECWEB} "SECWEB_DESC"
  !insertmacro MUI_DESCRIPTION_TEXT ${SECDB} "SECDB_DESC"
  !insertmacro MUI_DESCRIPTION_TEXT ${SECDATA} "SECDATA_DESC"
!insertmacro MUI_FUNCTION_DESCRIPTION_END

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

!insertmacro NSD_FUNCTION_INIFILE

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
  DumpLog::DumpLog "$INSDIR\log.txt" .R0
FunctionEnd

Function .onInstFailed
  ${LogText} "onInstFailed"
  DumpLog::DumpLog "$INSDIR\log.txt" .R0
FunctionEnd

Function .onPrevPage
  ${LogText} "onPrevPage"
  MessageBox MB_OK "Actions already performed. Please cancel and use the uninstaller"
  Abort
FunctionEnd

Function un.onInit
  MessageBox MB_ICONQUESTION|MB_YESNO|MB_DEFBUTTON2 "Are you sure you want to completely remove $(^Name) and all of its components?" IDYES +2
  Abort
FunctionEnd

Function un.onUninstSuccess
  MessageBox MB_ICONINFORMATION|MB_OK "$(^Name) was successfully removed from your computer."
FunctionEnd

######################################################################
# POST-COPY FUNCTIONS
######################################################################

!define UNNEEDED "Found on PATH. No input needed"

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
!macro PrereqStart Prereq
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

Function ${Prereq}Check

  ClearErrors
  ReadINIStr $R1 "$INSINI" "${Prereq}" "State"
  ${LogText} "Checking ${Prereq}. State=$R1"

  ${If} $R1 == ""
    StrCpy $Message "${Prereq} is empty"
    ${LogText} "$Message"
    SetErrors
  ${EndIf}

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

FunctionEnd

Function ${Prereq}Page

  !insertmacro StartAction "${Prereq}Page"
  !insertmacro MUI_HEADER_TEXT $(PREREQ_PAGE_TITLE) $(PREREQ_PAGE_SUBTITLE)
  StrCpy $PREREQ "${Prereq}"

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
!macroend
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; Your code goes here

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
!macro PrereqEnd Prereq
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

   nsDialogs::Create 1018
   Pop $hnDialog

   ${If} $hnDialog == error
     MessageBox MB_OK "Fatal error creating requirements dialog"
     ${LogText} "Fatal error creating requirements dialog"
     Quit
   ${EndIf}

   ClearErrors
   Call ${Prereq}Check
   ${If} ${Errors}
     nsDialogs::SelectFolderDialog "Manually choose a directory for ${Prereq}"
   ${Else}
   ${EndIf}

   ${NSD_CreateLabel} 0 0 100% 35u "These are the installation directories found by the installer. If they are incorrect or empty, point to the installation if you know it. If not, you will be asked to install the package."
   Pop $hnLabel

   ${NSD_CreateLabel} 20 46 38u 35u "${PREREQ}"
   Pop $hnLabel

   ${NSD_CreateDirRequest} 62 46 180u 35u ""
   Pop $hnText

   nsDialogs::Show

  !insertmacro FinishAction "${Prereq}Page"

FunctionEnd

Function ${Prereq}Leave

  !insertmacro StartAction "${Prereq}Leave"

  StrCpy $PREREQ "${Prereq}"
  Call ${Prereq}Check
  IfErrors 0 Success
    MessageBox MB_OK "$Message"
    Abort
  Success:
  ${LogText} "Did not abort for ${Prereq}"

  !insertmacro FinishAction "${Prereqs}Leave"

FunctionEnd

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
!macroend
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


!insertmacro PrereqStart "PostgreSQL"

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
    Call GetPg
  ${Else}
    ${If} $R1 != ""
      ${LogText} "Found multiple Postgres instances. Choosing first"
    ${EndIf}
    WriteRegStr ${PRODUCT_INST_ROOT_KEY} "${PRODUCT_INST_KEY}" "PgPath" "$R0"
    WriteINIStr "$INSINI" "PostgreSQL" "State" "$R0"
  ${EndIf}

  PGReady: ; ---------------------------------------------

!insertmacro PrereqEnd "PostgreSQL"

!insertmacro PrereqStart "Ice"

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

!insertmacro PrereqEnd "Ice"

!insertmacro PrereqStart "Python"

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
    Call GetPython
  ${Else}
    ${If} $R1 != ""
      ${LogText} "Found multiple Python instances. Choosing first"
    ${EndIf}
    WriteRegStr ${PRODUCT_INST_ROOT_KEY} "${PRODUCT_INST_KEY}" "PythonPath" "$R0"
    WriteINIStr "$INSINI" "Python" "State" "$R0"
  ${EndIf}

  PythonReady: ; ---------------------------------------------

!insertmacro PrereqEnd "Python"

!insertmacro PrereqStart "Java"

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

!insertmacro PrereqEnd "Java"

Function Config1Page

  !insertmacro StartAction "Config1Page"

  !insertmacro MUI_HEADER_TEXT $(CONFIG_PAGE_TITLE) $(CONFIG_PAGE_SUBTITLE)

  ${NSD_CreateLabel} 0 0 100% 35u "Database configuration" "The default properties below are acceptable for default installations."
  Pop $hnLabel

  ${NSD_CreateLabel} 20 46 38u 35u "Database name"
  Pop $1

  ${NSD_CreateDirRequest} 62 46 180u 35u "omero"
  Pop $2

  nsDialogs::Show

  !insertmacro FinishAction "Config1Page"

FunctionEnd

!macro GetCfgState

  ReadINIStr $R0 "$INSINI" "Config" "ROOTPASS"
  ReadINIStr $R1 "$INSINI" "Config" "ROOTCONF"
  ReadINIStr $R2 "$INSINI" "Config" "DBNAME"
  ReadINIStr $R3 "$INSINI" "Config" "DBUSER"
  ReadINIStr $R4 "$INSINI" "Config" "DBPASS"
  ReadINIStr $R5 "$INSINI" "Config" "DBCONF"
  ReadINIStr $R6 "$INSINI" "Config" "DATADIR"
  ReadINIStr $R8 "$INSINI" "Config" "WEBPORT"
  ReadINIStr $R9 "$INSINI" "Config" "ROUTERPORT"

!macroend

!macro NonEmpty State Config
  StrLen $1 "${State}"
  ${If} $1 == 0
    ${LogText} "${XXXState} is empty"
    MessageBox MB_OK "${Field} is required"
    Abort
  ${Else}
    ${LogText} "NonEmpty check: len(${Field}) = $1"
  ${EndIf}
!macroend

Function Config1Leave

  !insertmacro StartAction "Config1Leave"

  ExpandEnvStrings $COMSPEC %COMSPEC%
  Call UpdateINIState
  !insertmacro GetCfgState

  ;
  ; Validate non empty
  ;
  !insertmacro NonEmpty "$R2" "Name of database"
  !insertmacro NonEmpty "$R3" "Database user name"
  !insertmacro NonEmpty "$R6" "Data directory"
  !insertmacro NonEmpty "$R9" "Router port"
  !insertmacro NonEmpty "$R0" "Password for OMERO's root user"

  ;
  ; Confirm passwords equals
  ;
  ${If} $R0 != $R1
    MessageBox MB_OK "Passwords for OMERO's root user don't match"
    Abort
  ${EndIf}

  ${If} $R4 != $R5
    MessageBox MB_OK "Passwords for database user don't match"
    Abort
  ${EndIf}

  ;
  ; Validate database settings
  ;

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

  ; Create the database
  ; This is the standard invocation as in all the docs.

  StrCpy $CommandLine '"$COMSPEC" /C "psql -U $R3 -c \l"'
  ${Execute} $CommandLine "Failed to connect to postgres" ""
  ${If} ${Errors}
    MessageBox MB_OK "Cannot connect to postgres"
    Abort
  ${Else}
    ${LogText} "Connected to postgres as $R3"
  ${EndIf}

  StrCpy $CommandLine '"$COMSPEC" /C "psql -U $R3 $R2" < $PLUGINSDIR\now.sql'
  ${Execute} $CommandLine "Database already exists $R2" ""
  ${IfNot} ${Errors}
    MessageBox MB_OK "Database $R2 already exists. Please enter a new one"
    Abort
  ${Else}
    ${LogText} "Database $R2 doesn't exit. Good."
  ${EndIf}

  ;
  ; Validate data directory
  ;
  IfFileExists "$R6\*.*" 0 Exists
    ${LogText} "$R6 (Field 6) exists"
    MessageBox MB_OK "Data directory already in use"
  Exists:


  !insertmacro FinishAction "Config1Leave"
  Return

  ;
  ; Error handling
  ;
  FatalError:
    MessageBox MB_OK "Aborting install..."
    Quit

FunctionEnd

Function Config2Page
  !insertmacro StartAction "Config2Page"
  !insertmacro FinishAction "Config2Page"
FunctionEnd
Function Config2Leave
  !insertmacro StartAction "Config2Leave"
  !insertmacro FinishAction "Config2Leave"
FunctionEnd

Function ActualInstall

  !insertmacro StartAction "ActualInstall"

  Abort "actual install"

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
