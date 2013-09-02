#!/bin/bash
# There is an unresolved bug in which the Processor server becomes unresponsive
# See: http://trac.openmicroscopy.org.uk/ome/ticket/8266
# For some reason it currently (August 2013) appears in newly built VMs.
# Restarting just the Processor server usually fixes things

set -e -u -x

if [ "$ENABLE_OMERO_NO_PROCESSOR_FIX" -eq 0 ]; then
    exit 0
fi

"$OMERO_PREFIX/bin/omero" admin ice server stop Processor-0

set +e
for n in `seq 10`; do
    "$OMERO_PREFIX/bin/omero" admin ice server state Processor-0 | grep '^active'
    RET=$?
    if [ $RET -eq 0 ]; then
        break
    fi
    sleep 10
done
set -e

if [ $RET -ne 0 ]; then
    echo "ERROR: Processor is still activating."
fi

