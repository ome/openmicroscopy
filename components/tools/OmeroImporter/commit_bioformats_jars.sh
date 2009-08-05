#!/bin/bash
#
# This script is intended to be called from lib/repository
# at the top-level (along with update_bioformats_jars)
#

set -e
set -u
set -x

OLD=$1
NEW=$2

perl -i -pe "s/versions.bio-formats=$OLD/versions.bio-formats=$NEW/" ../../etc/omero.properties
svn add ../../etc/omero.properties
LIST="bio-formats jai_imageio loci-common mdbtools-java ome-xml poi-loci"
for l in $LIST; do
    svn del --force $l-$OLD.jar
    svn add $l-$NEW.jar
    perl -i -pe "s/$l-$OLD.jar/$l-$NEW.jar/" ../../.classpath
done
svn add ../../.classpath
