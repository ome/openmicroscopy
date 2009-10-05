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

!define SZIP_INSTALLER "szip21-vnet-enc.zip"
!ifndef SZIP_URL
  !define SZIP_URL "http://www.hdfgroup.org/ftp/lib-external/szip/2.1/bin/windows/szip21-vnet-enc.zip"
!endif
!ifndef SZIP_MD5
  !define SZIP_MD5 "b18d044f6e259c561935db2d4e0e3ad0"
!endif

!define ZLIB_INSTALLER "zlib123-vnet.zip"
!ifndef ZLIB_URL
  !define ZLIB_URL "http://www.hdfgroup.org/ftp/lib-external/zlib/1.2/bin/windows/zlib123-vnet.zip"
!endif
!ifndef ZLIB_MD5
  !define ZLIB_MD5 "1030239f01683bbc97e90e82a8f4685e"
!endif

!define HDF_INSTALLER "hdf5_183_xp32_vs2008_ivf101.zip"
!ifndef HDF_URL
  !define HDF_URL "http://www.hdfgroup.org/ftp/HDF5/current/bin/windows/hdf5_183_xp32_vs2008_ivf101.zip"
!endif
!ifndef HDF_MD5
  !define HDF_MD5 "b4c32cc622358ca319d417ee824221b9"
!endif

!define PYTABLES_INSTALLER "tables-2.1.2.win32-py2.5.exe"
!ifndef PYTABLES_URL
  !define PYTABLES_URL "http://www.pytables.org/download/stable/tables-2.1.2.win32-py2.5.exe"
!endif
!ifndef PYTABLES_MD5
  !define PYTABLES_MD5 "e300566559965eedb68ea1de66c9ff9e"
!endif

!define SCIPY_INSTALLER "scipy-0.7.1-win32-superpack-python2.5.exe"
!ifndef SCIPY_URL
  !define SCIPY_URL "http://users.openmicroscopy.org.uk/~jmoore/nsis/${SCIPY_INSTALLER}"
!endif
!ifndef SCIPY_MD5
  !define SCIPY_MD5 "324248e01f235a301424ac30658b3355"
!endif

!define NUMPY_INSTALLER "numpy-1.3.0-win32-superpack-python2.5.exe"
!ifndef NUMPY_URL
  !define NUMPY_URL "http://users.openmicroscopy.org.uk/~jmoore/nsis/${NUMPY_INSTALLER}"
!endif
!ifndef NUMPY_MD5
  !define NUMPY_MD5 "e8d2b1f0d30416ee72bc29e3b6762fef"
!endif

!macro CheckTables

  !insertmacro StartAction "CheckTables"
  ;-----------------------------------------------------------
  Push "import scipy; scipy.show_config()"
  Call IsModuleInstalled
  Pop $R0
  ${LogText} "scipy value is $R0"
  ${If} $R0 == "${UNNEEDED}"
    WriteINIStr "$INSINI" "scipy" "State" "${UNNEEDED}"
    Goto SciPyReady
  ${Else}
    ${ConfirmInstall} "scipy"
    Call GetSciPy
    ${IfNot} ${Errors}
      ${LogText} "scipy installed in python"
      WriteINIStr "$INSINI" "scipy" "State" "${UNNEEDED}"
    ${EndIf}
  ${EndIf}
  SciPyReady:

  Push "import numpy; numpy.show_config()"
  Call IsModuleInstalled
  Pop $R0
  ${LogText} "numpy value is $R0"
  ${If} $R0 == "${UNNEEDED}"
    WriteINIStr "$INSINI" "numpy" "State" "${UNNEEDED}"
    Goto NumPyReady
  ${Else}
    ${ConfirmInstall} "numpy"
    Call GetNumPy
    ${IfNot} ${Errors}
      ${LogText} "numpy installed in python"
      WriteINIStr "$INSINI" "numpy" "State" "${UNNEEDED}"
    ${EndIf}
  ${EndIf}
  NumPyReady:

  Push "import tables; tables.print_versions()"
  Call IsModuleInstalled
  Pop $R0
  ${LogText} "tables value is $R0"
  ${If} $R0 == "${UNNEEDED}"
    WriteINIStr "$INSINI" "Tables" "State" "${UNNEEDED}"
    Goto TablesReady
  ${Else}
    ${ConfirmInstall} "Tables"
    Call GetTables
    ${IfNot} ${Errors}
      ${LogText} "tables installed in python"
      WriteINIStr "$INSINI" "Tables" "State" "${UNNEEDED}"
    ${EndIf}
  ${EndIf}
  TablesReady:
  ; ---------------------------------------------
  !insertmacro FinishAction "CheckTables"

!macroend

;
; Usage:
;  Push "import something; something.test_me()"
;  Call IsModuleInstalled
;   Pop $0 ; "${UNNEEDED}" or ""
;
Function IsModuleInstalled

  Pop $0 ; Command
  StrCpy $CommandLine 'python -c "$0"'
  ${Execute} $CommandLine "$0 failed" "" 0
  ${If} ${Errors}
  ${OrIf} $ExitCode == 1
    push ""
    ClearErrors
  ${Else}
    push "${UNNEEDED}"
    return
  ${EndIf}
FunctionEnd

Function GetSciPy
  ClearErrors
  StrCpy $R1 "$INSDIR\${SCIPY_INSTALLER}"
  ${DownloadAndRun} "${SCIPY_URL}" "$R1" "${SCIPY_MD5}" "SciPy installer failed."
FunctionEnd

Function GetNumPy
  ClearErrors
  StrCpy $R1 "$INSDIR\${NUMPY_INSTALLER}"
  ${DownloadAndrun} "${NUMPY_URL}" "$R1" "${NUMPY_MD5}" "NumPy installer failed."
FunctionEnd

Function GetTables
  ClearErrors
  StrCpy $R1 "$INSDIR\${PYTABLES_INSTALLER}"
  ${DownloadAndRun} "${PYTABLES_URL}" "$R1" "${PYTABLES_MD5}" "PyTables installer failed"
FunctionEnd

