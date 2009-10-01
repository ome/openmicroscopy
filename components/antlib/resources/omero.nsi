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
######################################################################

# TODOS--------------------------
#
#    - Create all important Start Menu Items
#    - Startmenu icons components/antlib/resources/icons.nsh
#    - SilentInstall
#    - Downloading Postgres, Ice, etc. from the Web (Python) and checking MD5
#    - Creating database
#    - postgres like up and down scripts from Startup menu OMERO_HOME+a copy of script
#    - LOGGING
#    - http://nsis.sourceforge.net/Simple_Java_Runtime_Download_Script
#
#    - uninstall
#      -- should only remove C:\Ice if we added it
#      -- Leave Ice installed
#      -- Have our own path
#      -- What about postgres
#      -- More careful about which registries entries are created
#
#   POSSIBILITES:
#    - PG installed/not
#    - Ice installed/not
#    - Java installed/not
#    - OMERO installed/not
#    - DB keep/don't keep


######################################################################
# DEFINITIONS
#
# Provided on command-line by build.xml's release-nsis target
#
#   - VERSION
#   - OMERO_VERSION
#   - INCLUDE_DIR
#   - INSTALLER_NAME
#
######################################################################

Name "${PRODUCT_NAME} ${PRODUCT_VERSION}"
OutFile "${INSTALLER_NAME}"
InstallDir "C:\OMERO-${OMERO_VERSION}"
ShowInstDetails show
ShowUnInstDetails show
XPStyle on

!define PRODUCT_NAME "OMERO.platform"
!define PRODUCT_VERSION "${VERSION}"
!define PRODUCT_PUBLISHER "The Open Microscopy Environment"
!define PRODUCT_WEB_SITE "http://www.openmicroscopy.org"
!define PRODUCT_INST_KEY "Software\${PRODUCT_NAME}"
!define PRODUCT_INST_ROOT_KEY "HKLM"
!define PRODUCT_UNINST_KEY "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}"
!define PRODUCT_UNINST_ROOT_KEY "HKLM"
!define PRODUCT_STARTMENU_REGVAL "OMERO:StartMenuDir"

!define MUI_ICON "${NSISDIR}\Contrib\Graphics\Icons\modern-install.ico"
!define MUI_UNICON "${NSISDIR}\Contrib\Graphics\Icons\modern-uninstall.ico"
!define MUI_STARTMENUPAGE_DEFAULTFOLDER "OMERO"
!define MUI_STARTMENUPAGE_REGISTRY_ROOT "${PRODUCT_UNINST_ROOT_KEY}"
!define MUI_STARTMENUPAGE_REGISTRY_KEY "${PRODUCT_UNINST_KEY}"
!define MUI_STARTMENUPAGE_REGISTRY_VALUENAME "${PRODUCT_STARTMENU_REGVAL}"
!define MUI_FINISHPAGE_SHOWREADME "$INSTDIR\README.txt"
!define MUI_FINISHPAGE_RUN "omero"
!define MUI_FINISHPAGE_RUN_PARAMETERS "admin start"
!define MUI_ABORTWARNING
!define MUI_STARTMENUPAGE_NODISABLE

######################################################################
# MUI
######################################################################

; MUI variables
var ICONS_GROUP

; MUI 1.67 compatible ------
!addincludedir "${INCLUDE_DIR}"
!include "omero.nsh"
!include "EnvVarUpdate.nsh"
!include "TextLog.nsh"
!include LogicLib.nsh
!include WinMessages.nsh
!include "MUI.nsh"

; INSTALLER -----------------
!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_LICENSE "LICENSE.txt"
Page custom PrereqsPage PrereqsLeave ""
Page custom ConfigPage ConfigPageLeave ""
!insertmacro MUI_PAGE_COMPONENTS
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_STARTMENU Application $ICONS_GROUP
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH

; UNINSTALLER -----------------
!insertmacro MUI_UNPAGE_WELCOME
!insertmacro MUI_UNPAGE_INSTFILES
!insertmacro MUI_LANGUAGE "English"

######################################################################
# SECTIONS
######################################################################

Section "OMERO.server" SEC01
  ${LogText} "Copying files"
  SetOverwrite off
  SetOutPath "$INSTDIR"
  File "README.txt"
  File /r "dist/etc"
  File /r "dist/bin"
  ;File /r "dist/lib"

  !insertmacro MUI_STARTMENU_WRITE_BEGIN Application
  SetShellVarContext All
  CreateDirectory "$SMPROGRAMS\$ICONS_GROUP"
  CreateShortCut "$SMPROGRAMS\$ICONS_GROUP\README.lnk" $INSTDIR\README.txt
  CreateDirectory "$SMPROGRAMS\$ICONS_GROUP\configuration"
  CreateDirectory "$SMPROGRAMS\$ICONS_GROUP\logs"
  CreateShortCut "$SMPROGRAMS\$ICONS_GROUP\All Logs.lnk" "$INSTDIR\var\logs"
  !insertmacro MUI_STARTMENU_WRITE_END

  # REGISTRY
  WriteRegStr ${PRODUCT_INST_ROOT_KEY} "${PRODUCT_INST_KEY}" "InstallDir" "$INSTDDIR"
