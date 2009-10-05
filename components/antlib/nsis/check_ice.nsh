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

!define ZEROC_VS2005_KEY "Software\ZeroC\Ice 3.3.1 for Visual Studio 2005"
!define ZEROC_VS2008_KEY "Software\ZeroC\Ice 3.3.1 for Visual Studio 2008"

!macro CheckIce

  !insertmacro StartAction "CheckIce"

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
      ${ConfirmInstall} Ice
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
  !insertmacro FinishAction "CheckIce"

!macroend

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

