#! /bin/bash
# Script to create the training material using the CLI

set -e
set -u
set -x

HOSTNAME=${HOSTNAME:-localhost}
PORT=${PORT:-4064}
ROOT_PASSWORD=${ROOT_PASSWORD:-omero}
GROUP_NAME=${GROUP_NAME:-robot_group}
GROUP_PERMS=${GROUP_PERMS:-rwra--}
GROUP_NAME_2=${GROUP_NAME_2:-robot_group_2}
USER_NAME=${USER_NAME:-robot_user}
USER_PASSWORD=${USER_PASSWORD:-ome}
CONFIG_FILENAME=${CONFIG_FILENAME:-robot_ice.config}
IMAGE_NAME=${IMAGE_NAME:-test&sizeZ=3&sizeT=10.fake}
TINY_IMAGE_NAME=${TINY_IMAGE_NAME:-test.fake}
PLATE_NAME=${PLATE_NAME:-SPW&plates=1&plateRows=1&plateCols=2&fields=1&plateAcqs=1.fake}
BULK_ANNOTATION_CSV=${BULK_ANNOTATION_CSV:-bulk_annotation.csv}

# Create robot user and group
bin/omero login root@$HOSTNAME:$PORT -w $ROOT_PASSWORD
bin/omero group add $GROUP_NAME --ignore-existing --perms $GROUP_PERMS
bin/omero group add $GROUP_NAME_2 --ignore-existing
bin/omero user add $USER_NAME $USER_NAME $USER_NAME $GROUP_NAME $GROUP_NAME_2 --ignore-existing -P $USER_PASSWORD
bin/omero user joingroup --name $USER_NAME --group-name $GROUP_NAME --as-owner
bin/omero logout

# Create fake files
touch $IMAGE_NAME
touch $TINY_IMAGE_NAME
touch $PLATE_NAME

# Create batch annotation csv
echo "Well,Well Type,Concentration" > "$BULK_ANNOTATION_CSV"
echo "A1,Control,0" >> "$BULK_ANNOTATION_CSV"
echo "A2,Treatment,10" >> "$BULK_ANNOTATION_CSV"

# Create robot setup
bin/omero login $USER_NAME@$HOSTNAME:$PORT -w $USER_PASSWORD
# Parse the sessions file to get session key
key=$(grep omero.sess $(bin/omero sessions file) | cut -d= -f2)
echo "Session key: $key"
nProjects=1
nDatasets=1
nImages=2
echo "Creating projects and datasets"
for (( i=1; i<=$nProjects; i++ ))
do
  project=$(bin/omero obj new Project name='Project '$i)
  for (( j=1; j<=$nDatasets; j++ ))
  do
    dataset=$(bin/omero obj new Dataset name='Dataset '$i-$j)
    bin/omero obj new ProjectDatasetLink parent=$project child=$dataset
    echo "Importing images into dataset"
    for (( k=1; k<=$nImages; k++ ))
    do
      bin/omero import -d $dataset $IMAGE_NAME --debug ERROR
    done
  done
done

# Create Dataset with images for deleting
delDs=$(bin/omero obj new Dataset name='Delete')
for (( k=1; k<=5; k++ ))
do
  bin/omero import -d $delDs $TINY_IMAGE_NAME --debug ERROR
done

# Import Plate
bin/omero import $PLATE_NAME --debug ERROR > plate_import.log 2>&1
plateid=$(sed -n -e 's/^Plate://p' plate_import.log)
# Use populate_metadata to upload and attach bulk annotation csv
python lib/python/omero/util/populate_metadata.py -k $key Plate:$plateid $BULK_ANNOTATION_CSV

# Logout
bin/omero logout

# Create ice.config file
echo "omero.host=$HOSTNAME" > "$CONFIG_FILENAME"
echo "omero.port=$PORT" >> "$CONFIG_FILENAME"
echo "omero.user=$USER_NAME" >> "$CONFIG_FILENAME"
echo "omero.pass=$USER_PASSWORD" >> "$CONFIG_FILENAME"
echo "omero.projectid=${project##*:}" >> "$CONFIG_FILENAME"
echo "omero.datasetid=${dataset##*:}" >> "$CONFIG_FILENAME"

# Remove fake file
rm *.fake
rm $PLATE_NAME
