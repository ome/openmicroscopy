#!/bin/bash

set -e -u -x

export VMNAME=${VMNAME:-"$1"}
export VMNAME=${VMNAME:-"omerovm"}

VBOX="VBoxManage --nologo"


$VBOX list runningvms | grep "$VMNAME" && {
    $VBOX controlvm "$VMNAME" poweroff && sleep 5
}

$VBOX list vms | grep "$VMNAME" && {

	VBoxManage modifyvm "$VMNAME" --natpf1 "ssh,tcp,127.0.0.1,2222,10.0.2.15,22"
	VBoxManage modifyvm "$VMNAME" --natpf1 "omero-unsec,tcp,127.0.0.1,4063,10.0.2.15,4063"
	VBoxManage modifyvm "$VMNAME" --natpf1 "omero-ssl,tcp,127.0.0.1,4064,10.0.2.15,4064"
	VBoxManage modifyvm "$VMNAME" --natpf1 "omero-web,tcp,127.0.0.1,8080,10.0.2.15,8080"

}
