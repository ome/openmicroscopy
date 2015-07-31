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

# Create robot user and group
bin/omero login root@$HOSTNAME:$PORT -w $ROOT_PASSWORD
bin/omero group add $GROUP_NAME --ignore-existing --perms $GROUP_PERMS
bin/omero group add $GROUP_NAME_2 --ignore-existing
bin/omero user add $USER_NAME $USER_NAME $USER_NAME $GROUP_NAME $GROUP_NAME_2 --ignore-existing -P $USER_PASSWORD
bin/omero user joingroup --name $USER_NAME --group-name $GROUP_NAME --as-owner
bin/omero logout

# Create fake file
touch $IMAGE_NAME
touch $TINY_IMAGE_NAME

# Create robot setup
bin/omero login $USER_NAME@$HOSTNAME:$PORT -w $USER_PASSWORD
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
