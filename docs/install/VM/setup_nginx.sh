#!/bin/bash

set -e -u -x

$OMERO_PREFIX/bin/omero web config nginx --system --http "$OMERO_WEB_PORT" > nginx.tmp

if [ -d /etc/nginx/sites-available/ -a -d /etc/nginx/sites-enabled/ ]; then
    # Debian
    NGINX_CONF=/etc/nginx/sites-available/omero-web
    sudo cp nginx.tmp ${NGINX_CONF}
    sudo rm /etc/nginx/sites-enabled/default
    sudo ln -s ${NGINX_CONF} /etc/nginx/sites-enabled/
elif [ -d /etc/nginx/conf.d/ ]; then
    # Redhat
    NGINX_CONF=/etc/nginx/conf.d/omero-web.conf
    sudo mv /etc/nginx/conf.d/default.conf /etc/nginx/conf.d/default.disabled
    sudo cp nginx.tmp ${NGINX_CONF}
else
    echo "ERROR: Unable to find nginx configuration directory"
    exit 2
fi

rm nginx.tmp

