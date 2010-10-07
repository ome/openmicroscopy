#!/bin/bash
sudo cp /home/omero/omero.sh /etc/init.d/omero
sudo chmod a+x /etc/init.d/omero
sudo update-rc.d -f omero remove
sudo update-rc.d -f omero defaults 98 02

# clean RSA key
cd /home/omero
rm -f .ssh/authorized_keys
mv .ssh/authorized_keys.backup .ssh/authorized_keys