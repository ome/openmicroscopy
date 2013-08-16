#!/bin/bash

set -e -u -x

NGINX_CONF=/etc/nginx/sites-available/omero-web
$OMERO_PREFIX/bin/omero web config nginx --system --http "$OMERO_WEB_PORT" > nginx.tmp
sudo cp nginx.tmp ${NGINX_CONF}
rm nginx.tmp

sudo rm /etc/nginx/sites-enabled/default
sudo ln -s ${NGINX_CONF} /etc/nginx/sites-enabled/

