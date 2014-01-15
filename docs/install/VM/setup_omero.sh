#!/bin/bash

set -e -u -x

OMERO_BIN=$OMERO_PREFIX/bin
OMERO_BUILD_URL="http://hudson.openmicroscopy.org.uk/job/$OMERO_JOB/lastSuccessfulBuild"

readAPIValue() {
    URL=$1; shift
    wget -q -O- $URL | sed 's/^<.*>\([^<].*\)<.*>$/\1/'
    }

if [[ ${OMERO_JOB} == *.zip ]]; then
    DL_ARCHIVE=`basename "$OMERO_JOB"`
else
    echo "Grabbing last successful QA Build of OMERO.server ($OMERO_JOB)"
    URL=`readAPIValue $OMERO_BUILD_URL"/api/xml?xpath=/freeStyleBuild/url"`
    FILE=`readAPIValue $OMERO_BUILD_URL"/api/xml?xpath=//relativePath[contains(.,'server')]"`

    wget -q "$URL"artifact/$FILE
    DL_ARCHIVE=`basename $FILE`
fi

DL_FOLDER=${DL_ARCHIVE%.zip}
unzip $DL_ARCHIVE
mkdir -p `dirname $OMERO_PREFIX`
mv $DL_FOLDER $OMERO_PREFIX

mkdir -p "$OMERO_DATA_DIR"

$OMERO_BIN/omero config set omero.data.dir "$OMERO_DATA_DIR"
$OMERO_BIN/omero config set omero.db.name "$OMERO_DB_NAME"
$OMERO_BIN/omero config set omero.db.user "$OMERO_DB_USER"
$OMERO_BIN/omero config set omero.db.pass "$OMERO_DB_PASS"
$OMERO_BIN/omero db script -f "$OMERO_PREFIX/db.sql" "" "" "$OMERO_ROOT_PASS"

psql -h localhost -U "$OMERO_DB_USER" "$OMERO_DB_NAME" < "$OMERO_PREFIX/db.sql"
