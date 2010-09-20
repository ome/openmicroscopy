#!/bin/bash
# delete VM

export VMNAME=${VMNAME:-"$1"}
export VMNAME=${VMNAME:-"SEPT1"}
export HARDDISKS=${HARDDISKS:-"$HOME/Library/VirtualBox/HardDisks/"}

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

