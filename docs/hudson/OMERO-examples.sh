set -e
set -u
set -x

. $HOME/.bashrc
rm -rf OMERO.server*
cp /hudson/.hudson/jobs/OMERO-trunk/lastSuccessful/archive/trunk/target/OMERO.server-*.zip .
unzip -d OMERO.server OMERO.server-*.zip
rm -f test && ln -s OMERO.server/OMERO.server* test

rm -rf OMERO.cpp*
cp /hudson/.hudson/jobs/OMERO-trunk-cpp-so/lastSuccessful/archive/trunk/target/OMERO.cpp-*64dbg.zip .
unzip OMERO.cpp*.zip
rsync -va OMERO.cpp*/include test
rsync -va OMERO.cpp*/lib test

rm -f ice.config
wget http://hudson.openmicroscopy.org.uk/job/OMERO-trunk/lastSuccessfulBuild/artifact/trunk/ice.config
export ICE_CONFIG=`pwd`/ice.config

export TEST=$WORKSPACE/test
rm -rf examples && cp -r /hudson/.hudson/jobs/OMERO-trunk/workspace/trunk/examples .
rm -rf scons && cp -r /hudson/.hudson/jobs/OMERO-trunk/workspace/trunk/target/scons .
cd examples
python ../scons/scons.py builddir=$TEST run=1

