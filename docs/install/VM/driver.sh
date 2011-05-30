#!/bin/bash

set -e -u -x

PASSWORD=${PASSWORD:-"omero"}

# Set up stuff requiring su privileges
echo $PASSWORD | sudo -S sh setup_userspace.sh

# Set up everything else
sudo -k

sh setup_environment.sh
bash setup_omero.sh


# Everything is set up. Unleash the daemon
echo $PASSWORD | sudo -S cp /home/omero/omero-init.d /etc/init.d/omero
echo $PASSWORD | sudo -S chmod a+x /etc/init.d/omero
echo $PASSWORD | sudo -S update-rc.d -f omero remove
echo $PASSWORD | sudo -S update-rc.d -f omero defaults 98 02
echo $PASSWORD | sudo -S reboot