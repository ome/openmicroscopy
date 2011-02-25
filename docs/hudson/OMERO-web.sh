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

#
# Create a user
#
python dist/bin/omero login -s $OMERO_HOST -p $WEBPORT -u root -w ome
python dist/bin/omero group add web_group --perms=rwrw--
python dist/bin/omero user add web_user Web User web_group --userpassword abc
python dist/bin/omero logout

python dist/bin/omero config set omero.web.server_list '[["'$OMERO_HOST'", '$WEBPORT', "omero"]]'
python dist/bin/omero web unittest --config=$ICE_CONFIG --test=webadmin

python dist/bin/omero web start

python dist/bin/omero web seleniumtest webadmin localhost 'http://'$OMERO_HOST':'$WEBPORT firefox --config=$ICE_CONFIG


#
# Write test file for OMERO-web jobs
#
cd ..
FILE=web.xml
mkdir -p target/reports
wget -O - "http://hudson.openmicroscopy.org.uk/userContent/$FILE" > target/reports/$FILE
