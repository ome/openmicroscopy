#!/bin/bash

set -e -u -x

echo 'export JAVA_HOME=/usr/lib/jvm/java-6-sun' >> .bashrc
echo 'export JRE_HOME=/usr/lib/jvm/java-6-sun' >> .bashrc
echo 'export ICE_HOME=/usr/share/Ice-3.3.1' >> .bashrc
echo 'export POSTGRES_HOME=/usr/lib/postgresql/8.4' >> .bashrc
echo 'export OMERO_SERVER=/home/omero/OMERO.server' >> .bashrc
echo 'export PATH=$PATH:$JAVA_HOME/bin:$JRE_HOME/bin:$ICE_HOME:$POSTGRES_HOME/bin:$OMERO_SERVER/bin' >> .bashrc
echo 'export PYTHONPATH=/usr/lib/pymodules/python2.6:$PYTHONPATH' >> .bashrc
echo 'export DYLD_LIBRARY_PATH=/usr/share/java:/usr/lib:$DYLD_LIBRARY_PATH' >> .bashrc
echo 'export LD_LIBRARY_PATH=/usr/share/java:/usr/lib:$LD_LIBRARY_PATH' >> .bashrc

# Ugly fix for bug in Debian related to "HISTCONTROL parameter not set" error when we source our amended .bashrc file
# Turn off BASH script debugging whilst we update our environment vars to avoid premature exityup
set +e +u +x
. /home/omero/.bashrc