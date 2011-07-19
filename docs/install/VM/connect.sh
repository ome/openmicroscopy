#!/bin/bash
# This script will shell into an OMERO.VM

KEY="omerokey"
USER="omero"
HOST="localhost"
PORT="2222"
OPTIONS="StrictHostKeyChecking=no"

ssh -o $OPTIONS -i $KEY -p $PORT -t $USER@$HOST