#!/bin/bash
#
# $Id$
#
# Copyright 2008 Glencoe Software, Inc. All rights reserved.
# Use is subject to license terms supplied in LICENSE.txt
#
# Script used by hudson.openmicroscopy.org.uk

export JBOSS_HOME=$HOME/root/opt/jboss
export JAVA_OPTS="-Xmx600M -Djavac.maxmem=600M -Djavadoc.maxmem=600M -XX:MaxPermSize=256m"

#
# Build
#
J=7 java $JAVA_OPTS omero build-all
# integration unfinished


#
# Documentation and build reports
#
java $JAVA_OPTS omero javadoc
java $JAVA_OPTS omero findbugs # separate call to prevent PermGen OOM
java $JAVA_OPTS omero coverage


#
# Prepare a distribution
#
rm -f OMERO.server-build*.zip
java -Domero.version=build$BUILD_NUMBER omero zip
