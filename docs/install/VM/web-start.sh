#!/bin/bash

set -e
set -u
set -x


cd /Server/omero/dist
sudo -u omero bin/omero web syncmedia
sudo -u omero bin/omero web start localhost 8080

cat /Server/omero/dist/var/django.pid
sleep 5

echo "Web server started!"