#!/bin/bash

set -e
set -u
set -x

VERSION=$1
BUILD=$2
BRANCH=$3

mv virtualbox/omero-vm-latest-build.ova "virtualbox/omero-vm-$VERSION.ova"
mv virtualbox/omero-vm-latest-build.ova.md5sum "virtualbox/omero-vm-$VERSION.ova.md5sum"
perl -i -pe "s/latest-build/$VERSION/" "virtualbox/omero-vm-$VERSION.ova.md5sum"


BASE=`pwd`/releases/OMERO-$BRANCH
for x in $BASE/$BUILD/*;       do ln -sf "$x" "$VERSION/"; done
for x in $BASE-ice34/$BUILD/*; do ln -sf "$x" "$VERSION/"; done
