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

!define PY_KEY "SOFTWARE\Python\PythonCore"

!macro CheckPython

  !insertmacro StartAction "CheckPython"
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
    ${ConfirmInstall} Python
    Call GetPython
  ${Else}
    ${If} $R1 != ""
      ${LogText} "Found multiple Python instances. Choosing first"
    ${EndIf}
    WriteRegStr ${PRODUCT_INST_ROOT_KEY} "${PRODUCT_INST_KEY}" "PythonPath" "$R0"
    WriteINIStr "$INSINI" "Python" "State" "$R0"
  ${EndIf}

  PythonReady: ; ---------------------------------------------
  !insertmacro FinishAction "CheckPython"

!macroend

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
