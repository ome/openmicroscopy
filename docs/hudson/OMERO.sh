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
echo Building $OMERO_BRANCH

./build.py build-dev release-all
if [ -d .git ]
then
  ./build.py release-src
fi

if [ -z "${RELEASE-}" ]; then
  pushd target
  ../docs/hudson/omero_insight_sign.py ../lib/keystore omedev OMERO.server-*.zip -kp omedev -cp omedev -ts no -oz OMERO.server-*.zip
  popd
fi

# Log information
echo BUILD_NUMBER=$BUILD_NUMBER > target/$OMERO_BRANCH.log
echo OMERO_BRANCH=$OMERO_BRANCH >> target/$OMERO_BRANCH.log
git_info > target/GIT_INFO
env | sort >> target/GIT_INFO
