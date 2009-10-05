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

!define PIL_INSTALLER "PIL-1.1.6.win32-py2.5.exe"
!ifndef PIL_URL
  !define PIL_URL "http://effbot.org/downloads/PIL-1.1.6.win32-py2.5.exe"
!endif
!ifndef PIL_MD5
  !define PIL_MD5 "e1347e7055f42f3e50c20e5935a4b62e"
!endif

!macro CheckPIL

  !insertmacro StartAction "CheckPIL"
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
    ${ConfirmInstall} PIL
    Call GetPIL
  ${EndIf}

  PILReady: ; ---------------------------------------------
  !insertmacro FinishAction "CheckPIL"

!macroend

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

