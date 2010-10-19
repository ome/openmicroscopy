#!/bin/bash

export VMNAME=${VMNAME:-"$1"}
export VMNAME=${VMNAME:-"OMERO42"}

export SSH_PF=${SSH_PF:-"$2"}
export SSH_PF=${SSH_PF:-"2222"}

#OMERO_DB_USER=${OMERO_DB_USER:-"$3"}
#OMERO_DB_USER=${OMERO_DB_USER:-"omero"}
#OMERO_DB_NAME=${OMERO_DB_NAME:-"$4"}
#OMERO_DB_NAME=${OMERO_DB_NAME:-"omero"}
#PASSWORD=${PASSWORD:-"$5"}
#PASSWORD=${PASSWORD:-"ome"}
#PGPASSWORD=${PGPASSWORD:-"$PASSWORD"}

set -e
set -u
set -x

cd /home/omero 
cp .ssh/authorized_keys .ssh/authorized_keys.backup
rm -f .ssh/authorized_keys
cat omerokey.pub >> .ssh/authorized_keys







