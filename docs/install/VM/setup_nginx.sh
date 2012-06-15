#!/bin/bash

set -e -u -x

sudo apt-get update && sudo apt-get install nginx
# ugly -- but required for nginx to start
# as of version 0.7.53, nginx will use a compiled-in default error log
# location until it has read the config file. If the user running nginx
# doesn't have write permission to this log location, nginx will raise
# an alert
sudo chown -R omero:omero /var/log/nginx
/home/omero/OMERO.server/bin/omero web config nginx --http 8080 > /home/omero/OMERO.server/omero-web-nginx.conf
/usr/sbin/nginx -c /home/omero/OMERO.server/omero-web-nginx.conf
