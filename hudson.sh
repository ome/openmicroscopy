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

export JBOSS_HOME=${JBOSS_HOME:-"HOME/root/opt/jboss"}
export JAVA_OPTS=${JAVA_OPTS:-"-Xmx600M -Djavac.maxmem=600M -Djavadoc.maxmem=600M -XX:MaxPermSize=256m"}

#
# Various builds for usability
#
java $JAVA_OPTS omero clean build-importer
#  Temporarily removing the following due to
#  PermGen exceptions
#java $JAVA_OPTS omero clean build-webadmin
#java $JAVA_OPTS omero clean build-ear
#java $JAVA_OPTS omero clean build-blitz
#java $JAVA_OPTS omero clean build-py

#
# Real build
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
