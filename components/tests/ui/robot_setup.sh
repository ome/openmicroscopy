#! /bin/bash
# Script to create the training material using the CLI

set -e
set -u
set -x

now=$(date +"%s")

HOSTNAME=${HOSTNAME:-localhost}
PORT=${PORT:-4064}
ROOT_PASSWORD=${ROOT_PASSWORD:-omero}
GROUP_NAME=${GROUP_NAME:-robot_group}-$now
GROUP_PERMS=${GROUP_PERMS:-rwra--}
GROUP_NAME_2=${GROUP_NAME_2:-robot_group_2}-$now
USER_NAME=${USER_NAME:-robot_user}-$now
USER_PASSWORD=${USER_PASSWORD:-ome}
CONFIG_FILENAME=${CONFIG_FILENAME:-robot_ice.config}
IMAGE_NAME=${IMAGE_NAME:-test&acquisitionDate=2012-01-01_00-00-00&sizeZ=3&sizeT=10.fake}
TINY_IMAGE_NAME=${TINY_IMAGE_NAME:-test&acquisitionDate=2012-01-01_00-00-00.fake}
MIF_IMAGE_NAME=${MIF_IMAGE_NAME:-test&series=3.fake}
PLATE_NAME=${PLATE_NAME:-test&plates=1&plateAcqs=2&plateRows=2&plateCols=3&fields=5&screens=0.fake}
TINY_PLATE_NAME=${TINY_PLATE_NAME:-test&plates=1&plateAcqs=1&plateRows=1&plateCols=1&fields=1&screens=0.fake}
BULK_ANNOTATION_CSV=${BULK_ANNOTATION_CSV:-bulk_annotation.csv}
FILE_ANNOTATION=${FILE_ANNOTATION:-robot_file_annotation.txt}

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
touch $TINY_PLATE_NAME
touch $MIF_IMAGE_NAME

# Create batch annotation csv
echo "Well,Well Type,Concentration" > "$BULK_ANNOTATION_CSV"
echo "A1,Control,0" >> "$BULK_ANNOTATION_CSV"
echo "A2,Treatment,10" >> "$BULK_ANNOTATION_CSV"

# Create file for upload as File Annotation
echo "Robot test file annotations" > "$FILE_ANNOTATION"

# Create a python script for setting posX and posY on wellsamples
# Used below after importing a plate
WELLSCRIPT="wsScript.py"
echo "import argparse
import omero
from omero.gateway import BlitzGateway
from omero.model.enums import UnitsLength
parser = argparse.ArgumentParser()
parser.add_argument('host', help='OMERO host')
parser.add_argument('port', help='OMERO port')
parser.add_argument('key', help='OMERO session key')
parser.add_argument('plateId', help='Plate ID to process', type=int)
args = parser.parse_args()
conn = BlitzGateway(host=args.host, port=args.port)
conn.connect(args.key)
update = conn.getUpdateService()
plate = conn.getObject('Plate', args.plateId)
r = UnitsLength.REFERENCEFRAME
cols = 3
for well in plate.listChildren():
    for i, ws in enumerate(well.listChildren()):
        x = i % cols
        y = i / cols
        ws = conn.getQueryService().get('WellSample', ws.id)
        ws.posY = omero.model.LengthI(y, r)
        ws.posX = omero.model.LengthI(x, r)
        update.saveObject(ws)" > "$WELLSCRIPT"

# Create robot setup
bin/omero login $USER_NAME@$HOSTNAME:$PORT -w $USER_PASSWORD
# Parse the sessions file to get session key
key=$(bin/omero sessions key)
echo "Session key: $key"
nProjects=1
nDatasets=1
nImages=10
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
for (( k=1; k<=10; k++ ))
do
  bin/omero import -d $delDs $TINY_IMAGE_NAME --debug ERROR
done

# Create Dataset with MIF images
mifDs=$(bin/omero obj new Dataset name='MIF Images')
for (( k=1; k<=2; k++ ))
do
  bin/omero import -d $mifDs $MIF_IMAGE_NAME --debug ERROR
done

# Import Plate and rename
bin/omero import $PLATE_NAME --debug ERROR > plate_import.log 2>&1
plateid=$(sed -n -e 's/^Plate://p' plate_import.log)
bin/omero obj update Plate:$plateid name=spwTests
# Use populate_metadata to upload and attach bulk annotation csv
OMERO_DEV_PLUGINS=1 bin/omero metadata populate Plate:$plateid --file $BULK_ANNOTATION_CSV

# Run script to populate WellSamples with posX and posY values
PYTHONPATH=./lib/python python $WELLSCRIPT $HOSTNAME $PORT $key $plateid

# Import Tiny Plate (single acquisition & well) and rename
bin/omero import $TINY_PLATE_NAME --debug ERROR > show_import.log 2>&1
plateid=$(sed -n -e 's/^Plate://p' show_import.log)
bin/omero obj update Plate:$plateid name=tinyPlate
# Import Image into Project/Dataset and rename for test
showP=$(bin/omero obj new Project name='showProject')
showD=$(bin/omero obj new Dataset name='showDataset')
bin/omero obj new ProjectDatasetLink parent=$showP child=$showD
for (( k=1; k<=2; k++ ))
do
  bin/omero import -d $showD $IMAGE_NAME --debug ERROR > show_import.log 2>&1
  imageid=$(sed -n -e 's/^Image://p' show_import.log)
  bin/omero obj update Image:$imageid name=testShowImage$k
done

# Create Screen with empty plates for Create Scenario
scrDs=$(bin/omero obj new Screen name='CreateScenario')
for (( k=1; k<=6; k++ ))
do
  bin/omero import -r $scrDs $TINY_PLATE_NAME --debug ERROR
done

# Create Orphaned Images for Create Scenario
for (( k=1; k<=10; k++ ))
do
  bin/omero import $TINY_IMAGE_NAME --debug ERROR
done

# Uplodad file and create FileAnnotation
ofile=$(bin/omero upload $FILE_ANNOTATION)
bin/omero obj new FileAnnotation file=$ofile

# Logout
bin/omero logout

# Create ice.config file
echo "omero.host=$HOSTNAME" > "$CONFIG_FILENAME"
echo "omero.port=$PORT" >> "$CONFIG_FILENAME"
echo "omero.user=$USER_NAME" >> "$CONFIG_FILENAME"
echo "omero.pass=$USER_PASSWORD" >> "$CONFIG_FILENAME"
echo "omero.projectid=${project##*:}" >> "$CONFIG_FILENAME"
echo "omero.datasetid=${dataset##*:}" >> "$CONFIG_FILENAME"

# Remove fake file and logs
rm *.fake
rm plate_import.log
rm show_import.log
rm $WELLSCRIPT
