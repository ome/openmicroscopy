#!/bin/bash
#
# OMERO Grid Admin Script
# Copyright 2008 Glencoe Software, Inc.  All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

OMERO_HOME=${OMERO_HOME:-"."}
if [ "$#" -eq 0 ]; then
    ICE_CONFIG=$OMERO_HOME/etc/internal.cfg icegridadmin
else
    ICE_CONFIG=$OMERO_HOME/etc/internal.cfg icegridadmin "$*"
fi
