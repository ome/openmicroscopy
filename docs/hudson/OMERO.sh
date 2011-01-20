#!/bin/bash
#
# Copyright 2010 Glencoe Software, Inc. All rights reserved.
# Use is subject to license terms supplied in LICENSE.txt
#
# Main build script. Primarily launched by hudson.
#
set -e
set -u
set -x

source docs/hudson/functions.sh
export OMERO_BUILD=`git_short_rev`
echo Building $OMERO_BRANCH

./build.py clean
./build.py build-default
./build.py release-docs
./build.py release-zip

# Log information
echo OMERO_BUILD=$OMERO_BUILD > target/$OMERO_BRANCH.log
echo OMERO_BRANCH=$OMERO_BRANCH >> target/$OMERO_BRANCH.log
git_info > target/GIT_INFO
env | sort >> target/GIT_INFO
git_zip ../OMERO.source-$OMERO_BUILD.zip
