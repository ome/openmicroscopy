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

!define SCIPY_INSTALLER "scipy-0.7.1-win32-superpack-python2.5.exe"
!ifndef SCIPY_URL
  !define SCIPY_URL "http://sourceforge.net/projects/scipy/files/scipy/0.7.1/scipy-0.7.1-win32-superpack-python2.5.exe/download"
!endif
!ifndef SCIPY_MD5
  !define SCIPY_MD5 "30e13ab0b58e47cd31e8a31fda49bf7b"
!endif

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

!define NUMPY_INSTALLER "numpy-1.3.0-win32-superpack-python2.5.exe"
!ifndef NUMPY_URL
  !define NUMPY_URL "http://sourceforge.net/projects/numpy/files/NumPy/1.3.0/numpy-1.3.0-win32-superpack-python2.5.exe/download"
!endif
!ifndef NUMPY_MD5
  !define NUMPY_MD5 "e257df93546dfe1f41aabf33fde2d862"
!endif

!macro CheckTables

  Call IsTablesInstalled
  Pop $R0 ; First
  WriteINIStr "$INSINI" Tables Value  "$R0"

  ; If GLOBAL, installed into python
  ${If} $R0 == "GLOBAL"
    ${LogText} "PyTables value is GLOBAL"
    WriteINIStr "$INSINI" "Tables" "State" "${UNNEEDED}"
    Goto TablesReady
  ${EndIf}

  ${If} $R0 == "" ; None
    ${LogText} "PyTables not found"
    ${ConfirmInstall} PyTables
    Call GetTables
  ${EndIf}

  TablesReady: ; ---------------------------------------------

!macroend

;
; Usage:
;  Call IsTablesInstalled
;   Pop $0 ; "GLOBAL" or ""
;
Function IsTablesInstalled

  StrCpy $CommandLine 'python -mtables'
  ${Execute} $CommandLine "PyTables is not installed" "" 0
  ${If} ${Errors}
  ${OrIf} $ExitCode == 1
    push ""
    ClearErrors
  ${Else}
    push "GLOBAL"
    return
  ${EndIf}
FunctionEnd

Function GetTables
  ClearErrors

  StrCpy $R1 "$INSDIR\${SCIPY_INSTALLER}"
  ${DownloadAndRun} "${SCIPY_URL}" "$R1" "${SCIPY_MD5}" "SciPy installer failed."

  StrCpy $R1 "$INSDIR\${NUMPY_INSTALLER}"
  ${DownloadAndrun} "${NUMPY_URL}" "$R1" "${NUMPY_MD5}" "NumPy installer failed."

  StrCpy $R1 "$INSDIR\${PYTABLES_INSTALLER}"
  ${DownloadAndRun} "${PYTABLES_URL}" "$R1" "${PYTABLES_MD5}" "PyTables installer failed"

FunctionEnd

