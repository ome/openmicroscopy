#!/bin/bash

set -e -u -x

source omero_guest_settings.sh

bash setup_environment.sh

bash setup_postgres.sh
bash setup_omero.sh
bash setup_omero_daemon.sh
bash setup_nginx.sh

sudo /etc/init.d/omero start
sudo /etc/init.d/omero-web start
sudo /etc/init.d/nginx start

sleep 10

bash cleanup.sh

for SCRIPT in post/*.sh; do
    if [ -f "$SCRIPT" ]; then
        bash "$SCRIPT"
    fi
done

sleep 10

sudo halt
