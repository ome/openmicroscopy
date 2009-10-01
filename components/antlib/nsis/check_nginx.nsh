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

!define NGINX_INSTALLER "nginx-0.7.62.zip"
!ifndef NGINX_URL
  !define NGINX_URL "http://sysoev.ru/nginx/nginx-0.7.62.zip"
!endif
!ifndef NGINX_MD5
  !define NGINX_MD5 "4395ff2204477c416b059504467d3f7b"
!endif

!macro CheckNginx

  NginxReady: ; ---------------------------------------------

!macroend
