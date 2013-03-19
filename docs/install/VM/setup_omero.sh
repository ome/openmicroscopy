#!/bin/bash

set -e -u -x

PGPASSWORD=${PGPASSWORD:-"omero"}
URL_RELEASE="http://cvs.openmicroscopy.org.uk/snapshots/omero/"
RELEASE_ARCHIVE="OMERO.server-Beta-4.3.4.zip"
RELEASE_FOLDER=${RELEASE_ARCHIVE%.zip}
DB_VERSION="OMERO4.4"
DB_REVISION="0"
OMERO_PATH="/home/omero/OMERO.server"
OMERO_BIN=$OMERO_PATH/bin
INSTALL_FOLDER="OMERO.server"
OMERO_BUILD="http://hudson.openmicroscopy.org.uk/job/OMERO-trunk/"

readAPIValue() {
    URL=$1; shift
    wget -q -O- $URL | sed 's/^<.*>\([^<].*\)<.*>$/\1/'
    }

echo "Grabbing last successful QA Build of OMERO.server"
DL_ARCHIVE=""
if [ "x$DL_ARCHIVE" == "x" ]; then

    URL=`readAPIValue $OMERO_BUILD"/lastSuccessfulBuild/api/xml?xpath=/freeStyleBuild/url"`
    FILE=`readAPIValue $OMERO_BUILD"/lastSuccessfulBuild/api/xml?xpath=//relativePath[contains(.,'server')]"`

    wget -q "$URL"artifact/$FILE

    DL_ARCHIVE=`basename $FILE`
    DL_FOLDER=${DL_ARCHIVE%.zip}
else

    DL_LOC="http://hudson.openmicroscopy.org.uk/job/OMERO-trunk/lastSuccessfulBuild/artifact/"
    DL_FOLDER=${DL_ARCHIVE%.zip}

    wget $DL_LOC$DL_ARCHIVE
fi
unzip $DL_ARCHIVE
mv $DL_FOLDER $INSTALL_FOLDER

mkdir OMERO.data

$OMERO_BIN/omero config set omero.data.dir /home/omero/OMERO.data
$OMERO_BIN/omero config set omero.db.name 'omero'
$OMERO_BIN/omero config set omero.db.user 'omero'
$OMERO_BIN/omero config set omero.db.pass 'omero'
$OMERO_BIN/omero db script -f db.sql "" "" $PGPASSWORD

psql -h localhost -U omero omero < db.sql
