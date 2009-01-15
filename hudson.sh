#!/bin/bash
#
# $Id$
#
# Copyright 2008 Glencoe Software, Inc. All rights reserved.
# Use is subject to license terms supplied in LICENSE.txt
#
# Script used by hudson.openmicroscopy.org.uk

set -e
set -u
set -x

export JAVA_OPTS=${JAVA_OPTS:-"-Xmx600M -Djavac.maxmem=600M -Djavadoc.maxmem=600M -XX:MaxPermSize=256m"}

java_omero(){
    java $JAVA_OPTS -Domero.version=build$BUILD_NUMBER omero "$@"
}

#
# Cleaning to prevent strange hudson errors about
# stale tests and general weirdness.
#
java_omero clean

#
# Run the default build which most notably omits the C++ bindings.
# This is primarily due to build time of C++, but it also means that
# all the resulting build artifacts are platform-independent. Another
# later build can be used to produce platform-specific bits.
#
java_omero

#
# Various builds for usability
#

#
# Documentation and build reports
#
java_omero javadoc
java_omero findbugs

#
# Prepare a distribution
#
rm -f OMERO.server-build*.zip
java_omero zip

# Install into the hudson repository
java_omero ivy-hudson
