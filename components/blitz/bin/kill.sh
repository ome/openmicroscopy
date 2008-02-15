#!/bin/bash
#
# OMERO Grid Kill Script
# Copyright 2007 Glencoe Software, Inc.  All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

OMERO_HOME=${OMERO_HOME:-"."}
kill -9 `cat $OMERO_HOME/var/master.pid`
