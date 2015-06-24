#!/bin/bash

set -e -u -x

PGPASSWORD=${PGPASSWORD:-"omero"}
OMERO_JOB=${OMERO_JOB:-"OMERO-stable"}
URL_RELEASE="http://downloads.openmicroscopy.org.uk/latest/omero/"
RELEASE_ARCHIVE="OMERO.server-Beta-4.3.4.zip"
RELEASE_FOLDER=${RELEASE_ARCHIVE%.zip}
DB_VERSION="OMERO4.4"
DB_REVISION="0"
OMERO_PATH="/home/omero/OMERO.server"
OMERO_BIN=$OMERO_PATH/bin
INSTALL_FOLDER="OMERO.server"
OMERO_BUILD_URL="http://hudson.openmicroscopy.org.uk/job/"$OMERO_JOB"/lastSuccessfulBuild"

readAPIValue() {
    URL=$1; shift
    wget -q -O- $URL | sed 's/^<.*>\([^<].*\)<.*>$/\1/'
    }

unzip OMERO.server*.zip
rm -rf OMERO.server*.zip
mv OMERO.server* $INSTALL_FOLDER

mkdir OMERO.data

$OMERO_BIN/omero config set omero.data.dir /home/omero/OMERO.data
$OMERO_BIN/omero config set omero.db.name 'omero'
$OMERO_BIN/omero config set omero.db.user 'omero'
$OMERO_BIN/omero config set omero.db.pass 'omero'
$OMERO_BIN/omero db script -f db.sql "" "" $PGPASSWORD

psql -h localhost -U omero omero < db.sql
