set -e
set -u
set -x

test -e dist/bin/omero && \
    (python dist/bin/omero web stop || echo 'Not running?')

#
# Run tests
#
./build.py clean
./build.py

if [ $OMERO_PREFIX = 0 ]; then
    ROUTER='4064'
else
    ROUTER=$OMERO_PREFIX'4064'
fi

if [ $OMERO_PREFIX = 0 ]; then
    WEBPORT='4080'
else
    WEBPORT=$OMERO_PREFIX'4080'
fi

#
# Create a user
#

python dist/bin/omero login -s $OMERO_HOST -p $ROUTER -u root -w ome
python dist/bin/omero group add web_group --perms=rwrw-- || echo "Web Group already exists?"
python dist/bin/omero user add web_user Web User web_group --userpassword abc || echo "Web User already exists?"
python dist/bin/omero logout

python dist/bin/omero config set omero.web.server_list '[["'$OMERO_HOST'", '$ROUTER', "omero"]]'
python dist/bin/omero config set omero.web.debug True
python dist/bin/omero web unittest --config=$ICE_CONFIG --test=webadmin

python dist/bin/omero web start

python dist/bin/omero web seleniumtest webadmin localhost 'http://'$OMERO_HOST':'$WEBPORT firefox --config=$ICE_CONFIG

python dist/bin/omero web stop

#
# Write test file for OMERO-web jobs
#
cd ..
FILE=web.xml
mkdir -p target/reports
wget -O - "http://hudson.openmicroscopy.org.uk/userContent/$FILE" > target/reports/$FILE
