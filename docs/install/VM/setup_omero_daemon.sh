#!/bin/bash

set -e -u -x

PASSWORD=${PASSWORD:-"omero"}

echo $PASSWORD | sudo -S cp /home/omero/omero-init.d /etc/init.d/omero
echo $PASSWORD | sudo -S chmod a+x /etc/init.d/omero
echo $PASSWORD | sudo -S update-rc.d -f omero remove
echo $PASSWORD | sudo -S update-rc.d -f omero defaults 98 02

echo $PASSWORD | sudo -S cp /home/omero/omero-web-init.d /etc/init.d/omero-web
echo $PASSWORD | sudo -S chmod a+x /etc/init.d/omero-web
echo $PASSWORD | sudo -S update-rc.d -f omero-web remove
echo $PASSWORD | sudo -S update-rc.d -f omero-web defaults 98 02
