#!/bin/bash

set -e -u -x

PASSWORD=${PASSWORD:-"omero"}

OMERO_PATH="/home/omero/OMERO.server"
OMERO_BIN=$OMERO_PATH/bin

# Set up stuff requiring su privileges
echo $PASSWORD | sudo -S sh setup_userspace.sh
echo $PASSWORD | sudo -S sh setup_postgres.sh

# Set up everything else
sudo -k

bash setup_environment.sh
bash virtualbox_fix.sh
bash setup_omero.sh
bash setup_omero_daemon.sh
bash setup_nginx.sh

$OMERO_BIN/omero admin start

#OMERO_POST_INSTALL_SCRIPTS
for cmd in "$@"; do
    bash "`basename $cmd`"
done

echo $PASSWORD | sudo -S halt
#echo $PASSWORD | sudo -S reboot
