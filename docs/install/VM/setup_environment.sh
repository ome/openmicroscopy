#!/bin/bash

set -e -u -x

echo "export OMERO_PREFIX=$OMERO_PREFIX" >> ~/.bashrc
echo 'export PATH=$PATH:$OMERO_PREFIX/bin' >> ~/.bashrc

(source ~/.bashrc) || true
