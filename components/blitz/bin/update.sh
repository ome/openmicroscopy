#!/bin/bash
#
# OMERO Grid Update Script
# Copyright 2008 Glencoe Software, Inc.  All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

OMERO_HOME=${OMERO_HOME:-"."}
$OMERO_HOME/bin/admin.sh -e "application update $OMERO_HOME/etc/grid/default.xml $*"
