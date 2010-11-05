set -e
set -u
set -x

ulimit -n 8192
ulimit -a

#
# Run tests
#
./build.py -f components/tools/OmeroFS/build.xml  -Dtest.with.fail=true test
./build.py -f components/tools/OmeroFS/build.xml  -Dtest.with.fail=true integration

#
# Try DropBox, Hudson will look for ERROR in the output log.
#
cd dist
wget 'http://hudson.openmicroscopy.org.uk/userContent/very_small.d3d%20with%20spaces.dv'
echo omero.fstest.srcFile=very_small.d3d with spaces.dv >> etc/testdropbox.config
echo omero.fs.watchDir=TestDropBox >> etc/testdropbox.config

mkdir -p TestDropBox

bin/omero admin ports --prefix=5 # TODO refactor this into hudson configuration
bin/omero admin ice server start TestDropBox
