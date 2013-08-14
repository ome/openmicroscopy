#!/bin/bash

set -e -u -x
bash setup_environment.sh


OMERO_PATH="/home/omero/OMERO.server"
OMERO_BIN=$OMERO_PATH/bin

bash install_deps.sh
bash setup_postgres.sh
bash setup_omero.sh
bash setup_omero_daemon.sh
bash setup_nginx.sh

sudo /etc/init.d/omero start
sudo /etc/init.d/omero-web start
sudo /etc/init.d/nginx start

bash cleanup.sh

sudo halt
