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


# Build & Test
java_omero build-all
java_omero test-integration

#
# Documentation and build reports
#
java_omero release-javadoc
java_omero release-findbugs

#
# Prepare a distribution
#
rm -f OMERO.server-build*.zip
java_omero release-zip

# Install into the hudson repository
java_omero ivy-hudson
