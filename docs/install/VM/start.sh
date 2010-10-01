#!/bin/bash

export VMNAME=${VMNAME:-"$1"}
export VMNAME=${VMNAME:-"OMERO42"}

set -e
set -u
set -x

VBOX="VBoxManage --nologo"

$VBOX list runningvms | grep "$VMNAME" || {
    # Under what conditions should we restart
    # echo "Stopping VM "
    # $VBOX controlvm "$VMNAME" poweroff && sleep 10
    echo "Starting VM "
    $VBOX startvm "$VMNAME" --type headless
}