SectionEnd

Section -AdditionalIcons
  !insertmacro MUI_STARTMENU_WRITE_BEGIN Application
  SetShellVarContext All
  WriteIniStr "$INSTDIR\${PRODUCT_NAME}.url" "InternetShortcut" "URL" "${PRODUCT_WEB_SITE}"
  CreateDirectory "$SMPROGRAMS\$ICONS_GROUP"
  CreateShortCut "$SMPROGRAMS\$ICONS_GROUP\Website.lnk" "$INSTDIR\${PRODUCT_NAME}.url"
  CreateShortCut "$SMPROGRAMS\$ICONS_GROUP\Uninstall.lnk" "$INSTDIR\uninst.exe"
  !insertmacro MUI_STARTMENU_WRITE_END
SectionEnd

Section -Post
  WriteUninstaller "$INSTDIR\uninst.exe"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "DisplayName" "$(^Name)"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "UninstallString" "$INSTDIR\uninst.exe"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "DisplayVersion" "${PRODUCT_VERSION}"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "URLInfoAbout" "${PRODUCT_WEB_SITE}"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "Publisher" "${PRODUCT_PUBLISHER}"
SectionEnd

!insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
  !insertmacro MUI_DESCRIPTION_TEXT ${SEC01} ""
  !insertmacro MUI_DESCRIPTION_TEXT ${SEC02} ""
  !insertmacro MUI_DESCRIPTION_TEXT ${SEC03} ""
!insertmacro MUI_FUNCTION_DESCRIPTION_END

Section Uninstall
  SetAutoClose true
  !insertmacro MUI_STARTMENU_GETFOLDER "Application" $ICONS_GROUP
  Delete "$INSTDIR\${PRODUCT_NAME}.url"
  Delete "$INSTDIR\uninst.exe"
  Delete "$INSTDIR\README.txt"
  Delete "$INSTDIR\bin"
  Delete "$INSTDIR\etc"

  Delete "$SMPROGRAMS\$ICONS_GROUP\Uninstall.lnk"
  Delete "$SMPROGRAMS\$ICONS_GROUP\Website.lnk"

  RMDir "$SMPROGRAMS\$ICONS_GROUP"
  RMDir "$INSTDIR"

  ReadRegStr
  DeleteRegStr ${PRODUCT_INST_ROOT_KEY} "${PRODUCT_INST_KEY}"
  ${un.EnvVarUpdate} $0 "PATH" "R" "HKLM" "C:\Ice-3.3.1\bin"
  ${un.EnvVarUpdate} $0 "PYTHONPATH" "R" "HKLM" "C:\Ice-3.3.1\bin"

  # REGISTRY
  DeleteRegKey ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}"
  DeleteRegStr ${PRODUCT_INST_ROOT_KEY} "${PRODUCT_INST_KEY}"

SectionEnd

######################################################################
# FUNCTIONS
######################################################################

#!include "RelGotoPage.nsh"
!include nsDialogs.nsh
!insertmacro NSD_FUNCTION_INIFILE

Function .onInit
   ${LogSetFileName} "$INSTDIR\InstallLog.txt"
   ${LogSetOn}
   ${LogText} "In .onInit"

   System::Call 'kernel32::CreateMutexA(i 0, i 0, t "OMERO_INSTALLER_MUTEX") i .r1 ?e'
   Pop $R0

   StrCmp $R0 0 +3
   MessageBox MB_OK|MB_ICONEXCLAMATION "The installer is already running."
   Abort
FunctionEnd

Function .onVerifyInstDir
   IfFileExists $INSTDIR\var PathNotEmpty
       Goto PathEmpty
   PathNotEmpty:
       Abort ; Directory in use
   PathEmpty:
FunctionEnd

Function un.onInit
  MessageBox MB_ICONQUESTION|MB_YESNO|MB_DEFBUTTON2 "Are you sure you want to completely remove $(^Name) and all of its components?" IDYES +2
  Abort
FunctionEnd

Function un.onUninstSuccess
  HideWindow
  MessageBox MB_ICONINFORMATION|MB_OK "$(^Name) was successfully removed from your computer."
