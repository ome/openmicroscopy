#!/bin/bash

export VMNAME=${VMNAME:-"$1"}
export VMNAME=${VMNAME:-"omerovm"}

set -e -u -x

VBOX="VBoxManage --nologo"

$VBOX list runningvms | grep "$VMNAME" || {
    echo "Starting VM "
    $VBOX startvm "$VMNAME" --type headless
    echo "Let the OS boot..."
    sleep 30
}
