#!/bin/bash

export VMNAME=${VMNAME:-"$1"}
export VMNAME=${VMNAME:-"omero-vm"}

set -e
set -u
set -x

VBOX="VBoxManage --nologo"

($VBOX list runningvms | grep "$VMNAME") && {
    echo "Stopping VM "
    $VBOX controlvm "$VMNAME" poweroff && sleep 5
}
