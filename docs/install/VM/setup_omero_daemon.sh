#!/bin/bash

set -e -u -x

sudo cp omero-init.d /etc/init.d/omero
sudo chmod a+x /etc/init.d/omero
sudo update-rc.d -f omero remove
sudo update-rc.d -f omero defaults 98 02

sudo cp omero-web-init.d /etc/init.d/omero-web
sudo chmod a+x /etc/init.d/omero-web
sudo update-rc.d -f omero-web remove
sudo update-rc.d -f omero-web defaults 98 02
