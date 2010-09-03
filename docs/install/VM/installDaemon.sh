#!/bin/bash
sudo cp /home/omero/omero.sh /etc/init.d/omero
sudo chmod a+x /etc/init.d/omero
sudo update-rc.d -f omero remove
sudo update-rc.d -f omero defaults 98 02
