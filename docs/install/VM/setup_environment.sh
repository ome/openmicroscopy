#!/bin/bash

set -e -u -x

echo 'export OMERO_SERVER=/home/omero/OMERO.server' >> .bashrc
echo 'export PATH=$PATH:$OMERO_SERVER/bin' >> .bashrc

(source /home/omero/.bashrc) || true