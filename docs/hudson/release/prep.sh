#!/bin/bash

set -e
set -u
set -x

OMERO_RELASE=${OMERO_RELASE:-"$1"}
OMERO_BUILD=${OMERO_BUILD:-"$2"}
OMERO_ICE34_BUILD=${OMERO_ICE34_BUILD:-${OMERO_BUILD}}
OMERO_JOB=${OMERO_JOB:-OMERO-stable}
OMERO_ICE34_JOB=${OMERO_ICE34_JOB:-OMERO-stable-ice34}
VIRTUALBOX_PATH=${VIRTUALBOX_PATH:-/ome/data_repo/virtualbox}
ARTIFACT_PATH=${ARTIFACT_PATH:-/ome/data_repo/releases}
RELEASE_PATH=${RELEASE_PATH:-/var/www/cvs.openmicroscopy.org.uk/snapshots}

# Test OMERO artifact directory existence
OMERO_ARTIFACT_PATH=$ARTIFACT_PATH/$OMERO_JOB/$OMERO_BUILD
[[ -d $OMERO_ARTIFACT_PATH ]] || exit
OMERO_ICE34_ARTIFACT_PATH=$ARTIFACT_PATH/$OMERO_ICE34_JOB/$OMERO_ICE34_BUILD
[[ -d $OMERO_ICE34_ARTIFACT_PATH ]] || exit

# Create OMERO release directory
OMERO_RELEASE_PATH=$RELEASE_PATH/omero/$OMERO_RELASE
mkdir $OMERO_RELEASE_PATH

# Symlink OMERO artifacts
for x in $OMERO_ARTIFACT_PATH/*;
    do ln -sf "$x" "$OMERO_RELEASE_PATH/";
done
for x in $OMERO_ICE34_ARTIFACT_PATH/*;
    do ln -sf "$x" "$OMERO_RELEASE_PATH/";
done

# Rename the virtualbox artifacts
OMERO_VIRTUALBOX_PATH=$RELEASE_PATH/omero/virtualbox
cp $VIRTUALBOX_PATH/omero-vm-latest-build.ova "$OMERO_VIRTUALBOX_PATH/omero-vm-$OMERO_RELASE.ova"
cp $VIRTUALBOX_PATH/omero-vm-latest-build.ova.md5sum "$OMERO_VIRTUALBOX_PATH/omero-vm-$OMERO_RELASE.ova.md5sum"
perl -i -pe "s/latest-build/$OMERO_RELASE/" "$OMERO_VIRTUALBOX_PATH/omero-vm-$OMERO_RELASE.ova.md5sum"