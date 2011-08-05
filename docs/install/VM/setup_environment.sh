#!/bin/bash

set -e -u -x

echo 'export OMERO_SERVER=/home/omero/OMERO.server' >> .bashrc
echo 'export PATH=$PATH:$OMERO_SERVER/bin' >> .bashrc

# Ugly fix for bug in Debian related to "HISTCONTROL parameter not set" error when we source our amended .bashrc file
# Turn off BASH script debugging whilst we update our environment vars to avoid premature exityup
set +e +u +x
. /home/omero/.bashrc