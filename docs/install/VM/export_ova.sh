#!/bin/bash

export VMNAME=${VMNAME:-"$1"}
export VMNAME=${VMNAME:-"omerovm"}
export EXPORTVMNAME=${EXPORTVMNAME:-"$2"}
export EXPORTVMNAME=${EXPORTVMNAME:-"$VMNAME"}

set -e
set -u
set -x

VBOX="VBoxManage --nologo"

($VBOX list runningvms | grep "$VMNAME") && {
    echo "Stopping VM "
    $VBOX controlvm "$VMNAME" poweroff && sleep 5
}

if [[ -e $EXPORTVMNAME.ova ]]; then
	rm -f $EXPORTVMNAME.ova
fi

$VBOX export $VMNAME --output $EXPORTVMNAME.ova