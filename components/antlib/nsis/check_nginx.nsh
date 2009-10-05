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

!define NGINX_INSTALLER "nginx-0.7.62"
!ifndef NGINX_URL
  !define NGINX_URL "http://sysoev.ru/nginx/${NGINX_INSTALLER}.zip"
!endif
!ifndef NGINX_MD5
  !define NGINX_MD5 "4395ff2204477c416b059504467d3f7b"
!endif

!macro CheckNginx

  !insertmacro StartAction "CheckNginx"
  ; ---------------------------------------------
  Call IsNginxInstalled
  Pop $R0
  ${LogText} "nginx value is $R0"
  ${If} $R0 == "${UNNEEDED}"
    WriteINIStr "$INSINI" "Nginx" "State" "${UNNEEDED}"
    Goto NginxReady
  ${EndIf}
  Call GetNginx
  ${IfNot} ${Errors}
    ${LogText} "Nginx installed"
    WriteINIStr "$INSINI" "Nginx" "State" "${UNNEEDED}"
  ${EndIf}
  NginxReady:
  ; ---------------------------------------------
  !insertmacro FinishAction "CheckNginx"

!macroend

;
; Usage:
;  Call IsNginxInstalled
;   Pop $0 ; "${UNNEEDED}" or ""
;
Function IsNginxInstalled

  StrCpy $CommandLine 'nginx -v'
  ${Execute} $CommandLine "nginx failed" "" 0
  ${If} ${Errors}
    push ""
    ClearErrors
  ${Else}
    push "${UNNEEDED}"
  ${EndIf}
FunctionEnd

Function GetNginx
  ClearErrors
  StrCpy $R1 "$INSDIR\${NGINX_INSTALLER}.zip"
  ${Download} "${NGINX_URL}" "$R1" "${NGINX_MD5}"
  ${IfNot} ${Errors}
    IfFileExists "C:\\${NGINX_INSTALLER}\\nginx.exe" Installing Unzipping
    Unzipping:
      nsisunz::Unzip "$R1" "C:\\"
      Pop $0
      StrCmp $0 "success" Installing
        ${LogText} "Unzip failed: $0"
      ${LogText} "Unzipping of ${NGINX_INSTALLER} returned $0"
    Installing:
      ${LogText} "Updating PATH and PYTHONPATH with nginx"
      ${EnvVarUpdate} $1 "PATH" "A" "HKLM" "C:\\${NGINX_INSTALLER}\\"
  ${EndIf}
FunctionEnd
