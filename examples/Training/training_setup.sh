#! /bin/bash
# Script to create the training material using the CLI

set -e
set -u
set -x

HOSTNAME=${HOSTNAME:-localhost}
PORT=${PORT:-4064}
ROOT_PASSWORD=${ROOT_PASSWORD:-omero}
GROUP_NAME=${GROUP_NAME:-training_group}
GROUP_NAME_2=${GROUP_NAME_2:-training_group-2}
USER_NAME=${USER_NAME:-training_user}
USER_NAME_2=${USER_NAME_2:-training_user-2}
USER_PASSWORD=${USER_PASSWORD:-ome}
CONFIG_FILENAME=${CONFIG_FILENAME:-training_ice.config}

# Create training user and group
bin/omero login root@$HOSTNAME:$PORT -w $ROOT_PASSWORD
bin/omero group add $GROUP_NAME --type read-only --ignore-existing
bin/omero group add $GROUP_NAME_2 --type read-only --ignore-existing
bin/omero user add $USER_NAME $USER_NAME $USER_NAME $GROUP_NAME $GROUP_NAME_2 --ignore-existing -P $USER_PASSWORD
bin/omero user add $USER_NAME_2 $USER_NAME_2 $USER_NAME_2 $GROUP_NAME $GROUP_NAME_2 --ignore-existing -P $USER_PASSWORD
bin/omero logout

# Create fake files
touch test.fake

# Create training user and group
bin/omero login $USER_NAME@$HOSTNAME:$PORT -w $USER_PASSWORD -g $GROUP_NAME
nProjects=1
nDatasets=2
echo "Creating projects and datasets"
for (( i=1; i<=$nProjects; i++ ))
do
  project=$(bin/omero obj new Project name='Project '$i)
  for (( j=1; j<=$nDatasets; j++ ))
  do
    dataset=$(bin/omero obj new Dataset name='Dataset '$i-$j)
    bin/omero obj new ProjectDatasetLink parent=$project child=$dataset
    echo "Importing image into dataset"
    bin/omero import -d $dataset test.fake --debug ERROR
  done
done

# Create orphaned datasets
for (( j=1; j<=$nDatasets; j++ ))
do
  bin/omero obj new Dataset name='Orphaned dataset '$j
done

# Import Image
echo "Importing image file"
touch "test&sizeT=10&sizeZ=5&sizeC=3.fake"
bin/omero import "test&sizeT=10&sizeZ=5&sizeC=3.fake" > image_import.log 2>&1
imageid=$(sed -n -e 's/^Image://p' image_import.log)

# Create screen/plate
screen=$(bin/omero obj new Screen name='Screen')
plate=$(bin/omero obj new Plate name='Plate')
bin/omero obj new ScreenPlateLink parent=$screen child=$plate

# Import orphaned plate
echo "Importing SPW file"
touch "SPW&plates=1&plateRows=1&plateCols=1&fields=1&plateAcqs=1.fake"
bin/omero import -r $screen "SPW&plates=1&plateRows=1&plateCols=1&fields=1&plateAcqs=1.fake" > plate_import.log 2>&1
plateid=$(sed -n -e 's/^Plate://p' plate_import.log)

# Logout
bin/omero logout

# Create data owned by another user in the second group
bin/omero login $USER_NAME_2@$HOSTNAME:$PORT -w $USER_PASSWORD -g $GROUP_NAME
bin/omero obj new Project name='Project 0'
bin/omero obj new Dataset name='Dataset 0'
bin/omero import test.fake --debug ERROR
bin/omero obj new Screen name='Screen'
bin/omero obj new Plate name='Plate'

# Create data owned by the same user in the second group
bin/omero login $USER_NAME@$HOSTNAME:$PORT -w $USER_PASSWORD -g $GROUP_NAME_2
bin/omero obj new Project name='Project 0'
bin/omero obj new Dataset name='Dataset 0'
bin/omero import test.fake --debug ERROR
bin/omero obj new Screen name='Screen'
bin/omero obj new Plate name='Plate'

# Logout
bin/omero logout

# Create ice.config file
echo "omero.host=$HOSTNAME" > "$CONFIG_FILENAME"
echo "omero.port=$PORT" >> "$CONFIG_FILENAME"
echo "omero.user=$USER_NAME" >> "$CONFIG_FILENAME"
echo "omero.pass=$USER_PASSWORD" >> "$CONFIG_FILENAME"
echo "omero.group=$GROUP_NAME" >> "$CONFIG_FILENAME"
echo "omero.projectid=${project##*:}" >> "$CONFIG_FILENAME"
echo "omero.datasetid=${dataset##*:}" >> "$CONFIG_FILENAME"
echo "omero.imageid=${imageid}" >> "$CONFIG_FILENAME"
echo "omero.plateid=${plateid}" >> "$CONFIG_FILENAME"
echo "omero.screenid=${screen##*:}" >> "$CONFIG_FILENAME"

# Remove fake file
rm *.fake
