#!/bin/bash
# delete VM

export VMNAME=${VMNAME:-"$1"}
export VMNAME=${VMNAME:-"omerovm"}
if test -e $HOME/Library/VirtualBox; then
    export HARDDISKS=${HARDDISKS:-"$HOME/Library/VirtualBox/HardDisks/"}
elif test -e $HOME/.VirtualBox; then
    export HARDDISKS=${HARDDISKS:-"$HOME/.VirtualBox/HardDisks/"}
else
    echo "Cannot find harddisks! Trying setting HARDDISKS"
    exit 3
fi

set -e
set -u
set -x
if VBoxManage showvminfo "$VMNAME" | grep -q "running"
then
	VBoxManage controlvm "$VMNAME" poweroff
	sleep 20
fi
VBoxManage storageattach "$VMNAME" --storagectl "SATA CONTROLLER" --port 0 --device 0 --type hdd --medium none
VBoxManage unregistervm "$VMNAME" --delete
VBoxManage closemedium disk $HARDDISKS/"$VMNAME".vdi --delete

