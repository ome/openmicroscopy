set -e
set -u
set -x

#
# Run tests
#
./build.py clean
./build.py

python dist/bin/omero config set omero.web.server_list '[["'$OMERO_HOST'", '$OMERO_PREFIX'4064, "omero"]]'
python dist/bin/omero web unittest --config=$ICE_CONFIG --test=webadmin
