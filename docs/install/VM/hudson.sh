URL=`wget -q -O- "http://hudson.openmicroscopy.org.uk/job/OMERO-trunk-qa-builds/lastSuccessfulBuild/api/xml?xpath=/freeStyleBuild/url/text()"`
FILE=`wget -q -O- "http://hudson.openmicroscopy.org.uk/job/OMERO-trunk-qa-builds/lastSuccessfulBuild/api/xml?xpath=//relativePath[contains(.,'server')]/text()"`
wget -q "$URL"artifact/$FILE
echo $FILE
