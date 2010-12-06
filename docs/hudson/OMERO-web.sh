set -e
set -u
set -x

#
# Run tests
#
python bin/omero web test webadmin
