#!/bin/sh
# install VM from snapshot
export VMNAME="OMERO"
export MEMORY="1024"
export SSH_PF="2222"
export OMERO_PORT="4064"
export OMERO_PF="4064"
VBoxManage clonehd ~/Library/VirtualBox/HardDisks/OMERO-INSTALL.vdi ~/Library/VirtualBox/HardDisks/$VMNAME.vdi
VBoxManage createvm --name "$VMNAME" --register --ostype "Ubuntu_64"
VBoxManage storagectl "$VMNAME" --name "IDE CONTROLLER" --add ide
VBoxManage storagectl "$VMNAME" --name "SATA CONTROLLER" --add sata
VBoxManage storageattach "$VMNAME" --storagectl "SATA CONTROLLER" --port 0 --device 0 --type hdd --medium ~/Library/VirtualBox/HardDisks/OMERO-INSTALL-CLONE.vdi
# VBoxManage storageattach "$VMNAME" --storagectl "IDE CONTROLLER" --device 0 --port 1 --type dvddrive --medium ~/Desktop/linux\ distros/ubuntu\ 10.04/ubuntu-10.04-server-amd64.iso
VBoxManage modifyvm "$VMNAME" --nic1 nat --nictype1 "Am79C973" 
VBoxManage modifyvm "$VMNAME" --memory $MEMORY --acpi on
VBoxManage setextradata "$VMNAME" "VBoxInternal/Devices/pcnet/0/LUN#0/Config/ssh/HostPort" $SSH_PF
VBoxManage setextradata "$VMNAME" "VBoxInternal/Devices/pcnet/0/LUN#0/Config/ssh/GuestPort" 22
VBoxManage setextradata "$VMNAME" "VBoxInternal/Devices/pcnet/0/LUN#0/Config/ssh/Protocol" TCP
VBoxManage setextradata "$VMNAME" "VBoxInternal/Devices/pcnet/0/LUN#0/Config/omeroserver/HostPort" $OMERO_PF
VBoxManage setextradata "$VMNAME" "VBoxInternal/Devices/pcnet/0/LUN#0/Config/omeroserver/GuestPort" $OMERO_PORT
VBoxManage setextradata "$VMNAME" "VBoxInternal/Devices/pcnet/0/LUN#0/Config/omeroserver/Protocol" TCP