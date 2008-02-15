#!/bin/bash
#
# OMERO Gui Startup
# Copyright 2007 Glencoe Software, Inc.  All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

LOCATIONS="/opt/local/share/java/IceGridGUI.jar /usr/lib/Ice-3.2.1/IceGridGUI.jar /usr/lib/Ice-3.2.2/IceGridGUI.jar"
for loc in $LOCATIONS; do
    if test -e $loc; then
        exec java -jar $loc $*
    fi
done
echo No IceGridGUI.jar found. Searched:
for loc in $LOCATIONS; do
    echo "    $loc"
done
false
