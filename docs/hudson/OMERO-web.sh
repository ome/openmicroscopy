set -e
set -u
set -x

#
# Run tests
#
./build.py clean
./build.py

if [ $OMERO_PREFIX = 0 ]; then
    WEBPORT='4064'
else
    WEBPORT=$OMERO_PREFIX'4064'
fi

python dist/bin/omero config set omero.web.server_list '[["'$OMERO_HOST'", '$WEBPORT', "omero"]]'
python dist/bin/omero web unittest --config=$ICE_CONFIG --test=webadmin
