#!/bin/bash

set -e -u -x

NGINX_CONF=/etc/nginx/sites-available/omero-web
~omero/OMERO.server/bin/omero web config nginx --system --http 8080 > nginx.tmp
sudo cp nginx.tmp ${NGINX_CONF}
rm nginx.tmp

sudo rm /etc/nginx/sites-enabled/default
sudo ln -s ${NGINX_CONF} /etc/nginx/sites-enabled/

