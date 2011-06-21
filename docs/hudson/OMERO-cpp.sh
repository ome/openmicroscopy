set -e
set -u
set -x

python build.py clean
python build.py build-all

export RELEASE=Os
python build.py build-cpp

python build.py -f components/tools/OmeroCpp/build.xml test
python build.py -f components/tools/OmeroCpp/build.xml integration

#
# Unpack to target
#
unzip -d target/ target/OMERO.cpp*dbg.zip
OMERO_CPP=`pwd`/`ls -d target/OMERO.cpp*dbg`
mkdir -p $OMERO_CPP/etc/
cp $OMERO_BRANCH.config $OMERO_CPP/etc/ice.config

mkdir -p $OMERO_CPP/lib/client
cp dist/lib/client/omero_client.jar $OMERO_CPP/lib/client/ # For Java compilation

cd examples
python ../target/scons/scons.py builddir=$OMERO_CPP run_cpp=1

#
# Write test file for OMERO-cpp jobs
#
cd ..
FILE=cpp.xml
mkdir -p target/reports
wget -O - "http://hudson.openmicroscopy.org.uk/userContent/$FILE" > target/reports/$FILE
