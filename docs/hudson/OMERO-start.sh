set -e
set -u
set -x

#
# First do our best to clean up any left over servers
#
test -e dist/bin/omero && \
    (python dist/bin/omero admin stop || echo 'Not running?')

# Print out the environment
set

export OMERO_CONFIG=$OMERO_BRANCH
export ROUTERPREFIX=$OMERO_PREFIX
export ROUTER="$ROUTERPREFIX"4064
export ICE_CONFIG=`pwd`/$OMERO_BRANCH.config
export OMERO_DATA=`pwd`/target/datadir
GROUP=hudson_group
USER=hudson
USERPASSWORD=ome

echo omero.user=$USER >> $ICE_CONFIG
echo omero.pass=$USERPASSWORD >> $ICE_CONFIG
echo omero.host=$OMERO_HOST >> $ICE_CONFIG
echo omero.port=$ROUTER >> $ICE_CONFIG
echo omero.rootpass=ome >> $ICE_CONFIG
echo omero.prefix=$OMERO_PREFIX >> $ICE_CONFIG


#
# Prepare installation. Handled by QUICKSTART.bat on Windows
#
./build.py clean
./build.py
cd dist

dropdb $OMERO_CONFIG || echo Already gone maybe
createdb -h localhost -U postgres -E UTF8 -O hudson $OMERO_CONFIG
createlang -h localhost -U postgres plpgsql $OMERO_CONFIG || echo Already installed maybe

python bin/omero db script "" "" ome -f omero.sql
psql $OMERO_CONFIG -f omero.sql

rm -rf $OMERO_DATA
mkdir -p $OMERO_DATA

python bin/omero config set omero.data.dir $OMERO_DATA
python bin/omero config set omero.db.name $OMERO_CONFIG
python bin/omero config set omero.db.user hudson
# Fix TestTables.testBlankTable failure
python bin/omero config set omero.grid.registry_timeout 15000

python bin/omero admin ports --skipcheck --prefix "$OMERO_PREFIX"
python bin/omero admin stop || echo Not running
BUILD_ID=DONT_KILL_ME python bin/omero admin start
python bin/omero admin deploy memcfg omero.blitz.maxmemory=-Xmx1024M omero.blitz.permgen=-XX:MaxPermSize=256m


python bin/omero admin waitup

#
# Create a user
#
python bin/omero login -s localhost -p $ROUTER -u root -w ome
python bin/omero group add $GROUP --perms=rwrw--
python bin/omero user add --admin $USER Test User $GROUP -P $USERPASSWORD
python bin/omero logout

#
# Import a file for testing
#
FILE=very_small.d3d.dv
rm -f $FILE
wget "http://hudson.openmicroscopy.org.uk/userContent/$FILE"
python bin/omero login -s localhost -p $ROUTER -u $USER -w $USERPASSWORD
python bin/omero import "$FILE"

#
# Try DropBox, Hudson will look for ERROR in the output log.
# Must happen from the -start since it runs in the main
# icegridnode process
#
FILE1=very_small.d3d.dv
FILE2="very_small.d3d with spaces.dv"
rm -f $FILE1
rm -f $FILE2
wget "http://hudson.openmicroscopy.org.uk/userContent/$FILE1"
wget "http://hudson.openmicroscopy.org.uk/userContent/$FILE2"
echo omero.fstest.srcFile=$FILE1\;$FILE2\;$FILE1 >> etc/testdropbox.config
echo omero.fs.watchDir=TestDropBox >> etc/testdropbox.config
echo omero.fstest.timeout=480 >> etc/testdropbox.config

mkdir -p TestDropBox

python bin/omero admin ice server start TestDropBox

#
# Write test file for OMERO-start jobs
#
cd ..
FILE=startup.xml
mkdir -p target/reports
wget -O - "http://hudson.openmicroscopy.org.uk/userContent/$FILE" > target/reports/$FILE