FunctionEnd

Function PrereqsPage
  InitPluginsDir
  !insertmacro MUI_HEADER_TEXT $(PREREQ_PAGE_TITLE) $(PREREQ_PAGE_SUBTITLE)
  File "/oname=$PLUGINSDIR\reqs.ini" "${INCLUDE_DIR}/reqs.ini"
  StrCpy $0 "$PLUGINSDIR\reqs.ini"
  Call CreateDialogFromINI
  Call UpdateINIState
FunctionEnd

Function PrereqsLeave
  # Get control window handle.
  ReadINIStr $R0 "$PLUGINSDIR\reqs.ini" "Field 1" "HWND"
  # Check if text has been entered in field 1.
  ReadINIStr $R1 "$PLUGINSDIR\reqs.ini" "Field 1" "State"
  ExecShell open $PLUGINSDIR\reqs.ini
  # Make field background red!
  /*
  ${If} $R1 == ""
    SetCtlColors $R0 0x000000 0xFF0000
    Abort # Go back to page.
  # Reset field colours.
  ${Else}
    SetCtlColors $R1 0x000000 0xFFFFFF
  ${EndIf}
  */

  /*
  ReadINIStr $0 "$PLUGINSDIR\reqs.ini" "Field 1" "State"
  ${If} ${Cmd} `MessageBox MB_ICONQUESTION|MB_YESNO|MB_DEFBUTTON2 "Use Postgres directory $0?" IDYES`
    WriteINIStr $PLUGINSDIR\reqs.ini Settings RTL 1
    ExecShell open $PLUGINSDIR\reqs.ini
  ${EndIf}
  */

  /*
  IFErrors 0 NoError
    Abort "Ice msi download failed"
    Goto EndIt
  NoError:
    # http://pginstaller.projects.postgresql.org/silent.html
    ExecWait 'msiexec /i postgresql-8.0.0-rc1-int.msi  /qr INTERNALLAUNCH=1 ADDLOCAL=server,psql,docs SERVICEDOMAIN="%COMPUTERNAME%"
          SERVICEPASSWORD="SecretWindowsPassword123" SUPERPASSWORD="VerySecret" BASEDIR="c:\postgres" TRANSFORMS=:lang_de'
  EndIt:
  */

  /*
    ExecWait 'python.exe" -h' $0
    DetailPrint "some program returned $0"
    CopyFiles $INSTDIR\*.dat $INSTDIR\DATA
    ; http://nsis.sourceforge.net/Embedding_other_installers
    SetOutPath $INSTDIR\Prerequisites
    IfFileExists "C:\Ice-3.3.1\bin\icegridnode.exe" endNtBackup beginNtBackup
    Goto endNtBackup
    beginNtBackup:
    MessageBox MB_OK "Ice-3.3.1 not found$\n$\nPress OK to install it."
    File "..\Prerequisites\Ice-3.3.1.msi"
    ExecWait '"msiexec" /i "$INSTDIR\Prerequisites\Ice-3.3.1.msi"'
    endNtBackup:
  */
/*
    SetOutPath $INSTDIR\Prerequisites
    IfFileExists "C:\Ice-3.3.1\bin\icegridnode.exe" endNtBackup beginNtBackup
    Goto endNtBackup
    beginNtBackup:
    MessageBox MB_OK "Ice-3.3.1 not found$\n$\nPress OK to install it."

    ClearErrors
    ExecWait 'python "$INSTDIR\bin\omero setup ice-msi'
    ${LogText} "Downloaded Ice"
    IFErrors 0 NoError
        Abort "Ice msi download failed"
        Goto EndIt
    NoError:
        ExecWait '"msiexec" /i "$INSTDIR\lib\Ice-3.3.1-VC80.msi"'
    EndIt:
    endNtBackup:
  */


  ${EnvVarUpdate} $0 "PATH" "A" "HKLM" "C:\Ice-3.3.1\bin"
  ${EnvVarUpdate} $0 "PYTHONPATH" "A" "HKLM" "C:\Ice-3.3.1\python"

FunctionEnd

Function ConfigPage
  InitPluginsDir
  !insertmacro MUI_HEADER_TEXT $(CONFIG_PAGE_TITLE) $(CONFIG_PAGE_SUBTITLE)
  File "/oname=$PLUGINSDIR/cfg.ini" "${INCLUDE_DIR}/cfg.ini"
  StrCpy $0 "$PLUGINSDIR/cfg.ini"
  Call CreateDialogFromINI
  Call UpdateINIState
FunctionEnd

Function ConfigPageLeave
  CreateDirectory $INSTDIR\DATA
FunctionEnd
