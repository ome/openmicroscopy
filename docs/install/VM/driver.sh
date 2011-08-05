#!/bin/bash

set -e -u -x

PASSWORD=${PASSWORD:-"omero"}

# Set up stuff requiring su privileges
echo $PASSWORD | sudo -S sh setup_userspace.sh
echo $PASSWORD | sudo -S sh setup_postgres.sh

# Set up everything else
sudo -k

bash setup_environment.sh
bash setup_omero.sh
bash setup_omero_daemon.sh

echo $PASSWORD | sudo -S reboot