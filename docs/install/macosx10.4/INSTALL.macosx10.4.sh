#!/bin/bash

#
# Installation instructions for Mac OS X 10.4
#
# The 10.4 install was tested on a 10.4.11 machine with no special
# packages installed. Alternatively, it is possible to install via
# www.MacPorts.org    See below for more information.

curl http://www.zeroc.com/download/Ice/3.2/Ice-3.2.1-bin-macosx.tar.gz > Ice-3.2.1-bin-macosx.tar.gz
curl http://hudson.openmicroscopy.org.uk/job/OMERO/lastSuccessfulBuild/artifact/trunk/OMERO.server-build1007.zip > OMERO.server-build1007.zip

unzip OMERO*zip
tar xzf Ice*tar.gz

chmod a+x OMERO*/bin/omero
ln -s OMERO*/bin/omero .

# Configure omero.properties

#
# The following environment settings are available via from
# the env.sh file in this directory:
#
#   source env.sh
#
PATH=Ice-3.2.1/bin/:$PATH DYLD_LIBRARY_PATH=Ice-3.2.1/lib PYTHONPATH=Ice-3.2.1/python/ ./omero admin start




########################################
#
# Via MacPorts:
# $ port install ice-cpp ice-java ice-python
# $ port install postgresql82 postgresql82-server
