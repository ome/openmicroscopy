set -e
set -u
set -x

#
# Run tests
#
python dist/bin/omero web test webadmin
