#!/bin/bash

. /home/omero/venv/bin/activate
cd /tmp
unzip openmicroscopy*.zip
cd openmicroscopy*
./build.py
mv dist /tmp/dist
rm -rf /tmp/openmicroscopy*
