#! /bin/bash
# Script to create the training material using the CLI

set -e
set -u
set -x

HOSTNAME=${HOSTNAME:-localhost}
PORT=${PORT:-4064}
ROOT_PASSWORD=${ROOT_PASSWORD:-ome}
USER_NAME=${USER_NAME:-training_user}
USER_PASSWORD=${USER_PASSWORD:-ome}

# Create training user and group
bin/omero login root@$HOSTNAME:$PORT -w $ROOT_PASSWORD
bin/omero group add training_group --ignore-existing
bin/omero user add training_user training_user training_user training_group --ignore-existing -P ome
bin/omero logout

# Create fake files
touch test.fake
touch "SPW&plates=1&plateRows=1&plateCols=1&fields=1&plateAcqs=1.fake"

# Create training user and group
bin/omero login $USER_NAME@$HOSTNAME:$PORT -w $USER_PASSWORD
nProjects=1
nDatasets=2
echo "Creating projects and datasets"
for (( i=1; i<=$nProjects; i++ ))
do
  project=$(bin/omero obj new Project name='Project '$i)
  for (( j=1; j<=$nDatasets; i++ ))
  do
    dataset=$(bin/omero obj new Dataset name='Dataset '$i-$j)
    bin/omero obj new ProjectDatasetLink parent=$project child=$dataset
    echo "Importing image into dataset"
    imageid=$(bin/omero import -d $dataset test.fake --debug ERROR)
  done
done

# Import plate
echo "Importing SPW file"
plateid=$(bin/omero import "SPW&plates=1&plateRows=1&plateCols=1&fields=1&plateAcqs=1.fake")

# Logout
bin/omero logout

# Create ice.config file
echo "omero.host=$HOSTNAME" > training_ice.config
echo "omero.port=$PORT" >> training_ice.config
echo "omero.user=$USER_NAME" >> training_ice.config
echo "omero.pass=$USER_PASSWORD" >> training_ice.config
echo "omero.projectid=${project##*:}" >> training_ice.config
echo "omero.datasetid=${dataset##*:}" >> training_ice.config
echo "omero.imageid=${imageid}" >> training_ice.config
echo "omero.plateid=${plateid}" >> training_ice.config

# Remove fake file
rm *.fake