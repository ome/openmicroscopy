#!/bin/bash

OMERO_VERSION=${OMERO_VERSION:-"$1"}
OMERO_VERSION=${OMERO_VERSION:-"OMERO4.2"}

OMERO_PATCH=${OMERO_PATCH:-"$2"}
OMERO_PATCH=${OMERO_PATCH:-"0"}

OMERO_DB_USER=${OMERO_DB_USER:-"$3"}
OMERO_DB_USER=${OMERO_DB_USER:-"omero"}

OMERO_DB_NAME=${OMERO_DB_NAME:-"$4"}
OMERO_DB_NAME=${OMERO_DB_NAME:-"omero"}

PASSWORD=${PASSWORD:-"$5"}
PASSWORD=${PASSWORD:-"ome"}

PGPASSWORD=${PGPASSWORD:-"$PASSWORD"}

OMERODIR=${OMERODIR:-"$6"}
OMERODIR=${OMERODIR:-"/OMERO"}

REVISION=${REVISION:-"$7"}
REVISION=${REVISION:-"HEAD"}

set -e
set -u
set -x

##
# Set up server
#

FILE=.pgpass
sudo -u omero cat > ${FILE} << EOF
localhost:5432:$OMERO_DB_NAME:$OMERO_DB_USER:$PGPASSWORD
EOF
chmod 600 .pgpass

cd /Server/omero/dist

sudo -u omero bin/omero config set omero.data.dir $OMERODIR

echo "CREATE USER $OMERO_DB_USER PASSWORD '$PGPASSWORD'" | sudo -u postgres psql

###
### FREEZES SOMEWHERE HERE
###

sudo -u postgres createdb -O  $OMERO_DB_USER $OMERO_DB_NAME && {
    sudo -u postgres createlang plpgsql $OMERO_DB_NAME
    sudo -u omero bin/omero config set omero.db.name $OMERO_DB_NAME
    sudo -u omero bin/omero config set omero.db.user $OMERO_DB_USER
    sudo -u omero bin/omero config set omero.db.pass $PGPASSWORD
    sudo -u omero bin/omero db script -f DB.sql $OMERO_VERSION $OMERO_PATCH $PGPASSWORD
    psql -h localhost -U $OMERO_DB_USER $OMERO_DB_NAME < DB.sql
} || echo DB Exists

#sudo -u omero bin/omero admin start