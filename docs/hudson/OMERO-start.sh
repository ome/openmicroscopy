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
export ICE_CONFIG=$WORKSPACE/$OMERO_BRANCH.config

echo omero.version=$OMERO_BUILD > $ICE_CONFIG
echo omero.user=hudson >> $ICE_CONFIG
echo omero.pass=ome >> $ICE_CONFIG
echo omero.host=necromancer.openmicroscopy.org.uk >> $ICE_CONFIG
echo omero.port=$ROUTER >> $ICE_CONFIG
echo omero.rootpass=ome >> $ICE_CONFIG
echo omero.prefix=$OMERO_PREFIX >> $ICE_CONFIG


#
# Prepare installation. Handled by QUICKSTART.bat on Windows
#
. $HOME/.bashrc
./build.py
cd dist

dropdb $OMERO_CONFIG || echo Already gone maybe
createdb -h localhost -U postgres -O hudson $OMERO_CONFIG
createlang -h localhost -U postgres plpgsql $OMERO_CONFIG

python bin/omero db script "" "" ome -f omero.sql
psql $OMERO_CONFIG -f omero.sql

rm -rf $WORKSPACE/datadir
mkdir -p $WORKSPACE/datadir

python bin/omero config set omero.data.dir $WORKSPACE/datadir
python bin/omero config set omero.db.name $OMERO_CONFIG
python bin/omero config set omero.db.user hudson
# Fix TestTables.testBlankTable failure
python bin/omero config set omero.grid.registry_timeout 15000

python bin/omero admin ports --prefix "$OMERO_PREFIX"
python bin/omero admin stop || echo Not running
BUILD_ID=DONT_KILL_ME python bin/omero admin start
python bin/omero admin deploy memcfg omero.blitz.maxmemory=-Xmx1024M omero.blitz.permgen=-XX:MaxPermSize=256m


python bin/omero admin waitup

#
# Create a user
#
python bin/omero login -s localhost -p $ROUTER -u root -w ome
python bin/omero group add hudson_group --perms=rwrw--
python bin/omero user add --admin hudson Test User hudson_group
python bin/omero logout

#
# Import a file for testing
#
FILE=very_small.d3d.dv
rm -f $FILE
wget "http://hudson.openmicroscopy.org.uk/userContent/$FILE"
python bin/omero login -s localhost -p $ROUTER -u hudson -w ome
python bin/omero import "$FILE"


#
# Write test file for OMERO-start jobs
#
FILE=startup.xml
rm -f $FILE
wget "http://hudson.openmicroscopy.org.uk/userContent/$FILE"
mkdir -p target/reports
cp $FILE target/reports
