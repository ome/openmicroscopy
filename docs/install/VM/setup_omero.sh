#!/bin/bash

set -e -u -x

PGPASSWORD=${PGPASSWORD:-"omero"}
TARGET=${TARGET:-"QA"} # NB. Valid args are {QA | RELEASE}
URL_RELEASE="http://cvs.openmicroscopy.org.uk/snapshots/omero/"
RELEASE_ARCHIVE="OMERO.server-Beta-4.3.1.zip"
RELEASE_FOLDER=${RELEASE_ARCHIVE%.zip}
DB_VERSION="OMERO4.3"
DB_REVISION="0"
OMERO_PATH="/home/omero/OMERO.server"
OMERO_BIN=$OMERO_PATH/bin
INSTALL_FOLDER="OMERO.server"

if [[ "$TARGET" == "RELEASE" ]]; then
	echo "Grabbing current stable release of OMERO.server: $RELEASE_ARCHIVE"
	wget -q $URL_RELEASE$RELEASE_ARCHIVE
	unzip $RELEASE_ARCHIVE
	mv $RELEASE_FOLDER $INSTALL_FOLDER
else
	echo "Grabbing last successful QA Build of OMERO.server"
    DL_ARCHIVE=""
    if [ "x$DL_ARCHIVE" == "x" ]; then

    	URL=`wget -q -O- "http://hudson.openmicroscopy.org.uk/job/OMERO-trunk-qa-builds/lastSuccessfulBuild/api/xml?xpath=/freeStyleBuild/url/text()"`
    	FILE=`wget -q -O- "http://hudson.openmicroscopy.org.uk/job/OMERO-trunk-qa-builds/lastSuccessfulBuild/api/xml?xpath=//relativePath[contains(.,'server')]/text()"`

    	wget -q "$URL"artifact/$FILE

        DL_ARCHIVE=$FILE
    	DL_FOLDER=${DL_ARCHIVE%.zip}
    else
        DL_LOC="http://hudson.openmicroscopy.org.uk/job/OMERO-trunk-qa-builds/lastSuccessfulBuild/artifact/"
        DL_FOLDER=${DL_ARCHIVE%.zip}
    
    	wget $DL_LOC$DL_ARCHIVE
    fi
    unzip $DL_ARCHIVE
    mv $DL_FOLDER $INSTALL_FOLDER
fi

mkdir OMERO.data

$OMERO_BIN/omero config set omero.data.dir /home/omero/OMERO.data
$OMERO_BIN/omero config set omero.db.name 'omero'
$OMERO_BIN/omero config set omero.db.user 'omero'
$OMERO_BIN/omero config set omero.db.pass 'omero'
$OMERO_BIN/omero db script -f db.sql $DB_VERSION $DB_REVISION $PGPASSWORD

psql -h localhost -U omero omero < db.sql
