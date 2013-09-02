#!/bin/bash

set -e -u -x

sudo cp omero-init.d /etc/init.d/omero
sudo chmod a+x /etc/init.d/omero

sudo cp omero-web-init.d /etc/init.d/omero-web
sudo chmod a+x /etc/init.d/omero-web

if [ -f /usr/sbin/update-rc.d ]; then
    # Debian
    sudo update-rc.d -f omero remove
    sudo update-rc.d -f omero defaults 98 02
    sudo update-rc.d -f omero-web remove
    sudo update-rc.d -f omero-web defaults 98 02
elif [ -f /sbin/chkconfig ]; then
    # Redhat
    sudo chkconfig --del omero
    sudo chkconfig --add omero
    sudo chkconfig --del omero-web
    sudo chkconfig --add omero-web
else
    echo "ERROR: Failed to find init.d management script"
    exit 2
fi

