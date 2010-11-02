ulimit -n 8192
ulimit -a
cd trunk

rm -f ice.config
wget http://hudson.openmicroscopy.org.uk/job/OMERO-trunk/lastSuccessfulBuild/artifact/trunk/ice.config
export ICE_CONFIG=`pwd`/ice.config

./build.py build-default test-compile
./build.py -f components/tools/OmeroPy/build.xml -Dtest.with.fail=true test
./build.py -f components/tools/OmeroPy/build.xml -Dtest.with.fail=true integration
./build.py -f components/tools/OmeroFS/build.xml  -Dtest.with.fail=true test
./build.py -f components/tools/OmeroFS/build.xml  -Dtest.with.fail=true integration
