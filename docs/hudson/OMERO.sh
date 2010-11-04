set -e
set -u
set -x

export OMERO_BUILD=r"$SVN_REVISION"-trunk"$BUILD_NUMBER"
export PORT_PREFIX=5
export OMERO_CONFIG=$JOB_NAME
export ICE_CONFIG=`pwd`/ice.config
echo omero.version=$OMERO_BUILD > $ICE_CONFIG
echo omero.user=hudson >> $ICE_CONFIG
echo omero.pass=ome >> $ICE_CONFIG
echo omero.host=necromancer.openmicroscopy.org.uk >> $ICE_CONFIG
echo omero.port="$PORT_PREFIX"4064 >> $ICE_CONFIG
echo omero.rootpass=ome >> $ICE_CONFIG

#
# First do our best to clean up any left over servers
#
dist/bin/omero admin stop || echo "Couldn't stop"

./build.py clean
./build.py build-default
./build.py release-docs
./build.py release-zip

#
# By now, any servers should be gone, if they're not
# then we bail.
#
ps auxww | grep icegridnode | grep -v grep | grep OMERO-trunk && exit 1


. $HOME/.bashrc
cd dist

dropdb $OMERO_CONFIG || echo Already gone maybe
createdb -h localhost -U postgres -O hudson $OMERO_CONFIG
createlang -h localhost -U postgres plpgsql $OMERO_CONFIG

rm -f *.sql
bin/omero db script "" "" ome
psql $OMERO_CONFIG < *.sql

rm -rf $WORKSPACE/datadir
mkdir -p $WORKSPACE/datadir

bin/omero config set omero.data.dir $WORKSPACE/datadir
bin/omero config set omero.db.name $OMERO_CONFIG
bin/omero config set omero.db.user hudson
# Fix TestTables.testBlankTable failure
bin/omero config set omero.grid.registry_timeout 15000

bin/omero admin ports --prefix $PORT_PREFIX
bin/omero admin stop || echo Not running
BUILD_ID=DONT_KILL_ME bin/omero admin start
bin/omero admin deploy memcfg omero.blitz.maxmemory=-Xmx1024M omero.blitz.permgen=-XX:MaxPermSize=256m


echo "While we wait on the server to start..."
# Creating source release
cd ..
rm -rf target/svn-export
svn export . target/svn-export
cd target/svn-export
zip -r ../OMERO.source-r"$SVN_REVISION".zip .
cd ../../dist

bin/omero admin waitup


bin/omero login -s localhost -p 54064 -u root -w ome
bin/omero group add hudson_group --perms=rwrw--
bin/omero user add --admin hudson Test User hudson_group
echo "Server started. Will be stopped by join trigger"

