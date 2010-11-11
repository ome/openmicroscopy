## FIXME This needs to be configured externally.
OMERO_CONFIG=OMERO-$OMERO_BRANCH
OMERO_DATA_DIR=/hudson/.hudson/jobs/OMERO-trunk/workspace/datadir

./build.py -Domero.data.dir=$OMERO_DATA_DIR -Domero.db.name=$OMERO_CONFIG -Domero.db.user=hudson clean build-default test-compile test-server
