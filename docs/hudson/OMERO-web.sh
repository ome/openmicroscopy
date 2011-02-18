set -e
set -u
set -x

#
# Run tests
#
./build.py clean
./build.py
python dist/bin/omero web test webadmin
