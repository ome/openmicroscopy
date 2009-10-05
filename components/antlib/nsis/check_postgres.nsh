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

!define PG_ZIP "postgresql-8.3.7-1.zip"
!ifndef PG_URL
  !define PG_URL "http://wwwmaster.postgresql.org/redir/198/h/binary/v8.3.7/win32/postgresql-8.3.7-1.zip"
!endif
!ifndef PG_MD5
  !define PG_MD5 "ec22457ddcadc0924bd891550dacb67b"
!endif

!define PG_KEY "SOFTWARE\PostgreSQL\Installations"

!macro CheckPostgreSQL

  !insertmacro StartAction "CheckPG"
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
    ${ConfirmInstall} PostgreSQL
    Call GetPg
  ${Else}
    ${If} $R1 != ""
      ${LogText} "Found multiple Postgres instances. Choosing first"
    ${EndIf}
    WriteRegStr ${PRODUCT_INST_ROOT_KEY} "${PRODUCT_INST_KEY}" "PgPath" "$R0"
    WriteINIStr "$INSINI" "PostgreSQL" "State" "$R0"
  ${EndIf}

  PGReady: ; ---------------------------------------------
  !insertmacro FinishAction "CheckPG"

!macroend

;
; Usage:
;  Call IsPgInstalled
;   Pop $0 ; First instance, "GLOBAL", or ""
;   Pop $1 ; Second instance, "GLOBAL", or ""
;   Pop $2 ; Third instance, "GLOBAL", or ""
;
; Based on:
;  http://nsis.sourceforge.net/EnumUsersReg

Function IsPgInstalled

  StrCpy $CommandLine 'psql --version'
  ${Execute} $CommandLine "PostgreSQL is not installed" "" 0
  ${If} ${Errors}
  ${OrIf} $ExitCode == 1
    ClearErrors
  ${Else}
    push "GLOBAL"
    push "GLOBAL"
    push "GLOBAL"
    return
  ${EndIf}

  ${LogText} "Looking for PG under ${PG_KEY}"
  StrCpy $R0 0
  ${While} $R0 < 3
    EnumRegKey $R1 HKLM "${PG_KEY}" $R0
    IntOp $R0 $R0 + 1
    ${If} $R1 != ""
      ReadRegStr $R2 HKLM "${PG_KEY}\$R1" "Base Directory"
      ${LogText} "Found PG: $R1 = $R2"
      push "$R2bin"
    ${Else}
      push ""
    ${EndIf}
  ${EndWhile}

FunctionEnd

Function GetPg
  StrCpy $R0 "$INSDIR\${PG_ZIP}"
  StrCpy $R4 "$INSDIR\postgres"

  ClearErrors
  StrCpy $R0 "$INSDIR\${PG_INSTALLER}"
  ${Download} "${PG_URL}" "$R0" "${PG_MD5}"
  ${IfNot} ${Errors}
    IfFileExists "$R4\setup.bat" Installing Unzipping
    Unzipping:
      CreateDirectory "$R4"
      nsisunz::Unzip "$R0" "$R4"
      Pop $0
      StrCmp $0 "success" Installing
        ${LogText} "Unzip failed: $0"
      ${LogText} "Unzipping of PostgreSQL installer returned $0"
    Installing:
      ; Copied from setup.bat
      StrCpy $R2 '"$R4\vcredist_x86.exe"'
      ${Execute} $R2 "vcredist_x86 failed" "" 0
      IfErrors Failure 0
      StrCpy $R2 '"msiexec.exe" /i "$R4\postgresql-8.3.msi"'
      ${Execute} $R2 "PostgreSQL MSI installer failed" "" 0
      # http://pginstaller.projects.postgresql.org/silent.html
      # ExecWait 'msiexec /i postgresql-8.0.0-rc1-int.msi  /qr INTERNALLAUNCH=1 ADDLOCAL=server,psql,docs SERVICEDOMAIN="%COMPUTERNAME%"
      #      SERVICEPASSWORD="SecretWindowsPassword123" SUPERPASSWORD="VerySecret" BASEDIR="c:\postgres" TRANSFORMS=:lang_de'
  ${EndIf}

  Failure:
    SetErrors
    Push "Failed to install PostgreSQL"

FunctionEnd

